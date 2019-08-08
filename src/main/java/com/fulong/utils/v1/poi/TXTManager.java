package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TXTManager {
	/**
	 * 将model中的文件读取出来写到outFIle中，然后将其他数据追加到后边
	 * @param modelFile
	 * @param outFile
	 * @param myPaging
	 * @throws IOException 
	 * @throws Exception
	 */
	public static void export(File modelFile,File outFile,MyPaging myPaging) throws IOException{
		TXTWriter txt = new TXTWriter(modelFile, outFile);
		txt.putData2FileByList(myPaging);
	}
	/**
	 * 将model中的文件读取出来写到outFIle中，然后将其他数据追加到后边
	 * @param modelFile
	 * @param outFile
	 * @param myPaging
	 * @throws IOException 
	 * @throws Exception
	 */
	public static void exportByList(List<String> title,File outFile,MyPaging myPaging) throws IOException{
		TXTWriter txt = new TXTWriter(title, outFile);
		txt.putData2FileByList(myPaging);
	}
	/**
	 * 将model中的文件读取出来写到outFIle中，然后将其他数据追加到后边
	 * @param modelFile
	 * @param outFile
	 * @param myPaging
	 * @throws IOException 
	 * @throws Exception
	 */
	public static void exportByScroll(List<String> title,File outFile,MyPaging myPaging) throws IOException{
		TXTWriter txt = new TXTWriter(title, outFile);
		txt.putData2FileByScroll(myPaging);
	}
}
