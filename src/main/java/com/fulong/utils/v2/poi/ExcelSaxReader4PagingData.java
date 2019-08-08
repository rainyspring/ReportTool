package com.fulong.utils.v2.poi;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
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
 * 目前函数又个缺陷，便是starElement函数中发现新行的策略有问题
 */
public class ExcelSaxReader4PagingData {
	/**
	 * 判断是否为行的正则
	 */
	private final Pattern rowPattern = Pattern.compile(ExcelSaxReader4MaxRow.NEW_ROW_REGEX);
	/**
	 * 存储所有行的值，内部读取后的结构化值
	 */
	private List<List<IndexValue>> dataList = null;
	/**
	 * 解析后，对外提供使用的值
	 */
	public List<List<String>> myDataList = null;

	private final int startRow;
	private final int endRow;
	private int currentRow = 0;

	private final String filename;

	/**
	 * 
	 * 创建一个新的实例 MyExcel2007ForPaging_high.
	 *
	 * @param filename
	 * @param startRow 起始行（包含起始行，从1开始）
	 * @param endRow   结束行（包含结束行）
	 * @throws Exception
	 */
	public ExcelSaxReader4PagingData(String filename, int startRow, int endRow) throws Exception {
		if (StringUtils.isBlank(filename)) {
			throw new Exception("文件名不能空");
		}
		this.filename = filename;
		this.startRow = startRow;
		this.endRow = endRow;

		/*
		 * 预设一个空间大小，如果数据非常大，避免重复
		 */
		int defaultScope = this.endRow - this.startRow + 1;
		defaultScope = defaultScope > 0 ? defaultScope : 1;

		/**
		 * POI解析后的数据结构
		 */
		this.dataList = new ArrayList<List<IndexValue>>(defaultScope);
		/**
		 * 将dataList转换后，变为我们可以使用的结构
		 */
		this.myDataList = new ArrayList<List<String>>(defaultScope);
		processFirstSheet();
		System.out.println("##success");
	}

	/**
	 * 指定获取第一个sheet
	 * 
	 * @param filename
	 * @throws IOException
	 * @throws Exception
	 */
	private void processFirstSheet() throws IOException {
		InputStream sheet1 = null;
		OPCPackage pkg = null;
		try {
			pkg = OPCPackage.open(filename);
			XSSFReader r = new XSSFReader(pkg);
			SharedStringsTable sst = r.getSharedStringsTable();

			XMLReader parser = fetchSheetParser(sst);

			// To look up the Sheet Name / Sheet Order / rID,
			// you need to process the core Workbook stream.
			// Normally it's of the form rId# or rSheet#
			sheet1 = r.getSheet("rId1");
			InputSource sheetSource = new InputSource(sheet1);
			/**
			 * XMLReader中关于parse()方法，没有特别好的中断读取的方法，只说抛异常即可 This method is synchronous: it
			 * will not return until parsing has ended. If a client application wants to
			 * terminate parsing early, it should throw an exception.
			 */
			parser.parse(sheetSource);
			pkg.flush();
			sheet1.close();
			pkg.close();
		} catch (IOException | OpenXML4JException | SAXException e) {
			if (sheet1 != null) {
				sheet1.close();
			}
			if (pkg != null) {
				pkg.flush();
				pkg.close();
			}
		}
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
		/**
		 * 当前单元格索引
		 */
		private String index = null;
		/**
		 *  前一个单元格的索引
		 */
		private String formerIndex = null;

		/**
		 * 临时存储当前行的值
		 */
		private List<IndexValue> rowData = new ArrayList<IndexValue>();

		private PagingHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		/**
		 * 每个单元格开始时的处理
		 */
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (exceedBorder()) {
				//通过报异常，强制跳出循环，提高性能。
				throw new SAXException("it jumpt out of examing in case of exceedBorder");
			}
			// c => cell
			if ("c".equals(name)) {

				index = attributes.getValue("r");

				/* 新行判定：
				 * 1 如果formerIndex==null 代表是第一个检索到的单元格, 显然应为作为第一行；
				 * 2 其他情况，只需要检测检测行号，是否和formerIndex（上一次记录）为同一行？ 若不同，则显然 当前记录为新行开始。
				 * 若相同，则和formerIndex（上一次记录）为同一行，应该忽略。
				 */
				if (formerIndex == null||!IndexValue.isSameRow(index, formerIndex)) {
					// 存储上一行数据
					if (rowData != null && isNeededRows() && !rowData.isEmpty()) {
						dataList.add(rowData);
					}

					if (isNeededRows()) {// 只为需要的行创建空间即可
						rowData = new ArrayList<IndexValue>();// 新行要先清除上一行的数据

					}
					
					// 当前行+1
					currentRow++;
					//System.out.println("----currentRow:" + currentRow);

				}
				//新行判定完毕，将当前单元格的值 存成前一个
				formerIndex = index;
				
				if (isNeededRows()) {
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
			//System.out.println("	endElement[name:"+name+"]");
			
			if (isNeededRows()) {
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

					rowData.add(new IndexValue(index, lastContents));

				}
			}

		}

		/**
		 * 目前流的方式值支持 Excel单元格是文本 格式；日期、数字、公式不支持
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			//System.out.println("		characters[ch:"+new String(ch)+"]start:"+start+"][length:"+length+"]");
			
			if (isNeededRows()) {
				lastContents += new String(ch, start, length);
			}

		}

		/**
		 * 如果文档结束后，发现读取的末尾行正处在当前行中，存储下这行 （存在这样一种情况，当待读取的末尾行正好是文档最后一行时，最后一行无法存到集合中，
		 * 因为最后一行没有下一行了，所以不会启动starElement()方法， 当然我们可以通过指定最大列来处理，但不想那么做，扩展性不好）
		 */
		@Override
		public void endDocument() throws SAXException {
			if (rowData != null && isNeededRows() && !rowData.isEmpty()) {
				dataList.add(rowData);
				System.out.println("--getData--end");
			}

		}

	}

