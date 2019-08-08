package com.fulong.utils.v1.poi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.ScrollableResults;

/**
 * 专用于List形式的简单数据集合
 * @author jx
 * @deprecated Use {@link com.fulong.utils.paging.render.ArrayRender}
 */
@Deprecated
public abstract class AbstractMyPaging4List<T> implements MyPaging{
	private final List<T> list;
	private final int maxCol;
	
	public AbstractMyPaging4List(List<T> list,int maxCol) {
		// TODO Auto-generated constructor stub
		this.list = list;
		this.maxCol = maxCol;
	}
	public abstract void setPerObject(List<Object> r,T o);
	
	@Deprecated
	@Override
	public List<Object[]> getDataByPaging4ArrList(int page, int rows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<Object>> getDataByPaging4NestedList(int page, int rows) {
		List<List<Object>> datagrid = new ArrayList<List<Object>>();
		int startIndex = (page-1)*rows;
		int lastIndex = startIndex+rows;
		if(lastIndex>this.list.size()){
			lastIndex = this.list.size();
		}
		for(int i=startIndex;i<lastIndex;i++){
			List<Object> r = new ArrayList<Object>();
			
			T o = this.list.get(i);
			this.setPerObject(r, o);
			
			datagrid.add(r);
		}
		
		return datagrid;
	}
	

	@Deprecated
	@Override
	public ScrollableResults getDataByPaging4Scroll(int page, int rows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSumOfPages() {
		Long sum = (long) this.list.size();
		long total = sum == null ? 0 : (long) sum;
		return (long) (total / (this.getSpan() + 0.1)) + 1;// 总页数

	}

	@Override
	public int getMaxCol() {
		// TODO Auto-generated method stub
		return this.maxCol;
	}

	@Override
	public int getSpan() {
		// TODO Auto-generated method stub
		return MyPaging.SPAN;
	}

	
}
