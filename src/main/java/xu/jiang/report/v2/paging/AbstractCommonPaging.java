package xu.jiang.report.v2.paging;

/**
 * 
 * @author jx
 *
 * @param <T>
 */
public abstract class AbstractCommonPaging<T> implements Paging<T>{

	/**
	 * 计算总页数
	 */
	@Override
	public final long getSumOfPages() {
		long sum = (long) this.getTotal();
		return (long) (sum / (this.getSpan() + 0.1)) + 1;// 总页数
	}
	/**
	 * 每页的跨度,默认为系统跨度
	 * 不建议修改
	 */
	@Override
	public int getSpan() {
		// TODO Auto-generated method stub
		return Paging.DEFAULT_SPAN;
	}
	/**
	 * 总记录数
	 * @return
	 */
	public abstract long getTotal();
}
