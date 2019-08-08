package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MyExcel2007WriterBySmallData {
	public File outFile;
	public File modelFile;

	/**
	 * 
	 * @param modelFileNameWithPath 模板文件名
	 * @throws Exception
	 */
	public MyExcel2007WriterBySmallData(File modelFile,File outFile) throws Exception{

		this.outFile = outFile;
		this.modelFile = modelFile;
		if(modelFile==null||!modelFile.exists()){
			throw new Exception("文件不存在！");
		}
		
	}
	/**
	 * 
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void  putData2File(List<List<String>>  data) throws InvalidFormatException, IOException {
		
		FileInputStream in = new FileInputStream(modelFile);
		FileOutputStream fout = new FileOutputStream(outFile);
		XSSFWorkbook workbook = new XSSFWorkbook(in);
		//内存中保留 10000 条数据，以免内存溢出，其余写入 硬盘		
		SXSSFWorkbook wb = new SXSSFWorkbook(workbook, 5000);
		//将数据写入Excel
		if(data!=null&&data.size()>0) {
			
			SXSSFSheet sheet = wb.getSheetAt(0);
			int lastRow = sheet.getLastRowNum();
			System.out.println(lastRow);
			
			int size = data.size();
			for(int indexRow = 0 ;indexRow<size ;indexRow++ ){
				List<String> rowData = data.get(indexRow);
				//留出第一行的标题
				SXSSFRow row = sheet.createRow(indexRow);

				for(int j=0;j<rowData.size();j++){
					String value = rowData.get(j);
					if(value!=null && !"null".equals(value)){
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
		in.close();

	}
	public static void main(String[] args) throws Exception {
		File modelFile = new File("E:/b.xlsx");
		MyExcel2007WriterBySmallData excel = new MyExcel2007WriterBySmallData(modelFile,new File("E:/c.xlsx"));
		List<List<String>> data = new ArrayList<List<String>>();
		for(int i=0;i<100;i++){
			List<String> rowData = new ArrayList<String>();
			rowData.add(String.valueOf(new Random(10).nextInt(12)));
			rowData.add(String.valueOf(new Random(12).nextInt(30)));
			rowData.add(null);
			rowData.add(String.valueOf(new Random(30).nextInt(52)));
			rowData.add(String.valueOf(new Random(5).nextInt(8)));
			rowData.add(String.valueOf(new Random(25).nextInt(9)));
			data.add(rowData);
		}
		excel.putData2File(data);
		System.out.println(data);
		System.out.println("----------------");
	}
}
