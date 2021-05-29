package xu.jiang.report.v2.ui;
/**
 * 
 * @see
 * @author jx
 * @date 2017年11月30日 下午2:20:30
 */
public class UITreegrid {
 //{"id":1,"areaName":"主表区域","areaType":"表格","startPos":"A1","endPos":"B2","progress":60,"iconCls":"","state":"closed"},
	
	private String id;
	private String text;
	private String _parentId;
	private String iconCls = "icon-none";
	private String state = "closed";
	
	
	
	public UITreegrid() {
		super();
	}

	public UITreegrid(String id, String text,String _parentId) {
		super();
		this.id = id;
		this.text = text;
		this._parentId = _parentId;
	}
	
	public UITreegrid(String id, String text,String _parentId, boolean open) {
		super();
		this.id = id;
		this.text = text;
		this._parentId = _parentId;
		if(open){
			this.state = "open";
		}
	}
	
	/**
	 * 自定义 打开状态 方法
	 * @return
	 */
	public UITreegrid open(){
		this.setState("open");
		return this;
	}
	/**
	 * 自定义 关闭状态 方法
	 * @return
	 */
	public UITreegrid close(){
		this.setState("closed");
		return this;
	}
	
	
	
	public String getIconCls() {
		return iconCls;
	}
	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String get_parentId() {
		return _parentId;
	}

	public void set_parentId(String _parentId) {
		this._parentId = _parentId;
	}
	
}
