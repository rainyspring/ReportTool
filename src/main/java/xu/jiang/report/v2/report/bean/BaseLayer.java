package xu.jiang.report.v2.report.bean;

import xu.jiang.report.Param;

public abstract class BaseLayer implements Layer {
	private final int index;//层的序号
	public Param param;
	
	
	public BaseLayer(int index,Param param) {
		this.index = index;
		this.param = param;

	}

	/**
	 * 标记为组内层
	 */
	public abstract Layer remarkGroup();
	/**
	 * 该层是否为组内层
	 */
	public abstract boolean isGroup();
	/**
	 * 层索引
	 */
	@Override
	public int getIndex(){
		return this.index;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getIndex();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BaseLayer other = (BaseLayer) obj;
		if (this.getIndex() != other.getIndex()) {
			return false;
		}
		
		return true;
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

}
