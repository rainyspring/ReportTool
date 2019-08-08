package com.fulong.utils.v1.poi;
import java.util.List;

import org.hibernate.ScrollableResults;
/**
 * @author jx
 * @deprecated Use {@link com.fulong.utils.paging.Paging}
 */
@Deprecated
public interface MyPaging {
	/**
	 * 页号从startPage开始
	 */
	public final static int STARTPAGE= 1;
	public final static int SPAN = 2000;
	/**
	 * 返回值是List 数组
	 * @param page
	 * @param rows
	 * @return
	 */
	public List<Object[]> getDataByPaging4ArrList(int page,int rows);
	/**
	 * 返回值是嵌套的List
	 * @param page 页号从startPage开始
	 * @param rows
	 * @return
	 */
	public List<List<Object>> getDataByPaging4NestedList(int page,int rows);
	//public Iterator getDataByPaging4Iterater(int page,int rows);//Iterator
	/**
	 * 
	 * @param page 页号从startPage开始
	 * @param rows
	 * @return
	 */
	public ScrollableResults getDataByPaging4Scroll(int page,int rows);//
	public long getSumOfPages();
	/**
	 * 如果等于-1 代表没有最大行限制
	 * @return
	 */
	public int getMaxCol();
	
	public int getSpan();
}
