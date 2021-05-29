package xu.jiang.report.paging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.hibernate.ScrollableResults;

import com.opencsv.CSVWriter;

import xu.jiang.report.paging.render.AbstractSimallDataRender;
import xu.jiang.report.paging.render.HibernateRender;
import xu.jiang.report.util.PathUtil;
import xu.jiang.report.util.RandomGUID;

/**
 * Csv数据写入器
 * 
 * @author jx
 * @param <T>
 * @param <E>
 */
public class CsvWriter<T extends Paging> implements Writer<T> {

	private File outFile = null;
	private CSVWriter csvWriter = null;
	// ------------sys params--------------
	// csv标题
	private List<String> listTitle = null;

	@Override
	public final File writeByPaging(T paging) throws Exception {

		this.init();
		this.appendListTitle();

		if (paging == null) {
			this.close();
			return this.outFile;
		}

		// 从DB中分页读取
		int startPage = Paging.DEFAULT_START_PAGE;
		// 只调用一次
		long sumOfPages = paging.getSumOfPages();

		// 分页从DB中读取
		for (int page = startPage; page <= sumOfPages; page++) {

			if (paging instanceof HibernateRender) {
				writeByHibernatePaging((HibernateRender) paging, page);

			} else if (paging instanceof AbstractSimallDataRender) {
				writeBySimallDataPaging((AbstractSimallDataRender) paging, page);

			}

		}

		this.close();
		return outFile;
	}

	/**
	 * 处理PagingL类型为SimallDataRender的处理逻辑
	 * 
	 * @param paging
	 * @param page
	 * @throws Exception
	 */
	private final void writeBySimallDataPaging(AbstractSimallDataRender paging, int page) throws Exception {

		List data = paging.getData(page, paging.getSpan());
		// 将数据写入Excel
		if (data == null || data.size() <= 0) {
			return;// 跳过
		}
		// 判断接收的行的数据结构
		boolean isArray = false;
		if (data.get(0) instanceof Object[]) {
			isArray = true;
		}

		int size = data.size();
		for (int index = 0; index < size; index++) {// 遍历行
			Object[] rowData = null;
			if (isArray) {// 数组结构
				rowData = (Object[]) data.get(index);
			} else {
				List<Object> t = (List<Object>) data.get(index);
				rowData = t.toArray();
			}

			if (rowData == null || rowData.length <= 0) {
				continue;// 跳出行循环
			}
			// 将内容写入行
			this.writeRowData(rowData, paging.getMaxCol());

		}
	}

	/**
	 * 处理PagingL类型为HibernateRender的处理逻辑
	 * 
	 * @param paging
	 * @param page
	 * @throws Exception
	 */
	private void writeByHibernatePaging(HibernateRender paging, int page) throws Exception {

		ScrollableResults data = paging.getData(page, paging.getSpan());
		while (data.next()) {

			Object[] rowData = data.get();
			this.writeRowData(paging.transferPerObject(rowData), paging.getMaxCol());

		}

	}

	/**
	 * 将
	 * 
	 * @param rowIndex
	 *            要写入的行号
	 * @param rowData
	 *            行内容
	 */
	private void writeRowData(Object[] rowData, int maxCol) {

		// 写入行数据
		this.csvWriter.writeNext(this.transferFromArrObjects2ArrStrs(rowData, maxCol));

	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {
		// 创建输出文件
		Path path = PathUtil.getTempSavePath("report");
		// 没有后缀名
		this.outFile = path.resolve("CSV-" + RandomGUID.getGUID() + ".csv").toFile();
		/**
		 * 使用开源组件CSVWriter默认设置：逗号分隔，双引号是转移符， 且逗号间的值统一都加双引号，双引号内容中的逗号无需转移，
		 * 仅当内容中有双引号时需转义
		 * 而换行符：
		 * windows下的点一下回车，效果是：回车换行，就是\r\n
		 * unix系统下的回车一下就是一个\n
		 */
		//csvWriter = new CSVWriter(new FileWriter(this.outFile), ',');
		csvWriter = new CSVWriter(new FileWriterWithEncoding(outFile,"UTF-8"),CSVWriter.DEFAULT_SEPARATOR ,CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\r\n");

	}

	/**
	 * 清理会场 csvWriter会自动关闭包含的输出流
	 * 
	 * @throws IOException
	 */
	private void close() throws IOException {
		if (this.csvWriter != null) {
			this.csvWriter.close();
		}
	}

	/**
	 * 追加数据列标题
	 */
	private void appendListTitle() {
		if (this.listTitle == null) {
			return;
		}
		String[] t = new String[this.listTitle.size()];
		this.csvWriter.writeNext(this.listTitle.toArray(t));
	}

	/**
	 * 并行处理 将Object[]-->String[] 适合大数据处理
	 * 
	 * @param arrO
	 * @param maxCol
	 *            列边界:如果数据超出边界，则截断；如果小于边界，则用空串补足
	 * @return
	 */
	private String[] transferFromArrObjects2ArrStrs(Object[] arrO, int maxCol) {
		if (arrO == null) {
			return null;
		}

		String[] arrS = new String[maxCol];
		int length = arrO.length;

		Arrays.parallelSetAll(arrS, new IntFunction<Object>() {
			@Override
			public Object apply(int i) {
				if (i < length) {
					arrS[i] = (arrO[i] == null ? "" : String.valueOf(arrO[i].toString()));
				} else {
					arrS[i] = "";// 补空
				}
				return arrS[i];
			}

		});
		return arrS;
	}

	/**
	 * 设置数据列标题
	 * 
	 * @param listTitle
	 * @return
	 */
	public final CsvWriter setListTitle(List<String> listTitle) {

		this.listTitle = listTitle;
		return this;

	}

	public static void main(String[] args) throws IOException {
		File outFile = new File("d://CSV-xx.csv");

		CSVWriter w = new CSVWriter(new FileWriter(outFile),CSVWriter.DEFAULT_SEPARATOR ,CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\r\n");
		w.writeNext(new String[]{"1","11","xxx"},true);
		w.writeNext(new String[]{"2","22","y"});
		w.writeNext(new String[]{"3","33","z"});
		w.close();
	}

}
