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
 * XSSF and SAX (Event API) basic example. See {@link XLSX2CSV} for a fuller
 * example of doing XSLX processing with the XSSF Event code.
 */
public class MyExcel2007ForPaging_high {

	/**
	 * 代表Excel中必须有值的起始列(A、B、C....AA、AB...)
	 */
	private static final String INDEX_COL_DATA = "A";
	
	public List<List<IndexValue>> dataList = new ArrayList<List<IndexValue>>();
	private final int startRow;
	private final int endRow;
	private int currentRow = 0;
	private final String filename;
	private List<IndexValue> rowData;

	public MyExcel2007ForPaging_high(String filename, int startRow, int endRow) throws Exception {
		if (StringUtils.isBlank(filename)) {
			throw new Exception("文件名不能空");
		}
		this.filename = filename;
		this.startRow = startRow;
		this.endRow = endRow;
		processFirstSheet();
	}

	/**
	 * 指定获取第一个sheet
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void processFirstSheet() throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		// To look up the Sheet Name / Sheet Order / rID,
		// you need to process the core Workbook stream.
		// Normally it's of the form rId# or rSheet#
		InputStream sheet1 = r.getSheet("rId1");
		InputSource sheetSource = new InputSource(sheet1);
		parser.parse(sheetSource);
		sheet1.close();
		pkg.close();
	}

	private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new PagingHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private class PagingHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		private String index = null;

		private PagingHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		/**
		 * 每个单元格开始时的处理
		 */
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// c => cell
			if ("c".equals(name)) {
				// Print the cell reference
				// System.out.print(attributes.getValue("r") + " - ");

				index = attributes.getValue("r");
				System.out.println(index);
				if (index.contains("N")) {
					System.out.println("##" + attributes + "##");
				}

				// 这是一个新行
				if (Pattern.compile("^"+INDEX_COL_DATA+"[0-9]+$").matcher(index).find()) {

					// 存储上一行数据
					if (rowData != null && isAccess() && !rowData.isEmpty()) {
						dataList.add(rowData);
					}
					rowData = new ArrayList<IndexValue>();
					;// 新行要先清除上一行的数据
					currentRow++;// 当前行+1
					// System.out.println(currentRow);
				}
				if (isAccess()) {
					// Figure out if the value is an index in the SST
					String cellType = attributes.getValue("t");
					if (cellType != null && cellType.equals("s")) {
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
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (isAccess()) {
				// Process the last contents as required.
				// Do now, as characters() may be called more than once
				if (nextIsString) {
					int idx = Integer.parseInt(lastContents);
					lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
					nextIsString = false;
				}

				// v => contents of a cell
				// Output after we've seen the string contents
				if ("v".equals(name)) {
					// System.out.println(lastContents);

					rowData.add(new IndexValue(index, lastContents));

				}
			}

		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (isAccess()) {
				lastContents += new String(ch, start, length);
			}

		}

		/**
		 * 如果文档结束后，发现读取的末尾行正处在当前行中，存储下这行
		 * （存在这样一种情况，当待读取的末尾行正好是文档最后一行时，最后一行无法存到集合中，
		 * 因为最后一行没有下一行了，所以不为启动starElement()方法， 当然我们可以通过指定最大列来处理，但不想那么做，扩展性不好）
		 */
		@Override
		public void endDocument() throws SAXException {
			if (rowData != null && isAccess() && !rowData.isEmpty()) {
				dataList.add(rowData);
				System.out.println("--end");
			}

		}

	}

	private boolean isAccess() {
		if (currentRow >= startRow && currentRow <= endRow) {
			return true;
		}
		return false;
	}

	private class IndexValue {
		String v_index;
		String v_value;

		public IndexValue(String v_index, String v_value) {
			super();
			this.v_index = v_index;
			this.v_value = v_value;
		}

		@Override
		public String toString() {
			return "IndexValue [v_index=" + v_index + ", v_value=" + v_value + "]";
		}

		/**
		 * ！！此处只能比较英文字符相同且只有最后一个英文字符不同的2个单元格
		 * @param p
		 * @return
		 */
		public int getLevel(IndexValue p) {

			char[] other = p.v_index.replaceAll("[0-9]", "").toCharArray();
			char[] self = this.v_index.replaceAll("[0-9]", "").toCharArray();
			if (other.length != self.length) {
				return -1;
			}
			for (int i = 0; i < other.length; i++) {
				if (i == other.length - 1) {
					return self[i] - other[i];
				} else {
					if (self[i] != other[i]) {
						return -1;
					}
				}

			}
			return -1;

		}
	}

	/**
	 * 获取真实的数据（处理空格）
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<List<String>> getMyDataList() throws Exception {

		List<List<String>> myDataList = new ArrayList<List<String>>();
		if (dataList == null || dataList.size() <= 0) {
			return myDataList;
		}

		for (int i = 0; i < dataList.size(); i++) {
			List<IndexValue> i_list = dataList.get(i);
			List<String> tem = new ArrayList<String>();
			int j = 0;
			for (; j < i_list.size() - 1; j++) {
				// 获取当前值,并存储
				IndexValue current = i_list.get(j);
				//去掉空格
				String tempV = current.v_value!=null?current.v_value.trim():current.v_value;
				tem.add(tempV);
				// 预存下一个
				IndexValue next = i_list.get(j + 1);
				// 获取差值
				int level = next.getLevel(current);
				if (level <= 0) {
					throw new Exception("超出处理范围");
				}
				for (int k = 0; k < level - 1; k++) {
					tem.add(null);
				}
			}
			tem.add(i_list.get(j).v_value);
			myDataList.add(tem);

		}
		return myDataList;
	}
	
	public static void main(String[] args) throws Exception {
		List<List<String>> data = new MyExcel2007ForPaging_high("d:/2.xlsx", 1, 900).getMyDataList();
		
	}

}
