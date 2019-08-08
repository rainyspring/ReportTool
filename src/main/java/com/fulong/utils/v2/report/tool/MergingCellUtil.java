package com.fulong.utils.v2.report.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.fulong.utils.v2.report.bean.RangeCell;
import com.fulong.utils.v2.tool.BooleanMsg;

/**
 * 合并单元格处理类
 * 
 * @see
 * @author jx
 * @date 2017年4月25日 下午4:46:53
 */
public class MergingCellUtil {

	/**
	 * 判断单元格是否为合并单元格
	 * 
	 * @param cell
	 *            需要判断的单元格
	 * @param sheet
	 *            sheet
	 * @return
	 */
	public static BooleanMsg<RangeCell> isMergingCell(Cell cell) {
		if (cell == null) {
			return new BooleanMsg<RangeCell>("this cell is null");
		}

		int firstC = 0;
		int lastC = 0;
		int firstR = 0;
		int lastR = 0;
		
		Sheet s = cell.getSheet();
		int size = s.getNumMergedRegions();
		if(size<=0){
			return new BooleanMsg<RangeCell>("this cell is not mergingCell").putOther(new RangeCell(null, cell));
		}
		
		Sheet sheet = cell.getSheet();
		for (int i=0;i<size;i++) {
			CellRangeAddress mergingCell = sheet.getMergedRegion(i);
			// 获得合并单元格的起始行, 结束行, 起始列, 结束列
			firstC = mergingCell.getFirstColumn();
			lastC = mergingCell.getLastColumn();
			firstR = mergingCell.getFirstRow();
			lastR = mergingCell.getLastRow();
			if (cell.getColumnIndex() <= lastC && cell.getColumnIndex() >= firstC) {
				if (cell.getRowIndex() <= lastR && cell.getRowIndex() >= firstR) {

					Cell hearCell = POIUtil.getOrCreateCell(
							POIUtil.getOrCreateRow(cell.getSheet(), mergingCell.getFirstRow()),
							mergingCell.getFirstColumn());

					return new BooleanMsg<RangeCell>(null).putOther(new RangeCell(mergingCell, hearCell));
				}
			}
		}

		return new BooleanMsg<RangeCell>("this cell is not mergingCell").putOther(new RangeCell(null, cell));
	}

