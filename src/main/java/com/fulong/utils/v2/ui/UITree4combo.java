package com.fulong.utils.v2.ui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
/**
 * 带有复选框的一级下拉tree，没有父节点
 * @author 姜旭
 * @date 2015-08-25
 *
 */
public class UITree4combo implements RowMapper{

	//tree结构必须属性
	private String id;// 树id
	private String text;// 树名称
	private boolean checked;//是否被选中
	private String iconCls ="icon-none";

	
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		UITree4combo o  = new UITree4combo();
		o.setId(rs.getString("id"));
		o.setText(rs.getString("text"));
		return o;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}
}