	/**
	 * 
	 * @author jx
	 * @Description: TODO(是否为所需的行)
	 * @return
	 */
	private boolean isNeededRows() {
		if (currentRow >= startRow && currentRow <= endRow) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @author jx
	 * @Description: TODO(是否超出检索边界)
	 * @return
	 */
	private boolean exceedBorder() {
		if (currentRow > endRow) {
			return true;
		}
		return false;
	}

	

	/**
	 * 获取真实的数据（处理空格）
	 * @note 因为POI解析结果，会自动忽略空白单元格，需要我们自动补位。
	 * @return
	 * @throws Exception
	 */
	public List<List<String>> getMyDataList() throws Exception {

		if (dataList == null || dataList.isEmpty()) {
			return myDataList;
		}
		
		
		
		/*
		 * 是否是最后一行的数据
		 */
		boolean islastRow = false;
		for (int i = 0; i < dataList.size(); i++) {
			List<IndexValue> i_list = dataList.get(i);
			
			//存储变化后的每行的元素
			List<String> row = new ArrayList<String>();
			
			/*
			 * 生成每行的理论上的第一个元素,因为真实数据里，第一个元素根本不是以ROW_INDEX_PREFIX列开始。
			 * 故，如果出现行数据的起始单元格不是理论要求的第一个单元格，代表POI从ROW_INDEX_PREFIX列开始便没有获取到值，
			 * 需要我们后期不足空值位置
			 */
			//获取当前行的行号
			int currentRow = i_list.get(0).row;
			IndexValue firstTheoreticalCell = new IndexValue(ExcelSaxReader4MaxRow.ROW_INDEX_PREFIX+currentRow, null);
			
			/*
			 * 在每行数据遍历时，总是将当前值和后一个值比较
			 */
			int j = 0;
			for (; j < i_list.size() - 1; j++) {
				// 获取当前值,并存储
				IndexValue current = i_list.get(j);
				
				//只需判断数据行中第一个元素即可
				if(j==0) {
					int level4FromFirst2Current = current.getLevel(firstTheoreticalCell);
					// 将差值补充为null，
					if(level4FromFirst2Current>0) {
						//这里注意，补充null个数就是为差值本身
						row.addAll(getNullValueArray(level4FromFirst2Current));
					}
				}
				
				// 去掉空格
				String tempV = current.value != null ? current.value.trim() : current.value;
				row.add(tempV);
				
				// 预存下一个
				IndexValue next = i_list.get(j + 1);
				// 获取当前cell和下一个cell的单元格差值
				int level = next.getLevel(current);

				if (level <= 0) {
					System.err.println("---!!!到达最后一行，行号：" + (i + 1) + ";level:" + level + "[超出处理范围]");
					islastRow = true;
					break;
				}
				// 将差值补充为null，补充的null的个数为（差值-1）
				row.addAll(getNullValueArray(level-1));
			}
			/*
			 * 每行的最后一个值，留在最后插入 但最后一行除外
			 */
			if (!islastRow) {
				row.add(i_list.get(j).value);
			}
			myDataList.add(row);

		}
		dataList.clear();// 手动清除
		return myDataList;
	}
	/**
	 * 
	 * @author: jx   
	 * @Title: getNullValueArray   
	 * @Description: TODO(获取一个带有指定个数null的集合
	 * @param number
	 * @return      
	 * @throws
	 */
	private List<String> getNullValueArray(int number) {
		ArrayList<String> nullArray = new ArrayList<String>(number);
		for (int k = 1; k <= number; k++) {
			nullArray.add(null);
		}
		return nullArray;
	}
		
	public static void main(String[] args) throws Exception {
		File file = new File("f:/2.xlsx");
		if(!file.exists()) {
			
			System.out.println("no file");
			
			return ;
		}
		ExcelSaxReader4PagingData reader = new ExcelSaxReader4PagingData(file.getPath(), 1, 7);
		List<List<String>> data = reader.getMyDataList();

		for (List<String> r : data) {
			
			System.out.println(StringUtils.join(r.toArray(), ','));
		
		}
		
		

	}
}
