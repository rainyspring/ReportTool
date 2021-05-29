package xu.jiang.report.bean;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;

import xu.jiang.report.Param;

/**
 * 合并层
 * 带有合并单元格的循环层
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:31:43
 */
public class MergingLayer extends BaseLayer{
	private boolean isGroup = false;
	private List<LoopLayer> layers ;
	
	public MergingLayer(int index,List<LoopLayer> layers, Param param) {
		super(index, param);
		this.layers = layers;
		init();
	}

	
	private void init() {
		if(layers==null||layers.size()<=0){
			return;
		}
		for(LoopLayer l:layers){
			if(l.isGroup()){
				this.remarkGroup();
				break;
			}
		}
	}


	@Override
	public void receiveData(Session session,Map<String, String> pagingParams) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int writeData(int startRowIndex, Sheet sheet) {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * 标记为组内层
	 * @return
	 */
	@Override
	public MergingLayer remarkGroup(){
		this.isGroup = true;
		return this;
	}
	/**
	 * 该层是否为组内层
	 * @return
	 */
	@Override
	public boolean isGroup(){
		return this.isGroup;
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
}
