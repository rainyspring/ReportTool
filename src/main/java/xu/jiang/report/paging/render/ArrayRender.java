package xu.jiang.report.paging.render;

import java.util.List;
/**
 * 数据形式数组的Object[]为行的记录形式
 * @author jx
 *
 */
public class ArrayRender extends AbstractSimallDataRender<Object[]> {

	public ArrayRender(List<Object[]> allData, int maxCol) {
		super(allData, maxCol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void transferPerObject(List<Object> r, Object[] o) {
		// TODO Auto-generated method stub
		if(o==null||o.length<=0) {
			return;
		}
		for(Object i :o){
			r.add(i);
		}
		
		
	}
	
}
