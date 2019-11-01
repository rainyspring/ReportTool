package poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.fulong.utils.v2.poi.DomPOI;
import com.fulong.utils.v2.report.tool.POIUtil;

public class B {

	@Test
	public void test() throws IOException {
		int maxCol=1000 ;
		
		File f = new File("F:\\a\\handoverTemplet\\sh\\SH3503-J113表-合格焊工登记表.xlsx");
		FileInputStream in = new FileInputStream(f);

		Workbook wb = new XSSFWorkbook(in);
		System.out.println("*old*"+wb.getNumberOfSheets());
		wb.cloneSheet(0);
		wb.cloneSheet(0);
		System.out.println("*new*"+wb.getNumberOfSheets());

		for(int i=0;i<wb.getNumberOfSheets();i++) {
			System.out.println("*doooo*"+wb.getSheetName(i));

		}
		// 默认取第一个sheet
		Sheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();

		
		// 除去第一行为标题，剩下的为数据列，遍历获取每一行数据
		List<List<String>> dataList = new LinkedList<List<String>>();
		for (int i =0; i <= lastRowNum; i++) {
			Row row = sheet.getRow(i);
			
			
			short height = row.getHeight();
			float heightInPoints = row.getHeightInPoints();
			
			//System.out.println("height:["+height+"][heightInPoints:"+heightInPoints);
			
			int colNum = row.getPhysicalNumberOfCells();//该行的列数
			if(maxCol>-1){
				colNum = colNum>maxCol?maxCol:colNum;
			}
			StringBuffer sb = new StringBuffer();
			List<String> dataRow = new LinkedList<String>();
			for (int j = 0; j < colNum; j++) {
				int width = sheet.getColumnWidth(j);
				//System.out.println("width:["+width+"]");

				
				Cell cell = row.getCell(j);
				CellStyle style = cell.getCellStyle();
				
				style.setWrapText(true);  
				
				String value = POIUtil.getCellValue(cell);
				//System.out.println("--"+value);
				//sb.append(value + "#");
				//dataRow.add(value);
			}
			//dataList.add(dataRow);
		}
		
		wb.close();
		in.close();
		
	}

}
