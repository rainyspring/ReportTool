package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fulong.utils.v1.report.tool.POIUtil;
import com.fulong.utils.v1.tool.BaseHelper;
import com.fulong.utils.v2.error.MyExcelException;

public final class DomPOI {
	/*private  static DomPOI instance = null;
	
	private DomPOI() {

	}
	public static DomPOI getInstance() {
		if(instance==null){
			instance = new DomPOI();
		}
		return instance;
	}*/
	/**
	 * 导出Excel模板方法 根据文件名判断是那种类型(xls or xlsx)
	 * 
	 * @param fileName
	 * @param list
	 * @return
	 */
	public static Workbook getModelExcel(String fileName) {
		if (fileName.endsWith("xls")) {
			return getModelExcelByXls(fileName);
		}
		return getDefaultModelExcelByXlsx(fileName);
	}

	/**
	 * office Excel 97-2003
	 * 
	 * @param sheetName
	 * @param list
	 * @return
	 */
	private static HSSFWorkbook getModelExcelByXls(String sheetName) {

		String[] arr = getWeldTitles();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet1 = wb.createSheet("sheet1");
		HSSFRow row = sheet1.createRow(0);
		for (int i = 0; i < arr.length; i++) {

			row.createCell(i).setCellValue(arr[i]);

		}
		return wb;

	}
	/**
	 * Office 2007+
	 * @param sheetName
	 * @return
	 */
	private static Workbook getDefaultModelExcelByXlsx(String sheetName) {
		String[] arr = getWeldTitles();
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet1 = wb.createSheet("sheet1");
		XSSFRow row = sheet1.createRow(0);
		for (int i = 0; i < arr.length; i++) {

			row.createCell(i).setCellValue(arr[i]);

		}
		return wb;
	}
	/**
	 * 焊接导入的标题
	 * @return
	 */
	private static String[]  getWeldTitles(){
		return new String[] { "ISO图号*", "页码*", "ISO版本*", "焊缝号*",
				"管段号*", "预制位置*", "公称直径*", "焊缝类别*", "焊接类型*", "管道等级*", "区域*",
				"单元*", "管线号*", "油漆系统","元件编号1", "材料编码1", "材料小类1","元件编号2", "材料编码2", "材料小类2" };

	}
	/**
	 * @deprecated instead of using readExcel
	 * @param file
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	@Deprecated
	public static XSSFWorkbook readExcelByXlsx(File file)
			throws InvalidFormatException, IOException {
		return new XSSFWorkbook(file);
	}
	/**
	 * @deprecated instead of using readExcel
	 * @param in
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static XSSFWorkbook readExcelByXlsx(InputStream in)
			throws IOException {
		return new XSSFWorkbook(in);
	}
	/**
	 * 根据文件名自动识别使用POI何种类型的读取器
	 * @param file
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static Workbook readExcel(File file) throws InvalidFormatException, IOException{
		if(file==null||!file.exists()) {
			return null;
		}
		
		return DomPOI.readExcel(new FileInputStream(file), file.getName());
	}
	/**
	 * 根据文件名自动识别使用POI何种类型的读取器
	 * @param file
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static Workbook readExcel(InputStream io,String fileName) throws IOException {
		
		if(io==null){
			return null;
		}
		if(StringUtils.isBlank(fileName)){
			return new HSSFWorkbook(io);
		}
		
		if(fileName.toLowerCase().endsWith(".xlsx")){
			return new XSSFWorkbook(io);
		}
		
		return new HSSFWorkbook(io);
	}

	/**
	 * 利用POI的dom方式分页读取2007的Excel
	 * @deprecated
	 * @param file
	 * @param page
	 * @param rows
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 *//*
	@Deprecated
	public static List<List<String>> getPagingDataByPOIDom(File file, int page,
			int rows) throws InvalidFormatException, IOException {
		int headLineRowNum = SaxPOI.titleLineRowIndex;
		List<List<String>> dataList = new LinkedList<List<String>>();

		XSSFWorkbook wb = DomPOI.readExcelByXlsx(file);
		// 默认取第一个sheet
		XSSFSheet sheet = wb.getSheetAt(0);
		// int firstRowNum = sheet.getFirstRowNum();
		int lastRowNum = sheet.getLastRowNum();
		int colNum = sheet.getRow(0).getPhysicalNumberOfCells();

		// 循环的默认起始和结束
		int startRow = headLineRowNum + 1;
		int endRow = startRow + rows - 1;

		int sumOfRows = lastRowNum - headLineRowNum;// 实际总行数
		int odd = sumOfRows - ((page - 1) * rows);
		if ((page - 1) * rows >= sumOfRows) {// 本页之前的所有记录已经超过总数了
			return dataList;
		} else if (odd > 0 && odd < rows) {// 剩下的行数不够本页显示数
			// startRow =(headLineRowNum + 1)+ (page-1)*rows
			startRow = (headLineRowNum + 1) + (page - 1) * rows;
			endRow = lastRowNum;
		} else {// 所剩行数充足
			startRow = headLineRowNum + (page - 1) * rows + 1;
			endRow = headLineRowNum + page * rows;
		}

		for (int i = startRow; i <= endRow; i++) {
			XSSFRow row = sheet.getRow(i);
			// StringBuffer sb = new StringBuffer();
			List<String> dataRow = new LinkedList<String>();
			for (int j = 0; j < colNum; j++) {
				XSSFCell cell = row.getCell(j);
				String value = ExcelHelper.getCellValue(wb, cell);
				// sb.append(value + "#");
				dataRow.add(value);
			}
			dataList.add(dataRow);
		}
		return dataList;
	}*/
	/**
	 * 获取文件行数
	 * @param file
	 * @param headLineRowNum 标题行所处的行号（行号从0开始）
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public int getSize4TheFileByPOIDOM(File file, int headLineRowNum)
			throws IOException, InvalidFormatException {
		Workbook wb = DomPOI.readExcel(file);

		// 默认取第一个sheet
		Sheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();
		return lastRowNum - headLineRowNum;
	}
	/**
	 * 读取少量数据的Excel文件(不包含标题，标题默认在第0行)
	 * @param request
	 * @return
	 */
	public static List<List<String>> getDataBySingleUploadFile(
			HttpServletRequest request) throws IllegalStateException,
			IOException, MyExcelException, InvalidFormatException {
		
		return DomPOI.getDataBySingleUploadFile(request, SaxPOI.TITLELINE_ROW_INDEX);

	}
	
