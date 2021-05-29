package xu.jiang.report.dto;

import java.util.List;

import xu.jiang.report.v2.report.bean.KeyValue;

public class LayerDTO {

	private int index;//行号
	private List<KeyValue> columns;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<KeyValue> getColumns() {
		return columns;
	}

	public void setColumns(List<KeyValue> columns) {
		this.columns = columns;
	}
}
