package com.fulong.utils.v2.ui;

import java.util.List;

/**
 * EasyUI 的combobox 的控件bean
 * @author 姜旭
 * @date 2015-08-25
 */
public  class UICombobox <T extends Comparable>{
	private T id;
	private String text;
	private boolean selected;
	private String desc;
	
	
	public UICombobox() {
		
	}
	
	public UICombobox(T id, String text, boolean selected, String desc) {
		super();
		this.id = id;
		this.text = text;
		this.selected = selected;
		this.desc = desc;
	}

	/**
	 * 如果只存在一个值，设置为默认选中
	 * @param list
	 */
	public  static <E extends UICombobox> void makeFirstSelectedIfSingle(List<E> list){
		if(list==null ||list.isEmpty()) {
			return;
		}
		list.get(0).setSelected(true);
	}
	
	public T getId() {
		return id;
	}
	public void setId(T id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean getSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	@Override
	public String toString() {
		return "UICombobox [id=" + id + ", text=" + text + ", selected="
				+ selected + ", desc=" + desc + "]";
	}
	
}
