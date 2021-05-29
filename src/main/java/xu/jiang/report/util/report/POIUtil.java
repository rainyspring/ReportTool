package xu.jiang.report.util.report;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xu.jiang.report.util.BooleanMsg;
import xu.jiang.report.v2.report.bean.RangeCell;

/**
 * 读取Excel中单元格的值
 * 
 * @author 姜旭
 * 
 */
public class POIUtil {
	private static int regexIndex_sheetName = 1;
	
	
	
	public static String getCellValue(Cell cell) {

		if (cell == null) {
			return null;
		}
		String cellvalue = "";
		
		/*
		 * 方法1 ：太粗暴 
		RichTextString richTextString = cell.getRichStringCellValue();
		if(richTextString!=null){
			cellvalue = richTextString.getString();
		}*/
		
		
		
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING: {
			cellvalue = cell.getRichStringCellValue().getString();
			break;
		}
		case Cell.CELL_TYPE_NUMERIC: {
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				cellvalue = format.format(cell.getDateCellValue());
			} else {

				double d = cell.getNumericCellValue();
				double value = d - (int) d;
				if (value <= Double.MIN_VALUE) {
					// belong to int type
					cellvalue = Integer.toString((int) d);

				} else if (value >= 1) {
					// belong to long type
					cellvalue = Long.toString(new Double(d).longValue());
				} else {
					cellvalue = Double.toString(d);
				}

			}
			break;
		}
		case Cell.CELL_TYPE_FORMULA: {// 公式
			// Date
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				if (cell.getDateCellValue() == null || "".equals(cell.getDateCellValue())) {
					return null;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				cellvalue = sdf.format(cell.getDateCellValue());
			} else {
				
				if (cell.getDateCellValue() == null || "".equals(cell.getDateCellValue())) {
					return "";
				}
				FormulaEvaluator eval = getFormulaEvaluatorByCell(cell);
				cellvalue = String.valueOf(eval.evaluateInCell(cell));
				
			}

			break;
		}
		default:
			cellvalue = "";

		}
		return cellvalue.trim();
	}
	/**
	 * 
	 * @author jx
	 * @Description: TODO(获取计算公式计算器)
	 * @param cell
	 * @return
	 */
	public static FormulaEvaluator getFormulaEvaluatorByCell(Cell cell){
		Workbook wb = cell.getSheet().getWorkbook();
		FormulaEvaluator eval = null;
		if (wb instanceof XSSFWorkbook) {
			eval = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
		} else if (wb instanceof Workbook) {
			eval = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
		}
		
		return eval;
	}

	/**
	 * Sheet复制
	 * 
	 * @param fromSheet
	 * @param toSheet
	 * @param copyValueFlag
	 */
	public static void copySheet(Sheet fromSheet, Sheet toSheet, boolean copyValueFlag) {
		// 合并区域处理
		copyMergerRegion(fromSheet, toSheet);
		for (Iterator<Row> rowIt = fromSheet.rowIterator(); rowIt.hasNext();) {
			Row fromRow = rowIt.next();
			Row toRow = toSheet.createRow(fromRow.getRowNum());
			// 行复制
			copyRow(fromRow, toRow, copyValueFlag);
			// 设置行高
			toRow.setHeightInPoints(fromRow.getHeightInPoints());

		}
		// 设置默认列的宽度
		fromSheet.setDefaultColumnWidth(toSheet.getDefaultColumnWidth());

	}

	/**
	 * 行复制功能（同一个sheet内的行复制）
	 * 
	 * @param fromRow
	 * @param toRow
	 */
	public static void copyRow(Row fromRow, Row toRow, boolean copyValueFlag) {
		for (Iterator<Cell> cellIt = fromRow.cellIterator(); cellIt.hasNext();) {
			Cell fromCell = cellIt.next();
			Cell toCell = toRow.createCell(fromCell.getColumnIndex());
			copyCell(fromCell, toCell, copyValueFlag, false);

		}
	}

	/**
	 * 复制原有sheet的合并单元格到新创建的sheet
	 * 
	 * @param sheetCreat
	 *            新创建sheet
	 * @param sheet
	 *            原有的sheet
	 */
	private static void copyMergerRegion(Sheet fromSheet, Sheet toSheet) {
		for (CellRangeAddress a : fromSheet.getMergedRegions()) {
			toSheet.addMergedRegion(a);

			/*
			 * //复制合并单元格的宽度,没啥效果 int startIndex = a.getFirstColumn(); int
			 * lastIndex = a.getLastColumn(); for(int
			 * index=startIndex;index<=lastIndex;index++){
			 * toSheet.setColumnWidth(index, fromSheet.getColumnWidth(index)); }
			 */
		}
	}

	/**
	 * 复制单元格(同sheet内的单元格复制)
	 * 
	 * @param srcCell
	 * @param distCell
	 * @param copyValueFlag
	 *            {true则连同cell的内容一起复制}
	 * @param deleteSrcCellValue
	 *            是否清空srcCell中的值
	 */
	public static void copyCell(Cell srcCell, Cell distCell, boolean copyValueFlag, boolean deleteSrcCellValue) {
		Sheet fromSheet = srcCell.getSheet();
		Sheet toSheet = distCell.getSheet();

		//BooleanMsg<RangeCell> src_status = MergingCellUtil.isMergingCell(srcCell);
		/*
		 * srcCell不是合并单元格 或是合并的头，都要复制样式(废弃)
		 * 应该所有的都要复制样式，
		 * 因为合并单元格的样式是由内部单元格样式拼凑而成的，尤其是边框！！
		 * 颜色是安装第一个单元格显示的，至于都复制样式后，颜色问题是否会异常  还未测试
		 */
		//if (!src_status.isOk() || MergingCellUtil.isHeaderOfMergingCell(srcCell, src_status.otherObject.range)) {

			CellStyle style = srcCell.getCellStyle();
			if (style == null) {
	
				style = fromSheet.getWorkbook().createCellStyle();
				// copyCellStyle(srcStyle, newstyle);
			}
	
			distCell.setCellStyle(style);	
		//}

		// 样式

		// 复制单元格的宽度
		toSheet.setColumnWidth(distCell.getColumnIndex(), fromSheet.getColumnWidth(srcCell.getColumnIndex()));

		/*
		 * 如果目标单元格是合并单元格，单不是头时，无需复制值
		 */
		BooleanMsg<RangeCell> to_status = MergingCellUtil.isMergingCell(distCell);
		if (to_status.isOk() && !MergingCellUtil.isHeaderOfMergingCell(distCell, to_status.otherObject.range)) {

			return;
		}

		// 评论
		if (srcCell.getCellComment() != null) {
			distCell.setCellComment(srcCell.getCellComment());
		}
		// 不同数据类型处理
		int srcCellType = srcCell.getCellType();
		// distCell.setCellType(srcCellType);
		if (!copyValueFlag) {
			return;
		}
		if (srcCellType == Cell.CELL_TYPE_NUMERIC) {
			if (HSSFDateUtil.isCellDateFormatted(srcCell)) {
				distCell.setCellValue(srcCell.getDateCellValue());
			} else {
				distCell.setCellValue(srcCell.getNumericCellValue());
			}
		} else if (srcCellType == Cell.CELL_TYPE_STRING) {
			distCell.setCellValue(srcCell.getRichStringCellValue());

		} else if (srcCellType == Cell.CELL_TYPE_BLANK) {
			// nothing21
		} else if (srcCellType == Cell.CELL_TYPE_BOOLEAN) {
			distCell.setCellValue(srcCell.getBooleanCellValue());
		} else if (srcCellType == Cell.CELL_TYPE_ERROR) {
			distCell.setCellErrorValue(srcCell.getErrorCellValue());
		} else if (srcCellType == Cell.CELL_TYPE_FORMULA) {
			distCell.setCellFormula(srcCell.getCellFormula());
		} else { // nothing29
		}
		if (deleteSrcCellValue) {
			srcCell.setCellValue("");
		}

	}

	/**
	 * 复制一个单元格样式到目的单元格样式
	 * 
	 * @param fromStyle
	 * @param toStyle
	 */
	public static void copyCellStyle(CellStyle fromStyle, CellStyle toStyle) {

		toStyle.setAlignment(fromStyle.getAlignmentEnum());
		// 边框和边框颜色
		toStyle.setBorderBottom(fromStyle.getBorderBottomEnum());
		toStyle.setBorderLeft(fromStyle.getBorderLeftEnum());
		toStyle.setBorderRight(fromStyle.getBorderRightEnum());
		toStyle.setBorderTop(fromStyle.getBorderTopEnum());
		toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
		toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
		toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
		toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());

		// 背景和前景
		toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
		toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());

		toStyle.setDataFormat(fromStyle.getDataFormat());
		toStyle.setFillPattern(fromStyle.getFillPatternEnum());
		// toStyle.setFont(fromStyle.getFont(null));
		toStyle.setHidden(fromStyle.getHidden());
		toStyle.setIndention(fromStyle.getIndention());// 首行缩进
		toStyle.setLocked(fromStyle.getLocked());
		toStyle.setRotation(fromStyle.getRotation());// 旋转
		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignmentEnum());
		toStyle.setWrapText(fromStyle.getWrapText());

	}

	/**
	 * 以rowIndex为起点向下插入num空白行，
	 * 
	 * @param rowIndex
	 * @param num
	 */
	public static void insertBlankRowsAfterRowIndex(Sheet s, int rowIndex, int num) {
		if (s == null) {
			return;
		}
		num = num < 0 ? 0 : num;
		s.shiftRows(rowIndex + 1, s.getLastRowNum(), num, true, false);

	}

	/**
	 * 以rowIndex为起点向下插入num行，自带rowIndex行样式
	 * 
	 * @param rowIndex
	 * @param num
	 * @param copyValue
	 *            是否复制rowIndex行的值
	 */
	public static void insertRowsAfterRowIndex(Sheet s, int rowIndex, int num, boolean copyValue) {
		
		num = num < 0 ? 0 : num;
		int firstMovedIndex = rowIndex + 1;
		/*
		 * 如果当前插入行是最后一行就不用shiftRows 因为会报错，这个函数是插入函数，只能用createRow,
		 * 而createRow在后边会调用，这里无需过多操作
		 */
		if (firstMovedIndex <= s.getLastRowNum()) {

			s.shiftRows(firstMovedIndex, s.getLastRowNum(), num, true, false);

		}

		Row startRow = s.getRow(rowIndex);
		for (int i = 0; i < num; i++) {
			Row copyRow = POIUtil.getOrCreateRow(s, rowIndex + 1 + i);
			POIUtil.copyRow(startRow, copyRow, copyValue);
		}

	}

	/**
	 * 在location位置插入值v，其他向后平移 如果遇到合并单元,则保留样式的往后推
	 * 注意：location位置插入的单元格和location位置本身单元格样式完全相同
	 * 
	 * @param location
	 * @param rowSpan
	 *            以location为准向下跨行 除非想让更多的行都随location平移，需要设这个值外，其他的情况都用1即可
	 *            而且该函数自动识别location是否是合并单元格， 如果是，会自动将rowspan变为合并单元格的rowSpan
	 * @param v
	 * @return
	 */
	private static void insertCellAndPutValue(Cell location, int rowSpan, String v) {
		if (location == null) {
			return;
		}

		BooleanMsg<RangeCell> state = MergingCellUtil.isMergingCell(location);

		// 默认移动的步调
		rowSpan = rowSpan <= 0 ? 1 : rowSpan;// 需要移动的行数
		int moveColStep = 1;// 需要移动的列数
		Cell headerCell = location;// 最后v要填写的位置
		// Sheet sheet = headerCell.getSheet();
		if (state.isOk()) {// 说明location处在合并单元格中，找到头

			headerCell = state.otherObject.headerCell;
			int rowSpan_mergingCell = state.otherObject.getRowSpan();
			rowSpan = rowSpan_mergingCell > rowSpan ? rowSpan_mergingCell : rowSpan;

			moveColStep = state.otherObject.getColSpan();
		}
		// 平移
		movingOfHorizontal(headerCell, rowSpan, moveColStep, false, false);

		headerCell.setCellValue(v);

	}

	/**
	 * 在location位置插入等样式单元格，并复制其值
	 * 
	 * @param location
	 * @param rowSpan
	 *            从location开始向下计算，涉及的跨行数（显然 这里的跨行必须是连续的）
	 */
	public static void insertAndCopyValue(Cell location, int rowSpan, int moveColStep) {
		if (location == null) {
			return;
		}

		BooleanMsg<RangeCell> state = MergingCellUtil.isMergingCell(location);

		// 默认移动的步调
		rowSpan = rowSpan <= 0 ? 1 : rowSpan;// 需要移动的行数
		moveColStep = moveColStep < 1 ? 1 : moveColStep;// 需要移动的列数
		Cell headerCell = location;// 最后v要填写的位置
		// Sheet sheet = headerCell.getSheet();
		if (state.isOk()) {// 说明location处在合并单元格中，找到头
			headerCell = state.otherObject.headerCell;
			int rowSpan_mergingCell = state.otherObject.getRowSpan();
			rowSpan = rowSpan_mergingCell > rowSpan ? rowSpan_mergingCell : rowSpan;

			moveColStep = state.otherObject.getColSpan();
		}
		// 平移
		movingOfHorizontal(headerCell, rowSpan, moveColStep, false, true);

	}

	/**
	 * 在location位置插入值String，其他向后平移 如果遇到合并单元,则保留样式的往后推
	 * 注意：location位置插入的单元格和location位置本身单元格样式完全相同
	 * 
	 * 适合于单行的平移（单行也可以是合并单元格的单行）
	 * 
	 * @param location
	 * @param v
	 * @return
	 */
	public static void insertCellAndPutValue(Cell location, String v) {
		insertCellAndPutValue(location, 1, v);

	}

	/**
	 * 在location位置插入多个值List<Object>，其他向后平移 如果遇到合并单元,则保留样式的往后推
	 * 注意：location位置插入的单元格和location位置本身单元格样式完全相同
	 * 
	 * 适合于单行的平移（单行也可以是合并单元格的单行）
	 * 
	 * @param location
	 * @param v
	 * @return
	 */
	public static void insertCellAndPutValue(Cell location, List<? extends Object> olist) {

		// 列号
		int j = location.getColumnIndex();
		// 处理本行的值
		int last = olist.size() - 1;
		// 从最后一个开始添加，无需推
		String v = olist.get(last) == null ? "" : olist.get(last).toString();
		Cell firstCell = POIUtil.getOrCreateCell(location.getRow(), j);
		firstCell.setCellValue(v);

		// 因为自身也算作一个，故少推一个,即第0个不推
		for (int k = last-1; k >= 0; k--) {
			String m = olist.get(k) == null ? "" : olist.get(k).toString();

			//Cell flag = POIUtil.getOrCreateCell(location.getRow(), j);
			// 每次只能推一个单元格
			POIUtil.insertCellAndPutValue(firstCell, m);
		}

	}

	/**
	 * 从指定单元格位置开始平移move4ColStep个单元格 注意：
	 * 平移范围定义：从header开始，行跨rowSpan，列移动move4ColStep，所扫过的区域
	 * 1如果范围内存在合并单元格，且完整包含它，要保持合并单元格样式不变平移 2 如果出现合并单元格并未全部在范围内，检查其头是否在区域内， 2.1
	 * 如果头在区域内，则保持合并单元格样式不变平移 2.2 否则 忽略，因为合并单元格的样式和值应该由头来决定
	 * 
	 * 新值插入headerCell后，原来位置保留原始样式
	 * 
	 * @param headerCell
	 *            必须是单元格的头
	 * @param rowSpan
	 *            涉及平移的行的跨度（从headerCell的行号开始计算，包括当前行）
	 * @param move4ColStep
	 *            平移多少列
	 * @param removeHeaderCellRange
	 *            如果headerCell是合并单元格，在平移后，是否删除它的合并状态
	 * @param saveStepHistory
	 *            是否保留移动过程的足迹 ，默认false
	 */
	public static void movingOfHorizontal(Cell headerCell, int rowSpan, int move4ColStep, boolean removeHeaderCellRange,
			boolean removeStepHistory) {
		Sheet sheet = headerCell.getSheet();
		/*
		 * 先划定待移动的区域的范围
		 */
		// 当前单元格的位置
		int index_startR = headerCell.getRowIndex();
		int index_startC = headerCell.getColumnIndex();
		// 待移动的区域是个方阵，方阵的最后一列定义为indexCell所在行的末尾列
		int index_lastC = headerCell.getRow().getLastCellNum();
		// int index_lastC = headerCell.getRow().getPhysicalNumberOfCells();
		int index_lastR = index_startR + rowSpan - 1;

		/*
		 * 先将要新增的列追加的末尾
		 */
		for (int i = index_startR; i < index_lastR; i++) {// 遍历每一行，逐行新增单元格
			Row currentR = sheet.getRow(i);
			for (int j = 0; j < move4ColStep; j++) {
				getOrCreateCell(currentR, index_lastR + 1 + j);
			}
		}

		/*
		 * 保存需要平移的合并单元格
		 */
		Set<CellRangeAddress> ranges = new HashSet<CellRangeAddress>();

		for (int i = index_startR; i <= index_lastR; i++) {// 遍历每个合并单元格

			Row r = POIUtil.getOrCreateRow(sheet, i);

			for (int j = index_startC; j <= index_lastC; j++) {

				Cell c = POIUtil.getOrCreateCell(r, j);

				BooleanMsg<RangeCell> status = MergingCellUtil.isMergingCell(c);
				if (status.isOk()) {
					// 收集需要平移的合并单元格，且只把头在区域内的平移
					Cell temp_header = status.otherObject.headerCell;
					if (index_startR <= temp_header.getRowIndex() && temp_header.getRowIndex() <= index_lastR
							&& index_startC <= temp_header.getColumnIndex()
							&& temp_header.getColumnIndex() <= index_lastC) {

						ranges.add(status.otherObject.range);
					}
				}
			}
		}
		/*
		 * 开始使合并单元格平移：由于没有更新的方法，只能先删除，在追加
		 */
		for (CellRangeAddress r : ranges) {
			/*
			 * headerCell所在的合并单元格就不删了， 那是新值要插入的地方，默认采用原始样式
			 */
			if (!r.isInRange(headerCell.getRowIndex(), headerCell.getColumnIndex())) {
				MergingCellUtil.removeMergedRegion(sheet, r.getFirstRow(), r.getFirstColumn());
			}

		}
		/*
		 * 单元格复制平移 注意一定要倒着复制啊，呵呵
		 */
		for (int i = index_startR; i <= index_lastR; i++) {// 遍历每一行，将待平移区域逐个单元格复制
			Row currentR = sheet.getRow(i);
			for (int j = index_lastC; j >= index_startC; j--) {
				Cell srcCell = getOrCreateCell(currentR, j);
				Cell distCell = getOrCreateCell(currentR, j + move4ColStep);
				copyCell(srcCell, distCell, true, !removeStepHistory);

			}
		}

		// 合并单元格的移动就是删除后，重新创建
		for (CellRangeAddress r : ranges) {
			CellRangeAddress addMergingCell = new CellRangeAddress(r.getFirstRow(), r.getLastRow(),
					r.getFirstColumn() + move4ColStep, r.getLastColumn() + move4ColStep);
			sheet.addMergedRegion(addMergingCell);
		}

		if (removeHeaderCellRange) {
			BooleanMsg<RangeCell> range = MergingCellUtil.isMergingCell(headerCell);
			if (range.isOk()) {
				MergingCellUtil.removeMergedRegion(sheet, headerCell.getRowIndex(), headerCell.getColumnIndex());

			}
		}
	}

	/**
	 * 获取单元格，没有则创建
	 * 
	 * @param r
	 * @param index
	 * @return
	 */
	public static Cell getOrCreateCell(Row r, int index) {
		Cell c = r.getCell(index);
		if (c == null) {
			c = r.createCell(index);
			c.setCellValue("");
		}
		return c;
	}

	/**
	 * 获取同行的下一个单元格，没有则创建 这个单元格可以是合并单元格，也可能不是 当然，下一个单元格同理
	 * 
	 * @param r
	 * @param index
	 * @return
	 */
	public static Cell getOrCreateNextCell(Cell location) {
		if (location == null) {
			return null;
		}

		// Sheet sheet = location.getSheet();
		BooleanMsg<RangeCell> status = MergingCellUtil.isMergingCell(location);

		/*
		 * 获取当前单元格的头
		 */
		Cell headerCell = status.otherObject.headerCell;
		/*
		 * 默认下一个单元格的位置
		 */
		int nextR = headerCell.getRowIndex();
		int nextC = headerCell.getColumnIndex() + 1;
		if (status.isOk()) {

			CellRangeAddress range = status.otherObject.range;
			nextC = range.getLastColumn() + 1;

		}
		// 找到下一个单元格,当然不一定是头哦
		Cell nextCell = POIUtil.getOrCreateCell(location.getSheet(), nextR, nextC);
		return nextCell;

	}

	/**
	 * 获取单元格，没有则创建
	 * 
	 * @param r
	 * @param index
	 * @return
	 */
	public static Cell getOrCreateCell(Sheet s, int rowIndex, int colindex) {

		Row r = POIUtil.getOrCreateRow(s, rowIndex);
		return POIUtil.getOrCreateCell(r, colindex);
	}

	/**
	 * 获取row，没有则创建
	 * 
	 * @param r
	 * @param index
	 * @return
	 */
	public static Row getOrCreateRow(Sheet s, int index) {
		Row r = s.getRow(index);
		if (r == null) {
			return s.createRow(index);
		}
		return r;
	}

	/**
	 * 获取一个名为sheetName的sheet，不存在则创建
	 * 
	 * @param wk
	 * @param sheetName
	 * @return
	 */
	public static Sheet getOrCreateSheet(Workbook wk, String sheetName) {
		Sheet s = wk.getSheet(sheetName);
		if (s == null) {
			s = wk.createSheet();
			// 改名
			wk.setSheetName(wk.getSheetIndex(s.getSheetName()), sheetName);
		}
		return s;
	}

	/**
	 * sheet改名
	 * 
	 * @param sheet
	 * @param name
	 */
	public static Sheet renameSheet(Sheet sheet, String name) {
		Workbook wk = sheet.getWorkbook();
		if (wk.getSheet(name) == null) {
			wk.setSheetName(wk.getSheetIndex(sheet), name);
		} else {
			wk.setSheetName(wk.getSheetIndex(sheet), name + regexIndex_sheetName++);

		}
		return sheet;
	}

}