	/**
	 * 读取少量数据的Excel文件（需要指定是否数据中带有标题）
	 * @param request
	 * @param headLineRowByPOIDOM 【 n==-1 为带标题的数据集合，n>=0为忽略n行以前的数据】
	 * 注意：Excel文件从0行开始
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws MyExcelException
	 * @throws InvalidFormatException
	 */
	public static List<List<String>> getDataBySingleUploadFile(
			HttpServletRequest request,int headLineRowByPOIDOM) throws IllegalStateException,
			IOException, MyExcelException, InvalidFormatException {
		
		return DomPOI.getDataBySingleUploadFile(request, headLineRowByPOIDOM,-1);

	}
	/**
	 * 读取少量数据的Excel文件（需要指定是否数据中带有标题）
	 * @param request
	 * @param headLineRowByPOIDOM 【 n==-1 为所有行的数据集合，n>=0为忽略包括第n行及以前行的数据】
	 * 注意：Excel文件从0行开始
	 * @param maxCol 最大列，如果实际数据列不过，则按照实际列数据，如果实际大于maxCol，按照maxCol；maxCol=-1,则完全取实际列
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws MyExcelException
	 * @throws InvalidFormatException
	 */
	public static List<List<String>> getDataBySingleUploadFile(
			HttpServletRequest request,int headLineRowByPOIDOM,int maxCol) throws IllegalStateException,
			IOException, MyExcelException, InvalidFormatException {
		headLineRowByPOIDOM=headLineRowByPOIDOM<=-1?-1:headLineRowByPOIDOM;
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (!multipartResolver.isMultipart(request)) {
			throw new MyExcelException("spring 解析上传组件出现异常");
		}
		// 转换成多部分request
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		// 取得request中的所有文件名
		Iterator<String> iter = multiRequest.getFileNames();
		// while (iter.hasNext()) {
		// 记录上传过程起始时的时间，用来计算上传时间
		int pre = (int) System.currentTimeMillis();
		// 取得上传文件,默认只处理一个文件
		MultipartFile file = multiRequest.getFile(iter.next());
		if (BaseHelper.isNullorEmpty(file)){
			throw new MyExcelException("文件为空");
			}
		// 取得当前上传文件的文件名称
		String myFileName = file.getOriginalFilename();
		// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
		if (BaseHelper.isNullorEmpty(myFileName.trim())) {
			throw new MyExcelException("获取文件名为空");
		}
		
		List<List<String>> returnData = DomPOI.getData(
				file.getInputStream(), headLineRowByPOIDOM,maxCol,myFileName);

		return returnData;

	}
	/**
	 * 读取Excel文件的数据(不包含标题，如果要求数据中带标题，请使headLineRowNum=-1)
	 * @param in
	 * @param headLineRowNum -1 代表获取所有行，
	 * @param maxCol 最大列，如果实际数据列不过，则按照实际列数据，如果实际大于maxCol，按照maxCol；maxCol=-1,则完全取实际列
	 * @return
	 * @throws IOException
	 */
	public static List<List<String>> getData(InputStream in, int headLineRowNum,int maxCol,String fileName)
			throws IOException {
		Workbook wb = DomPOI.readExcel(in,fileName);
		// 默认取第一个sheet
		Sheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();

		// 除去第一行为标题，剩下的为数据列，遍历获取每一行数据
		List<List<String>> dataList = new LinkedList<List<String>>();
		for (int i = headLineRowNum + 1; i <= lastRowNum; i++) {
			Row row = sheet.getRow(i);
			int colNum = row.getPhysicalNumberOfCells();//该行的列数
			if(maxCol>-1){
				colNum = colNum>maxCol?maxCol:colNum;
			}
			// StringBuffer sb = new StringBuffer();
			List<String> dataRow = new LinkedList<String>();
			for (int j = 0; j < colNum; j++) {
				Cell cell = row.getCell(j);
				String value = POIUtil.getCellValue(cell);
				// sb.append(value + "#");
				dataRow.add(value);
			}
			dataList.add(dataRow);
		}
		return dataList;
	}
	/**
	 * 读取Excel文件的数据(不包含标题，如果要求数据中带标题，请使headLineRowNum=-1)
	 * @param file
	 * @param headLineRowNum 代表标题行所处的位置[第一行是0]
	 * @param maxCol 最大列，如果实际数据列小于maxCol，则取实际列数；如果实际列大于maxCol，按照maxCol；maxCol=-1,则完全取实际列
	 */
	public static List<List<String>> getData(File file, int headLineRowNum,int maxCol)
			throws IOException, InvalidFormatException {
		return DomPOI.getData(new FileInputStream(file), headLineRowNum,maxCol,file.getName());

	}
}
