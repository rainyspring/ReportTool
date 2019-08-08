package com.fulong.utils.v2.dto;

import java.util.List;

/**
 * 单据类数据接收结构
 * @author jx
 *
 * @param <M>单头
 * @param <L>明细
 */
public  class Bill<M,L> {
	public M mainFields;
	public List<L> rows;
	
	public M getMainFields() {
		return mainFields;
	}
	public void setMainFields(M mainFields) {
		this.mainFields = mainFields;
	}
	public List<L> getRows() {
		return rows;
	}
	public void setRows(List<L> rows) {
		this.rows = rows;
	}
	
	
}
