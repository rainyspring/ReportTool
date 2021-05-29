package xu.jiang.report.v2.paging.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xu.jiang.report.v2.paging.AbstractExcelWriter;
import xu.jiang.report.v2.paging.Paging;

/**
 * 在模板基础上的Excel写入（包括xls和xlsx格式）
 * 
 * @author jx
 *
 * @param <T>
 * @param <E>
 */
public class ExcelWriter4Templet<T extends Paging> extends AbstractExcelWriter<T> {

	private File templet;
	private FileInputStream in = null;

	public ExcelWriter4Templet(File templet) {
		this.templet = templet;

	}

	/**
	 * 初始化输入流
	 * 
	 * @throws FileNotFoundException
	 */
	@Override
	protected void initWorkbook_sheet() throws IOException {

		this.in = new FileInputStream(this.templet);

		// 根据模板格式来定输出文件格式
		if (this.isXlsx()) {
			wb = new XSSFWorkbook(in);
		
		} else {
			wb = new HSSFWorkbook(in);
		}
		this.sheet = wb.getSheetAt(0);


	}

	private boolean isXlsx() {
		// TODO Auto-generated method stub
		return this.templet.getName().endsWith("xlsx");
	}

	@Override
	protected void closeWorkbook() throws IOException {
		// TODO Auto-generated method stub
		super.closeWorkbook();
		if(this.in!=null){
			in.close();
		}
	}
	
}
