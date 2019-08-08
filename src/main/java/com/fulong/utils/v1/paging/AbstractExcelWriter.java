package com.fulong.utils.v1.paging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.ScrollableResults;

import com.fulong.utils.v1.paging.render.HibernateRender;
import com.fulong.utils.v1.paging.render.AbstractSimallDataRender;
import com.fulong.utils.v1.tool.PathUtil;
import com.fulong.utils.v1.tool.RandomGUID;

/**
 * 在模板基础上的Excel写入
 * 
 * @author jx
 *
 * @param <T>
 * @param <E>
 */
public abstract class AbstractExcelWriter<T extends Paging> implements Writer<T> {

	private File outFile = null;

	// ------------sys params--------------
	// 数据列标题
	private List<String> listTitle = null;
	// Excel头
	private List<List<String>> headContent = null;
	/**
	 * 记录当前处在的行数(目前仅用于headContent和listTItle，数据起始行不受控制)
	 */
	private int startRowIndex = 0;
	/*
	 * 数据列处在的行数
	 */
	private int dataRow = 0;
	protected Workbook wb = null;
	protected FileOutputStream out = null;

	protected Sheet sheet = null;


	@Override
	public final File writeByPaging(T paging) throws Exception {

		this.init();
		if(wb==null){
			this.close();
			return null;
		}
		// 开始输出标题
		this.appendHeadContent();
		this.appendListTitle();
		
		if(paging==null){
			this.close();
			return this.outFile;
		}

		// 从DB中分页读取
		int startPage = Paging.DEFAULT_STARTPAGE;
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
			this.writeRowData(this.dataRow++, rowData,paging.getMaxCol());

		}
	}

	private void writeByHibernatePaging(HibernateRender paging, int page) throws Exception {

		ScrollableResults data = paging.getData(page, paging.getSpan());
		while (data.next()) {

			Object[] rowData = data.get();
			this.writeRowData(this.dataRow++, paging.transferPerObject(rowData),paging.getMaxCol());

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
	private void writeRowData(int rowIndex, Object[] rowData,int maxCol) {
		int length = rowData.length;
		if(length>maxCol && maxCol>=0){
			length = maxCol;
		}
		// 留出第一行的标题
		Row row = this.getRow(rowIndex);
		for (int j = 0; j < length; j++) {
			String value = String.valueOf(rowData[j]);
			if (!this.isAbsoluteBlank(value)) {
				this.getCell(row, j).setCellValue(value);
			}
		}
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {
		this.initWorkbook_sheet();
		this.initOutput();

	}
	/**
	 * 初始化输入流和Workbook和sheet,
	 * 但无需关闭，当然 你也可以重新关闭方法来自己关闭
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	protected abstract void initWorkbook_sheet() throws IOException;
	
	/**
	 * 初始化输出流
	 * @throws FileNotFoundException
	 */
	private void initOutput() throws FileNotFoundException {
		// 创建输出文件
		Path path = PathUtil.getTempSavePath("material");
		// 没有后缀名
		this.outFile = path.resolve("Excel-" + RandomGUID.getGUID()).toFile();
		out = new FileOutputStream(this.outFile);

	}

	/**
	 * 清理会场
	 * 
	 * @throws IOException
	 */
	private void close() throws IOException {
		
		this.closeWorkbook();
		this.closeOutput();

	}

	/**
	 * 关闭POI的关闭输入流、workbook
	 * 默认已经实现了一种通用的关闭，当然你也可以自己关闭
	 * @throws IOException
	 */
	protected void closeWorkbook() throws IOException{
		if(wb!=null){
			wb.write(out);
			wb.close();
		}
	
	}
	
	/**
	 * 关闭输出流
	 * @throws IOException
	 */
	private void closeOutput() throws IOException{
		if (out != null) {
			out.flush();
			out.close();
		}
	}
	/**
	 * 追加头内容
	 */
	private void appendHeadContent() {
		if (this.headContent == null) {
			return;
		}
		for (List<String> rowData : this.headContent) {
			Row r = this.getRow(this.startRowIndex++);
			for (int i = 0; i < rowData.size(); i++) {
				String s = rowData.get(i);
				if (!this.isAbsoluteBlank(s)) {
					this.getCell(r, i).setCellValue(s);
					;
				}
			}
		}

	}

	/**
	 * 追加数据列标题
	 */
	private void appendListTitle() {
		if (this.listTitle == null) {
			return;
		}
		Row r = this.getRow(this.startRowIndex++);
		for (int i = 0; i < this.listTitle.size(); i++) {
			String s = this.listTitle.get(i);
			if (!this.isAbsoluteBlank(s)) {
				this.getCell(r, i).setCellValue(s);
			}
		}

	}

	/**
	 * 是否是绝对的空值
	 * 
	 * @param s
	 * @return
	 */
	private boolean isAbsoluteBlank(String s) {
		if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取Excel的行，不存在则创建
	 * 
	 * @param indexR
	 * @return
	 */
	private Row getRow(int indexR) {
		Row r = this.sheet.getRow(indexR);
		if (r == null) {
			r = this.sheet.createRow(indexR);

		}
		return r;
	}

	/**
	 * 获取行的单元格，不存在则创建
	 * 
	 * @param r
	 * @param index
	 * @return
	 */
	private Cell getCell(Row r, int index) {
		Cell c = r.getCell(index);
		if (c == null) {
			c = r.createCell(index);
		}
		return c;
	}

	/**
	 * 设置数据列标题
	 * 
	 * @param listTitle
	 * @return
	 */
	public final AbstractExcelWriter setListTitle(List<String> listTitle) {

		this.listTitle = listTitle;
		this.dataRow += 1;
		return this;

	}

	/**
	 * 设置头内容
	 * 
	 * @param headContent
	 * @return
	 */
	public final AbstractExcelWriter setHeader(List<List<String>> headContent) {

		this.headContent = headContent;
		this.dataRow += headContent.size();
		return this;

	}

	/**
	 * 写入的起始行位置（从0开始为第一行）
	 * 
	 * @param index
	 * @return
	 */
	public final AbstractExcelWriter setStartRowIndex(int index) {

		this.startRowIndex = (index < 0 ? this.startRowIndex : index);
		this.dataRow += this.startRowIndex;
		return this;

	}

	public final AbstractExcelWriter appendStartDataRow(int n) {

		this.dataRow += (n < 0 ? 0 : n);
		return this;

	}
}
