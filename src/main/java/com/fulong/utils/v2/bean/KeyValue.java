package com.fulong.utils.v2.bean;
/**
 * 符合Key-value结构的实体
 * @see
 * @author jx
 * @date 2017年11月17日 下午5:02:27
 */
public class KeyValue {
	public String key;
	public String value;
	

	public KeyValue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public KeyValue(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
