package xu.jiang.report.v2.tool;

import org.apache.commons.lang3.StringUtils;

/**
 * 只有true、false两种状态的信息类
 * false:有错误信息
 * true:没有错误信息
 * @author jx
 *
 */
public class BooleanMsg<T> {
	private boolean ok =false;
	private StringBuffer msg = new StringBuffer() ;
	/*
	 * 存储一些额外的信息
	 */
	public T otherObject = null;
	
	public BooleanMsg(String msg) {
		if(StringUtils.isBlank(msg)){
			this.ok = true;
		}else{
			
			this.msg.append(msg);
		}
	}

	public boolean isOk() {
		return this.ok;
	}
	public String getMsg() {
		return msg.toString();
	}
	public BooleanMsg<T> appendMsg(String msg){
		this.msg.append(msg);
		if(StringUtils.isBlank(this.getMsg())){
			this.ok = true;
		}else{
			this.ok = false;
		}
		return this;
	}
	/**
	 * 放入其他实体信息
	 * @param t
	 * @return
	 */
	public BooleanMsg<T> putOther(T t){
		this.otherObject = t;
		return this;
	}

}
