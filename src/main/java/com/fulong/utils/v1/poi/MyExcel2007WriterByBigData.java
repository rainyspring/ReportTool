package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.ScrollableResults;

public class MyExcel2007WriterByBigData {
	public File outFile = null;
	public File modelFile = null;
	public List<String>  title = null;
	/*
	 * 默认 第0行放置标题,如果以上条件都不满足，标题跳过，只保留标题位置
	 */
	private  int indexRow = 1 ;

	/**
	 * 在模板基础上进行写操作（模板文件中已经写好了格式和标题）
	 * @param modelFileNameWithPath 模板文件名
	 * @throws Exception
	 */
	public MyExcel2007WriterByBigData(File modelFile, File outFile){

		this.outFile = outFile;
		this.modelFile = modelFile;
	}
	public MyExcel2007WriterByBigData(List<String>  title, File outFile){

		this.outFile = outFile;
		this.title = title;
		
	}
	/**
	 * 在模板基础上进行写操作（模板文件中已经写好了格式和标题）,大数据性能极差
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void putData2FileByList(MyPaging myPaging)
			throws InvalidFormatException, IOException {
		XSSFWorkbook workbook = null;
		FileInputStream in = null;
		boolean customTitle = false;
		if(modelFile!=null&&modelFile.exists()){
			in = new FileInputStream(modelFile);
			workbook = new XSSFWorkbook(in);
		}else if(title!=null&&title.size()>0){
			workbook = new XSSFWorkbook();
			customTitle = true;
		}
		//int indexRow =1;//第0行放置标题,如果以上条件都不满足，标题跳过，只保留标题位置
		
		FileOutputStream fout = new FileOutputStream(outFile);
		// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
		SXSSFWorkbook wb = new SXSSFWorkbook(workbook, 5000);

		SXSSFSheet sheet = null;
		
		//开始输出标题
		if(customTitle){
			sheet = wb.createSheet("sheet1");
			SXSSFRow row0 = sheet.createRow(0);
			for(int i=0 ;i<title.size();i++){
				String s = title.get(i);
				if (s != null && !"null".equals(s)) {
					row0.createCell(i).setCellValue(s);
				}
			}
		}else{
			sheet = wb.getSheetAt(0);
		}
		
		//从DB中分页读取
		int page=MyPaging.STARTPAGE;
		int rows =myPaging.getSpan();
		//只调用一次
		long sumOfPages = myPaging.getSumOfPages();
		
		//分页从DB中读取
		for(int p=page;p<=sumOfPages;p++){
			List data = myPaging.getDataByPaging4NestedList(p, rows);
			//System.out.println("####---"+data);
			// 将数据写入Excel
			if (data != null && data.size() > 0) {
				//判断数据结构
				boolean isArray = false;
				if(data.get(0) instanceof Object[]){
					isArray = true;
				}
				
				int size = data.size();
				for (int index = 0; index < size; index++) {
					Object o = data.get(index);
					int length = 0;
					if(isArray){
						Object[] rowData = (Object[]) data.get(index);
						length = rowData.length;
						// 留出第一行的标题
						SXSSFRow row = sheet.createRow(indexRow++);
						for (int j = 0; j < length; j++) {
							String value = String.valueOf(rowData[j]);
							if (value != null && !"null".equals(value)) {
								row.createCell(j).setCellValue(value);
							}

						}
					}else{
						List<Object> rowData = (List<Object>) data.get(index);
						length = rowData.size();
						// 留出第一行的标题
						SXSSFRow row = sheet.createRow(indexRow++);
						for (int j = 0; j < length; j++) {
							Object value = rowData.get(j);
							if (value != null && !"null".equals(value.toString())) {
								row.createCell(j).setCellValue(value.toString());
							}

						}
					}
					
				}

			}

			
		}
		

		wb.write(fout);
		wb.close();
		workbook.close();
		fout.flush();
		fout.close();
		if(in!=null){
			in.close();
		}
		

	}
	
	/**
	 * 在模板基础上进行写操作（模板文件中已经写好了格式和标题）,大数据性能极差
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void putData2FileByScroll(MyPaging myPaging)
			throws InvalidFormatException, IOException {
		XSSFWorkbook workbook = null;
		FileInputStream in = null;
		boolean customTitle = false;
		if(modelFile!=null&&modelFile.exists()){
			in = new FileInputStream(modelFile);
			workbook = new XSSFWorkbook(in);
		}else if(title!=null&&title.size()>0){
			workbook = new XSSFWorkbook();
			customTitle = true;
		}
		int indexRow =1;//第0行放置标题,如果以上条件都不满足，标题跳过，只保留标题位置
		
		FileOutputStream fout = new FileOutputStream(outFile);
		// 内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘
		SXSSFWorkbook wb = new SXSSFWorkbook(workbook, 5000);

		SXSSFSheet sheet = null;
		
		//开始输出标题
		if(customTitle){
			sheet = wb.createSheet("sheet1");
			SXSSFRow row0 = sheet.createRow(0);
			for(int i=0 ;i<title.size();i++){
				String s = title.get(i);
				if (s != null && !"null".equals(s)) {
					row0.createCell(i).setCellValue(s);
				}
			}
		}else{
			sheet = wb.getSheetAt(0);
		}
		
		//从DB中分页读取
		int page=MyPaging.STARTPAGE;
		int rows =myPaging.getSpan();
		//只调用一次
		long sumOfPages = myPaging.getSumOfPages();
		
		//分页从DB中读取
		for(int p=page;p<=sumOfPages;p++){
			ScrollableResults data = myPaging.getDataByPaging4Scroll(p, rows);
			while(data.next()) {
				
				Object[] rowData =  data.get();
				int length = rowData.length;
					// 留出第一行的标题
				SXSSFRow row = sheet.createRow(indexRow++);
				for (int j = 0; j < length; j++) {
					String value = String.valueOf(rowData[j]);
					if (value != null && !"null".equals(value)) {
						row.createCell(j).setCellValue(value);
					}
				}
				
			}	
		}
		

		wb.write(fout);
		wb.close();
		workbook.close();
		fout.flush();
		fout.close();
		if(in!=null){
			in.close();
		}
		

	}
}
