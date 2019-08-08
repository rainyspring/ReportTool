package com.fulong.utils.v2.paging.render;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fulong.utils.v2.paging.AbstractCommonPaging;
import com.fulong.utils.v2.paging.Paging;

/**
 * 小量数据封装器：一次性提供所有数据(内存消耗大)
 * 专用于List形式的简单数据集合
 * @author jx
 *
 */
public abstract class AbstractSimallDataRender<E> extends AbstractCommonPaging<List<List<Object>>>{
	private final List<E> allData;
	private final int maxCol;
	
	public  AbstractSimallDataRender(List<E> allData,int maxCol) {
		// TODO Auto-generated constructor stub
		this.allData = allData;
		this.maxCol = maxCol;
	}
	/**
	 * 提供将[用户自定义实体E]转换成[系统行记录格式R]的转化逻辑
	 * @param o
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public abstract void transferPerObject(List<Object> r,E o) throws Exception;
	
	@Override
	public final List<List<Object>> getData(int page, int rows) throws Exception {
		List<List<Object>> datagrid = new ArrayList<List<Object>>();
		int startIndex = (page-1)*rows;
		int lastIndex = startIndex+rows;
		if(lastIndex>this.allData.size()){
			lastIndex = this.allData.size();
		}
		for(int i=startIndex;i<lastIndex;i++){
			List<Object> r = new ArrayList<Object>();
			E o = this.allData.get(i);
			this.transferPerObject(r,o);
			datagrid.add(r);
		}
		
		return datagrid;
	}

	@Override
	public final int getMaxCol() {
		// TODO Auto-generated method stub
		return this.maxCol;
	}

	@Override
	public final int getSpan() {
		// TODO Auto-generated method stub
		return Paging.DEFAULT_SPAN;
	}
	@Override
	public final long getTotal() {
		// TODO Auto-generated method stub
		return this.allData.size();
	}

	
}
