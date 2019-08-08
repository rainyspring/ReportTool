package com.fulong.utils.v2.poi;

import java.util.regex.Pattern;

import com.fulong.utils.v2.report.tool.MathUtil;
/**
 * 
 * @description:TODO(存储单元格内的索引和值的实体)   
 * @author:  jx
 * @date:   2019年5月21日 上午9:43:46   
 *
 */
public class IndexValue {
	public static final String INDEX_REGEX = "^[A-Z]+[0-9]+$";
	
	/**
	 * 单元格的索引，形如：INDEX_REGEX
	 */
	public final String index;
	/**
	 * 单元格的值
	 */
	public final String value;
	/**
	 * v_index的列号
	 */
	public final String col;
	/**
	 * v_index的行号
	 * int 取值范围-2^31~2^31-1够用
	 */
	public final int row;
	

	public IndexValue(String v_index, String v_value) {
		if(!IndexValue.isValidIndex(v_index)) {
			throw new IllegalArgumentException("the Index ["+v_index+"] is an illegal Argument!");
		}
		
		this.index = v_index;
		this.value = v_value;
			
		this.col = IndexValue.getCol(v_index);
		this.row = IndexValue.getRow(v_index);
	}
	
	
	@Override
	public String toString() {
		return "IndexValue [v_index=" + index + ", v_value=" + value + "]";
	}
	/**
	 * 
	 * @author: jx   
	 * @Title: isSameRow   
	 * @Description: TODO(是否是相同行)   
	 * @param formerIndex
	 * @param latterIndex
	 * @return      
	 * @throws
	 */
	public boolean isSameRow(IndexValue other) {
		return other.row==this.row;
	}
	/**
	 * 计算相同行内前后两个单元格相距多少列
	 * 
	 * @param p
	 * @return
	 */
	public int getLevel(IndexValue other) {
		if(!this.isSameRow(other)) {
			throw new IllegalArgumentException("other index is in the different row  ");
		}
		/*
		 * 计算列的差值
		 */
		return MathUtil.fromNumberSystem26(this.col) - MathUtil.fromNumberSystem26(other.col);

	}
	
	/**
	 * 
	 * @author: jx   
	 * @Title: getColIndex   
	 * @Description: TODO(获取列号)   
	 * @param index
	 * @return      
	 * @throws
	 */
	public static String getCol(String index) {
		return index.replaceAll("[0-9]", "");

	}
	/**
	 * 
	 * @author: jx   
	 * @Title: getRow   
	 * @Description: TODO(获取列号)   
	 * @param index
	 * @return      
	 * @throws
	 */
	public static int getRow(String index) {
		return Integer.valueOf(index.replaceAll("[A-Z]", ""));
	}
	
	/**
	 * 
	 * @author: jx   
	 * @Title: isValidIndex   
	 * @Description: TODO(验证index是否合法)   
	 * @return      
	 * @throws
	 */
	public static boolean isValidIndex(String index) {
		
		if(index!=null && Pattern.compile(INDEX_REGEX).matcher(index).find()){
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @author: jx   
	 * @Title: isSameRow   
	 * @Description: TODO(是否是相同行)   
	 * @param formerIndex
	 * @param latterIndex
	 * @return      
	 * @throws
	 */
	public static boolean isSameRow(String formerIndex,String latterIndex) {
		
		return getRow(formerIndex)==getRow(latterIndex);
	}
	/**
	 * @deprecated
	 * @author: jx   
	 * @Title: compare   
	 * @Description: TODO(比较formerIndex和latterIndex的大小)
	 * 1 ：formerIndex>latterIndex
	 * 0：formerIndex==latterIndex
	 * -1：   formerIndex<latterIndex
	 * @param formerIndex
	 * @param latterIndex
	 * @return      
	 * @throws
	 */
	public static int compare(String formerIndex,String latterIndex) {
		if(!Pattern.compile(INDEX_REGEX).matcher(formerIndex).find()){
			throw new IllegalArgumentException("the formerIndex ["+formerIndex+"] is an illegal Argument!");
		}
		if(!Pattern.compile(INDEX_REGEX).matcher(latterIndex).find()){
			throw new IllegalArgumentException("the latterIndex ["+latterIndex+"] is an illegal Argument!");
		}
		
		String formerIndexCol = getCol(formerIndex);
		int formerIndexRow = getRow(formerIndex);
		String latterIndexCol = getCol(latterIndex);
		int latterIndexRow = getRow(latterIndex);
		
		/*
		 * 先比较行号
		 */
		if(formerIndexRow>latterIndexRow) return 1;
		if(formerIndexRow<latterIndexRow) return -1;
		
		/*
		 * 行号相同，比较列号
		 */
		if(formerIndexCol.length()>latterIndexCol.length()) {
			return 1;
		}
		if(formerIndexCol.length()<latterIndexCol.length()) {
			return -1;
		}
		if(formerIndexCol.compareTo(latterIndexCol)>0) {
			return 1;
		}
		if(formerIndexCol.compareTo(latterIndexCol)<0) {
			return -1;
		}
		return 0;
	}
	
}
