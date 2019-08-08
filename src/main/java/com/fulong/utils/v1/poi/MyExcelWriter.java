package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.ScrollableResults;
/**
 * @deprecated
 * @author jx
 *
 */
@Deprecated
public class MyExcelWriter {
	/*
	 * data params
	 */
	private File outFile = null;
	private File modelFile = null;
	private List<String> listTitle = null;
	private List<List<String>> headContent = null;

	// ------------sys params--------------
	/*
	 * 数据列处在的行数
	 */
	private int dataRow = 0;
	private Workbook workbook = null;
	private FileInputStream in = null;
	private FileOutputStream fout = null;
	// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
	private Workbook wb = null;
	private Sheet sheet = null;

	private boolean isNewFile = false;
	/**
	 * 记录当前处在的行数(目前仅用于headContent和listTItle，数据起始行不受控制)
	 */
	private int startRowIndex = 0;

	/**
	 * 在模板基础上进行写操作（模板文件中已经写好了格式和标题）
	 * 
	 * @param modelFileNameWithPath
	 *            模板文件名
	 * @throws Exception
	 */
	public MyExcelWriter(File outFile) {

		this.outFile = outFile;
	}

	public MyExcelWriter setModelFile(File modelFile) {

		this.modelFile = modelFile;
		return this;

	}

	/**
	 * 设置数据列标题
	 * 
	 * @param listTitle
	 * @return
	 */
	public MyExcelWriter setListTitle(List<String> listTitle) {

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
	public MyExcelWriter setHeader(List<List<String>> headContent) {

		this.headContent = headContent;
		this.dataRow += headContent.size();
		return this;

	}
	/**
	 * 写入的起始行位置（从0开始为第一行）
	 * @param index
	 * @return
	 */
	public MyExcelWriter setStartRowIndex(int index) {

		this.startRowIndex = (index<0?this.startRowIndex:index);
		this.dataRow+=this.startRowIndex;
		return this;

	}
	public MyExcelWriter appendStartDataRow(int n) {

		this.dataRow+=(n<0?0:n);
		return this;

	}
	/**
	 * 普通的写数据方案（采用分页读取数据逐步写入的方式，减小每次写入的数据行数， 同时也限制了每次读取的数据行数，进而控制内存消耗）
	 * 
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void write2FileByList(MyPaging myPaging) throws InvalidFormatException, IOException {

		this.init();

		// 开始输出标题
		this.appendHeadContent();
		this.appendListTitle();

		// 从DB中分页读取
		int page = MyPaging.STARTPAGE;
		int rows = myPaging.getSpan();
		// 只调用一次
		long sumOfPages = myPaging.getSumOfPages();

		// 分页从DB中读取
		for (int p = page; p <= sumOfPages; p++) {
			
			List data = myPaging.getDataByPaging4NestedList(p, rows);
			// 将数据写入Excel
			if (data == null || data.size() <= 0) {
				continue;//跳过
			}
			// 判断接收的数据结构
			boolean isArray = false;
			if (data.get(0) instanceof Object[]) {
				isArray = true;
			}

			int size = data.size();
			for (int index = 0; index < size; index++) {//遍历行
				Object[] rowData = null;
				if (isArray) {//数组结构
					rowData = (Object[]) data.get(index);
				} else {
					List<Object> t = (List<Object>) data.get(index);
					rowData = t.toArray();
				}
				
				if(rowData==null||rowData.length<=0){
					continue;//跳出行循环
				}
				
				
				Row row = this.getRow(this.dataRow++);
				
				int length = rowData.length;
				for (int j = 0; j < length; j++) {
					String value = String.valueOf(rowData[j]);
					if (!this.isAbsoluteBlank(value)) {
						this.getCell(row,j).setCellValue(value);
					}

				}

			}

		}
		//清场
		this.close();

	}

	/**
	 * 结合hibernate获取大数据采用滚动式提取数据的方式，性能好，内存消耗低
	 * 
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void write2FileByScroll(MyPaging myPaging) throws InvalidFormatException, IOException {

		this.init();

		// 开始输出标题
		this.appendHeadContent();
		this.appendListTitle();

		// 从DB中分页读取
		int page = MyPaging.STARTPAGE;
		int rows = myPaging.getSpan();
		// 只调用一次
		long sumOfPages = myPaging.getSumOfPages();

		// 分页从DB中读取
		for (int p = page; p <= sumOfPages; p++) {
			ScrollableResults data = myPaging.getDataByPaging4Scroll(p, rows);
			while (data.next()) {

				Object[] rowData = data.get();
				int length = rowData.length;
				// 留出第一行的标题
				Row row = this.getRow(this.dataRow++);
				for (int j = 0; j < length; j++) {
					String value = String.valueOf(rowData[j]);
					if (!this.isAbsoluteBlank(value)) {
						this.getCell(row, j).setCellValue(value);
					}
				}

			}
		}

		this.close();

	}

	/**
	 * 初始化
	 * @throws IOException
	 */
	private void init() throws IOException {

		//获取excel文件类型
		String outFileName = this.outFile.getName();
		// init file and workbook
		if (modelFile != null && modelFile.exists()) {

			in = new FileInputStream(modelFile);
			if(outFileName.endsWith("xlsx")){
				workbook = new XSSFWorkbook(in);
			}else{
				workbook = new HSSFWorkbook(in);
			}

		} else if (listTitle != null && listTitle.size() > 0) {
			
			if(outFileName.endsWith("xlsx")){
				workbook = new XSSFWorkbook();
			}else{
				workbook = new HSSFWorkbook();
			}

			this.isNewFile = true;
		}

		fout = new FileOutputStream(outFile);
		// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
		//wb = new SXSSFWorkbook((XSSFWorkbook)workbook, 5000);
		wb = workbook;
		
		// init sheet
		if (!this.isNewFile) {
			sheet = wb.getSheetAt(0);
		} else {
			sheet = wb.createSheet("sheet1");
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
	 * 清理会场
	 * 
	 * @throws IOException
	 */
	private void close() throws IOException {
		wb.write(fout);
		wb.close();
		workbook.close();
		if (fout != null) {
			fout.flush();
			fout.close();
		}

		if (in != null) {
			in.close();
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

}
