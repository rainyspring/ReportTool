package xu.jiang.report.v2.report.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xu.jiang.report.v2.report.tool.R;
import xu.jiang.report.v2.report.tool.RegexUtil;

/**
 * 
 * @description:TODO(分页属性的存储容器)
 * @author: jx
 * @date: 2019年1月31日 下午2:25:12
 *
 */
public class PagingColumnContainer {
	
	
	/**
	 * @note 分页属性集合 严禁
	 */
	private final Set<PagingColumn> pagingColumns = new HashSet<>(5);
	/**
	 * 缓存专用于 分页属性的脱掉替换符外衣{}的所有属性
	 */
	private List<String> cachePigingColumns = null;
	/**
	 * 缓存 用于层sql的属性个数
	 */
	private int cacheLengthOfWhereSQL4Layer = -1;

	/**
	 * 缓存专用于 分页属性的脱掉替换符外衣{}的拼写layer的where部分的属性
	 */
	private List<String> cachePigingColumnsOfwhereSQL4layer = null;

	/**
	 * 
	 * @author: jx @Title: add @Description: TODO(添加分页属性)
	 *          <p>
	 *          覆盖规则：普通属性(derivative==false且addingWhereSQL==true)>衍生属性(derivative==true且addingWhereSQL==true)>衍生属性(derivative==true且addingWhereSQL==false)
	 *          </p>
	 * @param column
	 * @return: void @throws
	 */
	public void add(PagingColumn column) {
		Set<PagingColumn> a = this.pagingColumns;
		if (column == null) {
			return;
		}
		/*
		 * 如果是普通分页属性，必须要保证一定覆盖既有的key 这个覆盖规则的判断必须放在第1位
		 */
		if (column.isCommon()) {
			pagingColumns.remove(column);
			pagingColumns.add(column);
			return;
		}
		/*
		 * 需要加layer的whereSQL的衍生属性，必须要保证一定覆盖既有的key 这个覆盖规则的判断必须放在第2位
		 */
		if (column.isAddingWhereSQLByLayer()) {
			pagingColumns.remove(column);
			pagingColumns.add(column);
			return;
		}
		this.pagingColumns.add(column);

	}

	/**
	 * 
	 * @author: jx @Title: add @Description:
	 *          TODO(通过字符串内容来智能放入分页属性集合中) @param: @param content @return:
	 *          void @throws
	 */
	public void add(String content) {
		List<PagingColumn> list = this.createPagingColumnsByContent(content);
		for (PagingColumn c : list) {
			this.add(c);
		}

	}

	/**
	 * 
	 * @author: jx @Title: createPagingColumnsByContent @Description:
	 *          TODO(根据内容创建分页属性) @param: @param content @param: @return @return:
	 *          List<PagingColumn> @throws
	 */
	private List<PagingColumn> createPagingColumnsByContent(String content) {
		List<PagingColumn> list = new ArrayList<>(1);
		/*
		 * 普通属性
		 */
		if (content.matches(R.SHEET_REGEX)) {
			// 拼入layer的whereSQL语句中:addingWhereSQLByLayer==true
			list.add(new PagingColumn(content, true, PagingColumnType.common));
			return list;

		}
		/*
		 * 复杂属性 ，不拼入layer的whereSQL
		 */
		list.add(new PagingColumn(content, false, PagingColumnType.complex));

		// 遇到此类函数，不要加入层的where部分
		if(content.indexOf("sum(")>=0
				||content.indexOf("SUM(")>=0
				||content.indexOf("count(")>=0
				||content.indexOf("COUNT(")>=0
				||content.indexOf("group_contat(")>=0
				||content.indexOf("GROUP_CONCAT(")>=0) {
			
			return list;
		}
		/*
		 * 衍生属性
		 */
		List<String> derivativeColumns = RegexUtil.getMatchedGroup4Regex(content, R.SHEET_REGEX);

		// 判断衍生属性的是否加入layer的whereSQL
		boolean addingWhereSQLByLayer = true;
		if (content.contains("sum(") || content.contains("count(") || content.contains("group_concat(")) {
			addingWhereSQLByLayer = false;
		}

		for (String c : derivativeColumns) {
			list.add(new PagingColumn(c, addingWhereSQLByLayer, PagingColumnType.derivative));
		}
		return list;
	}

	
	/**
	 * 
	 * @author: jx @Title: getPieceOfWhereSQL4Layer @Description: TODO(获取专为层
	 *          使用的where部分的SQL语句) @return: String @throws
	 */
	public List<String> getOrInitializeCachePigingColumnsOfwhereSQL4layer() {
		if (this.cachePigingColumnsOfwhereSQL4layer != null) {

			return this.cachePigingColumnsOfwhereSQL4layer;
		}
		this.cachePigingColumnsOfwhereSQL4layer = new ArrayList<>(10);
		for (PagingColumn pagingColumn : this.pagingColumns) {
			if (pagingColumn.isAddingWhereSQLByLayer()) {
				String key = pagingColumn.getKey();
				// 去掉{}
				String[] arr = key.substring(1, key.length() - 1).split("\\.");

				this.cachePigingColumnsOfwhereSQL4layer.add(arr[1]);
			}
		}
		return this.cachePigingColumnsOfwhereSQL4layer;
	}

	

	/**
	 * 
	 * @author: jx @Title: getOrInitializeCachePigingColumns @Description:
	 * TODO(获取去掉{sheet.xxxx}外衣->xxxx的所有分页属性集合) @return @throws
	 */
	public List<String> getOrInitializeCachePigingColumns() {
		if (this.cachePigingColumns != null) {

			return this.cachePigingColumns;
		}
		this.cachePigingColumns = new ArrayList<>(5);
		for (PagingColumn pagingColumn : this.pagingColumns) {
			String key = pagingColumn.getKey();
			// 去掉{}
			String arr = key.replace("{", "").replace("}", "").replace(R.SHEET_REGEX_CONTENT_PREFIX_STRING, "");

			this.cachePigingColumns.add(arr);
		}
		return this.cachePigingColumns;
	}
	
	

}
