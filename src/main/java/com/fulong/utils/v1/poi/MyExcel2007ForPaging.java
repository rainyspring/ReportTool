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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author jx
 * XSSF and SAX (Event API) basic example.
 * See {@link XLSX2CSV} for a fuller example of doing
 *  XSLX processing with the XSSF Event code.
 */
@Deprecated
public class MyExcel2007ForPaging {
	/**
	 * 判断是否为行的正则
	 */
	private Pattern rowPattern =  Pattern.compile("^A[0-9]+$");
	
		public List<List<String>> dataList = new ArrayList<List<String>>();
		public final int startRow;
		public final int endRow;
		private int currentRow = 0;
		private final String filename;
		private List<String> rowData;
		
		public MyExcel2007ForPaging(String filename,int startRow,int endRow) throws Exception{
			if(StringUtils.isBlank(filename)) {
				throw new Exception("文件名不能空");
			}
			this.filename = filename;
			this.startRow = startRow;
			this.endRow = endRow;
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
			InputStream sheet1 = r.getSheet("rId1");
			InputSource sheetSource = new InputSource(sheet1);
			parser.parse(sheetSource);
			sheet1.close();
		}
		
		private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
			XMLReader parser =
				XMLReaderFactory.createXMLReader(
						"org.apache.xerces.parsers.SAXParser"
				);
			ContentHandler handler = new PagingHandler(sst);
			parser.setContentHandler(handler);
			return parser;
		}

		/** 
		 * See org.xml.sax.helpers.DefaultHandler javadocs 
		 */
		private  class PagingHandler extends DefaultHandler {
			private SharedStringsTable sst;
			private String lastContents;
			private boolean nextIsString;
			
			private PagingHandler(SharedStringsTable sst) {
				this.sst = sst;
			}
			/**
			 * 每个单元格开始时的处理
			 */
			@Override
			public void startElement(String uri, String localName, String name,
					Attributes attributes) throws SAXException {
				// c => cell
				if("c".equals(name)) {
					// Print the cell reference
//					System.out.print(attributes.getValue("r") + " - ");
					//获取索引值
					String index = attributes.getValue("r");
					//这是一个新行
					if(rowPattern.matcher(index).find()){
						
						//存储上一行数据
						if(rowData!=null&&isAccess()&&!rowData.isEmpty()){
							dataList.add(rowData);
						}
						rowData = new ArrayList<String>();;//新行要先清除上一行的数据
						currentRow++;//当前行+1
						System.out.println(currentRow);
					}
					if(isAccess()){
						// Figure out if the value is an index in the SST
						String cellType = attributes.getValue("t");
						if(cellType != null && cellType.equals("s")) {
							nextIsString = true;
						} else {
							nextIsString = false;
						}
					}
				
				}
				// Clear contents cache
				lastContents = "";
			}
			/**
			 * 每个单元格结束时的处理
			 */
			@Override
			public void endElement(String uri, String localName, String name)
					throws SAXException {
				if(isAccess()){
					// Process the last contents as required.
					// Do now, as characters() may be called more than once
					if(nextIsString) {
						int idx = Integer.parseInt(lastContents);
						lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
						nextIsString = false;
					}

					// v => contents of a cell
					// Output after we've seen the string contents
					if("v".equals(name)) {
//						System.out.println(lastContents);
						rowData.add(lastContents);
						
					}
				}
				
			}
			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				if(isAccess()){
					lastContents += new String(ch, start, length);
				}
				
			}
			/**
			 * 如果文档结束后，发现读取的末尾行正处在当前行中，存储下这行
			 * （存在这样一种情况，当待读取的末尾行正好是文档最后一行时，最后一行无法存到集合中，
			 * 因为最后一行没有下一行了，所以不为启动starElement()方法，
			 * 当然我们可以通过指定最大列来处理，但不想那么做，扩展性不好）
			 */
			@Override
			public void endDocument ()throws SAXException{
				if(rowData!=null&&isAccess()&&!rowData.isEmpty()){
					dataList.add(rowData);
					System.out.println("--end");
				}
				  
			}
		
		}
		private boolean isAccess(){
			if(currentRow>=startRow&&currentRow<=endRow){
				return true;
			}
			return false;
		}
		public static void main(String[] args) throws Exception {
			MyExcel2007ForPaging reader = new MyExcel2007ForPaging("E:/weld_small.xlsx",15,100);

			System.out.println("\n---"+reader.dataList);
			
		}
}
