package xu.jiang.report.v2.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * @author jx
 * @date 2017-11-30
 */
public class UITree{

	//tree结构必须属性
	private String id;// 树id
	private String text;// 树名称
	private String state = "closed";// 状态
	private boolean checked = false;//是否被选中
	private String iconCls ;//"icon-none"
	/**
	 * 被添加到节点的自定义属性
	 */
	private Map<String,String> attributes = new HashMap<String,String>();
	
	/**
	 * 一个节点数组声明了若干节点
	 */
	private List<UITree> children = new LinkedList<UITree>();
	
	
	
	public UITree(String id, String text) {
		super();
		this.id = id;
		this.text = text;
	}
	
	public UITree(String id, String text, boolean open) {
		super();
		this.id = id;
		this.text = text;
		if(open){
			this.state = "open";
		}
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
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<UITree> getChildren() {
		return children;
	}
	public void setChildren(List<UITree> children) {
		this.children = children;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
}
