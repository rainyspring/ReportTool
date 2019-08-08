package com.fulong.utils.v1.poi;

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * XSSF and SAX (Event API) basic example.
 * See {@link XLSX2CSV} for a fuller example of doing
 *  XSLX processing with the XSSF Event code.
 */
public class MyExcel2007ForMaxRow {
	/**
	 * 判断是否为行的正则
	 */
	private Pattern rowPattern =  Pattern.compile("^A[0-9]+$");
	//new add
	public long maxRow = 0;//记录总行数
	
	private String filename = null;
	public MyExcel2007ForMaxRow(String filename) throws Exception{
		if(StringUtils.isBlank(filename)) {
			throw new Exception("文件名不能空");
		}
		this.filename = filename;
		processFirstSheet();
	}
	/**
	 * 指定获取第一个sheet
	 * @param filename
	 * @throws Exception
	 */
	private void processFirstSheet() throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		// To look up the Sheet Name / Sheet Order / rID,
		//  you need to process the core Workbook stream.
		// Normally it's of the form rId# or rSheet#
		InputStream sheet2 = r.getSheet("rId1");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();//关闭流
		pkg.close();//关闭POI的sax流
	}
	
	private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser =
			XMLReaderFactory.createXMLReader(
					"org.apache.xerces.parsers.SAXParser"
			);
		ContentHandler handler = new MaxRowHandler();
		parser.setContentHandler(handler);
		return parser;
	}

	/** 
	 * See org.xml.sax.helpers.DefaultHandler javadocs 
	 */
	private  class MaxRowHandler extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			// c => cell
			if("c".equals(name)) {
				String index = attributes.getValue("r");
				if(rowPattern.matcher(index).find()){
					maxRow++;
				}
		
			}

		}
	
	}
	
	/*public static void main(String[] args) throws Exception {
		MyExcel2007ForMaxRow reader = new MyExcel2007ForMaxRow("E:/welding_small.xlsx");

		System.out.println("\n---"+reader.maxRow);
		
	}*/
}
