package xu.jiang.report.v2.ui;
/**
 * 
 * @author jx
 *
 */
public  class UIDatagrid {
	private String id;
	/*
	 * 用于view层显示的标记
	 */
	private String flag;
	/*
	 * 用于view层提交Controller层的对指定行操作信号（C、R、U、D）
	 */
	private String crud;
	
	private boolean selected = false;//用于View层显示是否勾选
	
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getCrud() {
		return crud;
	}
	public void setCrud(String crud) {
		this.crud = crud;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
}
