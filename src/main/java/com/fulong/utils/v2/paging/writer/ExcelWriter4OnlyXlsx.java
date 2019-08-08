package com.fulong.utils.v2.paging.writer;

import java.io.IOException;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fulong.utils.v2.paging.AbstractExcelWriter;
import com.fulong.utils.v2.paging.Paging;

/**
 * 无模板的Excel写入（仅支持xlsx格式） 优点：支持大数据写入，写入过程结合POI处理大数据写入特点 技术点：使用POI的SXSSFWorkbook类
 * 
 * @author jx
 *
 * @param <T>
 * @param <E>
 */
public class ExcelWriter4OnlyXlsx<T extends Paging> extends AbstractExcelWriter<T> {

	// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
	private SXSSFWorkbook cacheWorkbook = null;

	@Override
	protected void initWorkbook_sheet() throws IOException {

		// 根据模板格式来定输出文件格式
		super.wb = new XSSFWorkbook();
		// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
		this.cacheWorkbook = new SXSSFWorkbook((XSSFWorkbook)super.wb, 5000);
		this.sheet = this.cacheWorkbook.createSheet("sheet1");

	}

	@Override
	protected void closeWorkbook() throws IOException {
		
		if(cacheWorkbook!=null){
			cacheWorkbook.write(super.out);
			cacheWorkbook.close();
		}
		if(super.wb!=null){
			super.wb.close();
		}
	}
	
}