	/**
	 * 移除(range_startR,range_startC)所处在的合并单元格
	 * 警告：不能再遍历合并单元格时调用该方法！！
	 * @param sheet
	 * @param range_startR
	 * @param range_startC
	 * return CellRangeAddress
	 */
	public static void removeMergedRegion(Sheet sheet, int range_startR, int range_startC) {
		int sheetMergeCount = sheet.getNumMergedRegions();// 获取所有的单元格
		int index = -1;// 用于保存要移除的那个单元格序号
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress ca = sheet.getMergedRegion(i); // 获取第i个单元格
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if (range_startR >= firstRow && range_startR <= lastRow) {
				if (range_startC >= firstColumn && range_startC <= lastColumn) {
					index = i;
				}
			}
		}
		if(index!=-1){
			sheet.removeMergedRegion(index);// 移除合并单元格
		}

	}

	/**
	 * 判断cell是否属于这个合并单元格
	 * 
	 * @param cell
	 * @param mergingCell
	 * @return
	 */
	public static boolean isInRangeOfMergingCell(Cell cell, CellRangeAddress mergingCell) {
		if (cell == null) {
			return false;
		}
		// 获得合并单元格的起始行, 结束行, 起始列, 结束列
		int firstC = mergingCell.getFirstColumn();
		int lastC = mergingCell.getLastColumn();
		int firstR = mergingCell.getFirstRow();
		int lastR = mergingCell.getLastRow();

		if (cell.getColumnIndex() <= lastC && cell.getColumnIndex() >= firstC) {
			if (cell.getRowIndex() <= lastR && cell.getRowIndex() >= firstR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否是合并单元格的第一个单元格 合并单元格的第一个单元格具有特殊的意义 它即是合并单元格样式和值
	 * 
	 * @param cell
	 * @param mergingCell
	 * @return
	 */
	public static boolean isHeaderOfMergingCell(Cell cell, CellRangeAddress mergingCell) {
		if (cell == null) {
			return false;
		}

		if (cell.getRowIndex() == mergingCell.getFirstRow() && cell.getColumnIndex() == mergingCell.getFirstColumn()) {
			return true;
		}
		return false;
	}

	/**
	 * 得到cell单元格所处合并单元格的头， 如果不是合并单元格就返回自身
	 * 
	 * @param cell
	 * @return
	 */
	public static Cell getHeaderOfMergingCell(Cell cell) {
		BooleanMsg<RangeCell> status = isMergingCell(cell);
		if (status.isOk()) {
			CellRangeAddress mergingCell = status.otherObject.range;
			return cell.getSheet().getRow(mergingCell.getFirstRow()).getCell(mergingCell.getFirstColumn());

		}
		return cell;
	}

	/**
	 * 如果C不是合并单元格，就返回c.getLastRowIndex 否则返回合并单元格的最后一行
	 * 
	 * @param c
	 * @return
	 */
	public static int getLastRowIndex4MergingCell(Cell c) {

		BooleanMsg<RangeCell> mergingCell = isMergingCell(c);
		if (mergingCell.isOk()) {
			return mergingCell.otherObject.range.getLastRow();
		}
		// TODO Auto-generated method stub
		return c.getRowIndex();
	}

	/**
	 * 在location位置插入num个空白单元格，并合并成一个合并单元格 逻辑：
	 * 找到location水平方向下一个单元格A，从A(可能也是合并单元格或普通单元格)开始往后推
	 * 
	 * @param location
	 * @param num
	 */
	public static void insertBlankAndMergeCell2Cell(Cell location, int blankCellNum) {
		if (location == null) {
			return;
		}
		Sheet sheet = location.getSheet();
		BooleanMsg<RangeCell> status = MergingCellUtil.isMergingCell(location);
		Cell headerCell = status.otherObject.headerCell;
		
		// 找到下一个单元格,当然不一定是头哦
		Cell nextCell = POIUtil.getOrCreateNextCell(location);
		BooleanMsg<RangeCell> next_status = MergingCellUtil.isMergingCell(nextCell);
		int rowSpan = 1;
		if (next_status.isOk()) {
			CellRangeAddress range = next_status.otherObject.range;
			rowSpan = range.getLastRow() - range.getFirstRow() + 1;
		}
		// 平移
		POIUtil.movingOfHorizontal(next_status.otherObject.headerCell, rowSpan, blankCellNum,true,false);

		// 更新合并单元格
		/*
		 * 默认合并单元格位置
		 */
		int firstRow = headerCell.getRowIndex();
		int firstCol = headerCell.getColumnIndex();
		int lastRow = firstRow;
		int lastCol = firstCol+blankCellNum;
		if (status.isOk()) {

			CellRangeAddress range = status.otherObject.range;
			lastRow = range.getLastRow();
			lastCol = range.getLastColumn()+blankCellNum;
			removeMergedRegion(sheet, range.getFirstRow(), range.getFirstColumn());
			
		}
		CellRangeAddress range  = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		sheet.addMergedRegion(range);
		//设置合并单元格样式
		setMergingCellStyle(sheet,range,location.getCellStyle());
		
	}
	/**
	 * 设置合并单元格样式
	 * @param range
	 * @param cellStyle
	 */
	private static void setMergingCellStyle(Sheet sheet,CellRangeAddress range, CellStyle fromCellStyle) {
		
		for(int r = range.getFirstRow();r<=range.getLastRow();r++){
			
			for(int c = range.getFirstColumn();c<=range.getLastColumn();c++){
				
				Cell toCell = POIUtil.getOrCreateCell(sheet, r, c);
				CellStyle newstyle = sheet.getWorkbook().createCellStyle();

				POIUtil.copyCellStyle(fromCellStyle, newstyle);

				// 样式
				toCell.setCellStyle(newstyle);
			}
			
		}
	}

	public static void main(String[] args) throws IOException {
		FileInputStream io = new FileInputStream(new File("d:/c.xls"));
		Workbook w = new HSSFWorkbook(io);
		Sheet s = w.getSheetAt(0);// 默认第一个sheet
		//POIUtil.movingOfHorizontal(POIUtil.getOrCreateCell(s, 7, 2), 1, 5);
		//POIUtil.movingOfHorizontal(POIUtil.getOrCreateCell(s, 8, 2), 2, 5);
		MergingCellUtil.insertBlankAndMergeCell2Cell(POIUtil.getOrCreateCell(s, 7, 6), 5);
		MergingCellUtil.insertBlankAndMergeCell2Cell(POIUtil.getOrCreateCell(s, 8, 7), 5);


		FileOutputStream out = new FileOutputStream("d:/1.xls");
		w.write(out);
		out.close();
		System.out.println("---");

	}
	/**
	 * 是否在集合中已经存在了这个合并单元格
	 * @param mergingColumns
	 * @param range
	 * @return
	 */
	public static boolean exists(Set<CellRangeAddress> mergingColumns, CellRangeAddress i) {
		for(CellRangeAddress c :mergingColumns){
			if(c.getFirstRow()==i.getFirstRow()
					&&c.getFirstColumn()==i.getFirstColumn()
					&&c.getLastColumn()==i.getLastColumn()
					&&c.getLastRow()==i.getLastRow()){
				return true;
			}
		}
		return false;
	}
}
