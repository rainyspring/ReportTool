package com.fulong.utils.v1.poi;

import java.io.File;
import java.util.List;

/**
 * @deprecated
 * @author jx
 *
 */
@Deprecated
public class POIManager {
	public static final int HEADLINE_ROW_POI_DOM = 0;
	
	public static void exportExcel2007ByList(List<String> title,File outFile,MyPaging myPaging) throws Exception{
		MyExcel2007WriterByBigData xlsx = new MyExcel2007WriterByBigData(title, outFile);
		xlsx.putData2FileByList(myPaging);
	}
	public static void exportExcel2007ByScroll(List<String> title,File outFile,MyPaging myPaging) throws Exception{
		MyExcel2007WriterByBigData xlsx = new MyExcel2007WriterByBigData(title, outFile);
		xlsx.putData2FileByScroll(myPaging);
	}

}
