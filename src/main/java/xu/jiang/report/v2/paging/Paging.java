package xu.jiang.report.v2.paging;

import java.lang.reflect.InvocationTargetException;

/**
 * 数据提取器的分页类型
 * 将数据通过逻辑分页形式逐步读取数据
 * @author jx
 *
 * @param <T>数据提取器的获取的数据形式T
 */
public interface Paging<T> {
	/**
	 * 系统建议页号从startPage开始
	 */
	final static int DEFAULT_START_PAGE= 1;
	/**
	 * 系统建议的跨度
	 */
	final static int DEFAULT_SPAN = 2000;

	/**
	 * 分页获取数据集合
	 * @param page 当前页
	 * @param rows 实际单页总记录数（一般程序设计会采用span作为默认值，一切以实现类为准）
	 * @return T 每页获取的数据集合
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	T getData(int page,int rows) throws Exception;
	
	/**
	 * 总页数
	 * @return
	 */
	long getSumOfPages();
	/**
	 * 最大列数
	 * 如果等于-1 代表没有最大行限制
	 * @return int
	 */
	int getMaxCol();
	
	/**
	 * 单页跨度
	 * @return
	 */
	int getSpan();
}
