package xu.jiang.report.v2.poi;

import java.io.File;
import java.io.IOException;

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

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
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
 * XSSF and SAX (Event API) basic example. See {@link XLSX2CSV} for a fuller
 * example of doing XSLX processing with the XSSF Event code.
 */
public class ExcelSaxReader4MaxRow {
	/**
	 * 代表Excel中必须有值的起始列(A、B、C....AA、AB...)
	 */
	public static final String ROW_INDEX_PREFIX = "A";
	public static final String NEW_ROW_REGEX = "^" + ROW_INDEX_PREFIX + "[0-9]+$";
	// new add
	private int maxRow = 0;// 记录总行数

	private File f = null;

	public ExcelSaxReader4MaxRow(File f) throws IOException, SAXException, OpenXML4JException {
		this.f = f;
		processFirstSheet();
	}

	/**
	 * 指定获取第一个sheet
	 * 
	 * @param filename
	 * @throws IOException
	 * @throws SAXException
	 * @throws OpenXML4JException
	 * @throws Exception
	 */
	private void processFirstSheet() throws IOException, SAXException, OpenXML4JException {
		OPCPackage pkg = OPCPackage.open(f);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		// To look up the Sheet Name / Sheet Order / rID,
		// you need to process the core Workbook stream.
		// Normally it's of the form rId# or rSheet#
		InputStream sheet2 = r.getSheet("rId1");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();// 关闭流
		pkg.close();// 关闭POI的sax流
	}

	private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new MaxRowHandler();
		parser.setContentHandler(handler);
		return parser;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private class MaxRowHandler extends DefaultHandler {

		// 前一个index
		private String formerIndex = null;

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// c => cell
			if (!"c".equals(name)) {
				//不是单元格，直接忽略
				return;
			}
			
			String index = attributes.getValue("r");
			System.out.println("@"+index);
			/*
			 * 新行判定：
			 * 1 如果formerIndex==null 代表是第一个检索到的单元格, 显然应为作为第一行；
			 * 2 其他情况，只需要检测检测行号，是否和formerIndex（上一次记录）为同一行？ 若不同，则显然 当前记录为新行开始。
			 * 若相同，则和formerIndex（上一次记录）为同一行，应该忽略。
			 */
			if (formerIndex == null||!IndexValue.isSameRow(index, formerIndex)) {

				maxRow++;
				System.out.println("--"+maxRow);
			}
			
			formerIndex = index;
		}

	}

	/**
	 * 
	 * @author: jx   
	 * @Title: getMaxRow   
	 * @Description: TODO(获取最大行数)   
	 * @note 这个值很可能因为写入Excel数据时导致明明看上去是空行，但POI在解析时也会算作有效行。
	 * 这里不做处理，原因如下：
	 * 1 是能为开发人员改bug提供一个参数；
	 * 2 提高计算行的效率；
	 * 3 是个人不希望读取“Excel数据的接口”过分依赖 这个最大值的接口，在大数据面前，应该只能作为参考值，“Excel数据的接口”应该具体问题具体分析
	 * @return      
	 * @throws
	 */
	public int getMaxRow() {
		return maxRow;
	}

	public static void main(String[] args) throws IOException, SAXException, OpenXML4JException {
		// System.out.println(System.currentTimeMillis());
		ExcelSaxReader4MaxRow reader = 
				new ExcelSaxReader4MaxRow(new File("f:/4.xlsx"));
		// System.out.println(System.currentTimeMillis());
		System.out.println(reader.maxRow);
	}
}
