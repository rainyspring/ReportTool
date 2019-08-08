package com.fulong.utils.v2.report.bean;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
/**
 * 合并单元格
 * @see
 * @author jx
 * @date 2017年5月10日 下午2:43:17
 */
public class RangeCell {
	public final CellRangeAddress range;
	public final Cell headerCell ;
	
	
	public RangeCell(CellRangeAddress range, Cell headerCell) {
		this.range = range;
		this.headerCell = headerCell;
		
	}
	/**
	 * 获取行的跨度
	 * @return
	 */
	public int getRowSpan(){
		if(this.range==null) {
			return 1;//就是header的列跨度
		}
		return this.range.getLastRow() - this.range.getFirstRow() + 1;
	}
	/**
	 * 获取列的跨度
	 * @return
	 */
	public int getColSpan(){
		if(this.range==null) {
			return 1;//就是header的行跨度
		}
		return this.range.getLastColumn() - this.range.getFirstColumn() + 1;
	}
}
