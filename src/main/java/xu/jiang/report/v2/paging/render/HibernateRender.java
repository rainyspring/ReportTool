package xu.jiang.report.v2.paging.render;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import xu.jiang.report.v2.paging.AbstractCommonPaging;

/**
 * 专用用hibernate分页读取数据
 * 
 * @author jx
 *
 */
public  class HibernateRender extends AbstractCommonPaging<ScrollableResults> {
	private final Session session;
	private final boolean isHQL;
	private final String sql;
	private final int maxCol;
	private long total =-1;
	/**
	 * 
	 * @param session
	 * @param sql sql or HQL
	 * @param isHQL 是否是HQL
	 * @param maxCol
	 */
	public HibernateRender(Session session,String sql,boolean isHQL,final int maxCol) {
		this.session = session;
		this.sql = sql;
		this.isHQL = isHQL;
		this.maxCol = maxCol;
	}
	/**
	 * 将从hibernate获取到的实体装好成能够处理的结果集
	 * @param r
	 * @param o
	 * @throws Exception
	 */
	public Object[] transferPerObject(Object[] o) throws Exception {
		// TODO Auto-generated method stub
		return o;
	}
	
	@Override
	public final ScrollableResults getData(int page, int rows) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		session.flush();
		session.clear();
		
		Query query= null;
		if(this.isHQL){
			query = session.createQuery(this.getSql());
		}else{
			query = session.createSQLQuery(this.getSql());
		}
		query.setFirstResult((page - 1) * rows).setMaxResults(rows);
		return query.scroll(ScrollMode.FORWARD_ONLY);
	}
	
	/**
	 * 最大列数
	 */
	@Override
	public  int getMaxCol(){
		return this.maxCol;
		
	}

	/**
	 * 分页查询的sql
	 * @return
	 */
	public  String getSql(){
		return this.sql;
		
	}
	
	@Override
	public int getSpan() {
		// TODO Auto-generated method stub
		return super.getSpan();
	}

	@Override
	public long getTotal(){
		if(this.total==-1){
			if(this.isHQL){
				if(this.sql.trim().startsWith("from")){
					this.total = (long)  session.createQuery("select count(*) "+this.getSql()).uniqueResult();
				}else{//startWith 'select'
					String tem = this.getSql().trim();
					this.total = (long) session.createQuery("select count(*) "+tem.substring(tem.indexOf("from")-1)).uniqueResult();
				}
			}else{
				BigInteger bigInteger = (BigInteger) session.createSQLQuery("select count(*) from ("+this.getSql()+") www").uniqueResult();
				this.total = bigInteger.longValue();
			}
		}
		return this.total;
		
	}
	
}
