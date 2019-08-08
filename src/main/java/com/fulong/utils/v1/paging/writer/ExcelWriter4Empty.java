package com.fulong.utils.v1.paging.writer;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fulong.utils.v1.paging.AbstractExcelWriter;
import com.fulong.utils.v1.paging.Paging;

/**
 * 无模板的Excel写入（包括xls和xlsx格式）
 * 
 * @author jx
 *
 * @param <T>
 * @param <E>
 */
public class ExcelWriter4Empty<T extends Paging> extends AbstractExcelWriter<T> {

	@Override
	protected void initWorkbook_sheet() throws IOException {

		// 根据模板格式来定输出文件格式
		if (this.isXlsx()) {
			wb = new XSSFWorkbook();
		
		} else {
			wb = new HSSFWorkbook();
		}
		this.sheet = wb.createSheet("sheet1");
	}
	/**
	 * 对于没有模板，即新文件的生成，都采用旧版xls
	 * @return
	 */
	private boolean isXlsx() {
		// TODO Auto-generated method stub
		return false;
	}
}
