package poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class B {

	@Test
	public void test() throws IOException {
		File f = new File("d:/a.xlsx");
		FileInputStream in = new FileInputStream(f);

		Workbook wb = new XSSFWorkbook(in);
		
		
		
		wb.close();
		in.close();
		
	}

}
