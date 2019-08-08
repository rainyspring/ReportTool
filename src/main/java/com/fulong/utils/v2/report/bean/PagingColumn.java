package com.fulong.utils.v2.report.bean;

/**
 * @description:TODO(分页属性（这里将分页属性和同胞属性统称为 分页属性，不再将分页属性和同胞属性单独区分）)
 * @author: jx
 * @date: 2019年1月31日 上午10:39:50
 */
public class PagingColumn {

	/**
	 * @note 作为分页属性的唯一性标识,也是初始的单元格内的值
	 */
	private String key;
	/**
	 * @note 值
	 */
	private String value;
	/**
	 * @note 是否放入wheresql中
	 */
	private boolean addingWhereSQLByLayer = true;
	/**
	 * @note 衍生的分页属性:并非模板中直接体现的，而是从某些复杂分页属性中提取的部分片段
	 *       <p>
	 *       形如：分页属性为concat('{w.code}','{pd.name}'),系统会自动衍生出{w.code}
	 *       和{pd.name}这两个衍生的分页属性
	 *       </p>
	 */
	private PagingColumnType pagingColumnType = PagingColumnType.common;

	/**
	 * 
	 * @author: jx @Title: PagingColumn @Description:
	 * TODO(这里用一句话描述这个方法的作用) @param: @param key @param: @param
	 * addingWhereSQLByLayer @param: @param derivative @throws
	 */
	public PagingColumn(String key, boolean addingWhereSQLByLayer, PagingColumnType pagingColumnType) {
		this.key = key;
		this.addingWhereSQLByLayer = addingWhereSQLByLayer;
		this.pagingColumnType = pagingColumnType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PagingColumn other = (PagingColumn) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PagingColumn [key=" + key + ", value=" + value + ", addingWhereSQLByLayer=" + addingWhereSQLByLayer
				+ ", pagingColumnType=" + pagingColumnType + "]";
	}

	/**
	 * 
	 * @author: jx   
	 * @Title: isCommon   
	 * @Description: TODO(是否为普通属性)   
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	public boolean isCommon() {
		return PagingColumnType.common.compareTo(this.pagingColumnType)==0;
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

	public boolean isAddingWhereSQLByLayer() {
		return addingWhereSQLByLayer;
	}

	public void setAddingWhereSQLByLayer(boolean addingWhereSQLByLayer) {
		this.addingWhereSQLByLayer = addingWhereSQLByLayer;
	}

	public PagingColumnType getPagingColumnType() {
		return pagingColumnType;
	}

	public void setPagingColumnType(PagingColumnType pagingColumnType) {
		this.pagingColumnType = pagingColumnType;
	}

}
