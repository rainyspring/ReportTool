package xu.jiang.report.v2.report.bean;

import java.util.Map;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;

import xu.jiang.report.v2.report.Param;

/**
 * 组：带有循环层1个和小计层N个实现阶梯型展现
 * 为了解决分组小计的功能报表
 * 组内层不能含有合并单元格
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:32:57
 */
public class GroupLayer implements Layer{

	/**
	 * 
	 * @param index 第一个层的行号
	 * @param layers 第一个是循环层，其他的是小计层
	 * @param param
	 */
	public GroupLayer(int index,TreeSet<BaseLayer> layers , Param param) {
		
	}

	@Override
	public void receiveData(Session session,Map<String, String> pagingParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int writeData(int startRowIndex, Sheet sheet) {
		// TODO Auto-generated method stub
		return startRowIndex;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int compareTo(Layer o) {
		if(this.getIndex()>o.getIndex()){
			return 1;
		}else if(this.getIndex()==o.getIndex()){
			return 0;
		}
		return -1;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	
}
