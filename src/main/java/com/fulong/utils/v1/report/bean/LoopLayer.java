package com.fulong.utils.v1.report.bean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;

import com.fulong.utils.v1.report.Param;
import com.fulong.utils.v1.report.dealer.ReportDealer;
import com.fulong.utils.v1.report.tool.POIUtil;
import com.fulong.utils.v1.report.tool.R;
import com.fulong.utils.v1.report.tool.RegexUtil;
import com.fulong.utils.v1.tool.DataHelper;
import com.fulong.utils.v2.report.dto.MilepostDTO;

/**
 * 循环层 必定不包含合并单元格
 * 
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:31:43
 */
public class LoopLayer extends BaseLayer {

	private boolean isGroup = false;
	/**
	 * 是否在首部追加行号
	 */
	private boolean appendRowNo = false;

	private final List<String> columns;// 单元格内的属性

	/**
	 * 记录所有的select片段的属性（不包含带有函数的属性）
	 */
	private SelectAnalyzer sql_select = new SelectAnalyzer();
	/**
	 * 记录所有group by 片段,不能重复
	 */
	private GroupByAnalyzer sql_groupBy = new GroupByAnalyzer();
	/**
	 * 记录所有order by 片段
	 */
	private OrderByAnalyzer sql_orderBy = new OrderByAnalyzer();
	/**
	 * 记录所有where 片段
	 */
	private WhereAnalyzer sql_where;

	private List<Object[]> olist = null;
	/*
	 * 视图的别名
	 */
	private String table_AliasName = "";
	/*
	 * 视图名
	 */
	private String table_viewName = "";

	public LoopLayer(int index, List<String> columns, Set<CellRangeAddress> mergingColumns, Param param) {
		super(index, param);

		this.columns = columns;
		init();
	}

	/*
	 * 1 找视图或表的别名和真名
	 */
	private void init() {
		if (this.columns == null || this.columns.size() <= 0) {
			return;
		}

		for (String c : columns) {
			// 找表名或视图名
			List<String> groups = RegexUtil.getMatchedGroup4Regex(c, R.OBJECT_PROPERTY_REGEX);
			Iterator<String> iterator = groups.iterator();
			if (!iterator.hasNext()) {
				continue;
			}

			String someoneKey = RegexUtil.getFirstKeyByRegex(iterator.next(), R.OBJECT_PROPERTY_REGEX);

			// 如果是第一次获取表的别名，那么就获取下表或视图的别名

			this.table_AliasName = someoneKey.split("\\.")[0];
			break;

		}
		// 找到对应的真实的数据库表名或视图（去属性文件里找到对应关系）

		if (StringUtils.isBlank(this.table_AliasName)) {
			this.table_AliasName = "";
		}
		this.table_viewName = DataHelper.loadReportProperty(this.table_AliasName);

	}

	/**
	 * 1 解析columns，拼凑成sql， 2 将获取的数据源放入map<属性名,value> 规则：
	 * 普通属性替换符可以解析出对象和select属性集合 ui属性替换符解析出where部分 paging替换符也是where部分
	 * 系统替换符不作为sql拼写部分
	 * 
	 */
	@Override
	public void receiveData(Session session, Map<String, String> pagingParams) {

		sql_where = new WhereAnalyzer(param, this.sql_groupBy, pagingParams);

		/*
		 * 获取真正拼写sql的属性(select、order by、 group by)
		 */
		this.getFilterColumns(pagingParams);

		/*
		 * 获取分页的数据集
		 */
		this.olist = session.createSQLQuery(this.getSql()).list();
		if (this.olist == null) {
			this.olist = new ArrayList<Object[]>();
		}

	}

	/**
	 * 将真正拼写sql的columns填充到sql_columns中 并获取到sql的视图名和别名
	 */
	private void getFilterColumns(Map<String, String> pagingParams) {
		/*
		 * 循环过滤每个层的属性
		 * 
		 */
		int length = this.columns.size();
		for (int index = 0; index < length; index++) {
			String column = this.columns.get(index);
			// 空列
			if (StringUtils.isBlank(column)) {
				this.sql_select.add(this.createColunmName4fixedValue(""));
				continue;
			}

			// 解析排序替换符
			if (column.matches(R.Layer.ORDER_BY_REGEX)) {
				List<String> keys = ReportDealer.getColumnsRegex4OrderBySql(column);

				for (String key : keys) {
					String proName = RegexUtil.getFirstKeyByRegex(key, R.OBJECT_PROPERTY_REGEX);
					key = key.replaceAll(R.OBJECT_PROPERTY_REGEX, proName).replace("+", " asc ").replace("-", " desc ");
					// System.out.println("----"+key);
					this.sql_orderBy.add(key);

				}

			} else if (column.matches(R.Layer.WHERE_BY_REGEX)) {

				column = column.substring("whereBy(".length(), column.length() - 1);
				//去掉UI参数和对象属性的外衣
				column = RegexUtil.takeOffUIRegex(RegexUtil.takeOffPropertyRegex(column),this.param,true);
				
				this.sql_where.add(column);

			}
			else if (column.matches(R.Layer.GROUP_BY_REGEX)) {

				column = column.substring("GroupBy(".length(), column.length() - 1);
				//去掉UI参数和对象属性的外衣
				column = RegexUtil.takeOffUIRegex(RegexUtil.takeOffPropertyRegex(column),this.param,true);
				
				this.sql_groupBy.add(column);

			}
			// 带函数的焊缝动态里程碑
			else if (column.matches(R.Layer.DYNAMIC_MILEPOST_DATE_WITH_FUNCTION_WL)) {

				this.sql_select.addAll(this.analyzeRegex_DynamicMilepostDateInSumOrCountFunction(column,
						R.Layer.DYNAMIC_MILEPOST_DATE_WL, this.param.milepost_wl));
				// 记录动态的位置列号
				this.param.modifyDynamicFieldStartCol(index);

			}
			// 带函数的管段动态里程碑

			else if (column.matches(R.Layer.DYNAMIC_MILEPOST_DATE_WITH_FUNCTION_PS)) {
				String pipeSecPosId = this.param.uiParams.get("ui.pipeSecPosId");

				List<MilepostDTO> list = null;
				if ("Shop".equalsIgnoreCase(pipeSecPosId)) {
					list = this.param.milepost_ps_yz;

				} else if ("Field".equalsIgnoreCase(pipeSecPosId)) {
					list = this.param.milepost_ps_xc;

				} else {
					list = this.param.milepost_ps;

				}

				this.sql_select.addAll(this.analyzeRegex_DynamicMilepostDateInSumOrCountFunction(column,
						R.Layer.DYNAMIC_MILEPOST_DATE_PS, list));
				// 记录动态的位置列号
				this.param.modifyDynamicFieldStartCol(index);

			}
			// 解析普通的管段动态里程碑
			else if (column.matches(R.Layer.DYNAMIC_MILEPOST_DATE_PS)) {
				String pipeSecPosId = this.param.uiParams.get("ui.pipeSecPosId");

				List<MilepostDTO> list = null;
				if ("Shop".equalsIgnoreCase(pipeSecPosId)) {
					list = this.param.milepost_ps_yz;

				} else if ("Field".equalsIgnoreCase(pipeSecPosId)) {
					list = this.param.milepost_ps_xc;

				} else {
					list = this.param.milepost_ps;

				}

				for (MilepostDTO milepost : list) {

					this.sql_select.add(milepost.getMilepostId());

				}
				// 记录动态的位置列号
				this.param.modifyDynamicFieldStartCol(index);
			}
			// 解析普通的焊缝动态里程碑
			else if (column.matches(R.Layer.DYNAMIC_MILEPOST_DATE_WL)) {
				for (MilepostDTO milepost : this.param.milepost_wl) {

					this.sql_select.add(milepost.getMilepostId());

				}
				// 记录动态的位置列号
				this.param.modifyDynamicFieldStartCol(index);
			}

			else if (column.matches(".*" + R.Layer.FOUR_ARITHMETIC_IN_FUNCTION + ".*")) {
				// else if (column.matches(R.Layer.FourArithmeticOutOfFunction))
				// {

				/*
				 * 条件中的UI属性蜕皮,即去掉属性外层的{}
				 */
				column = RegexUtil.takeOffUIRegex(column, param, true);

				// 获取select片段
				String newColumn = this.analyzeRegex_FourAritheticOutOfFunction(column);
				this.sql_select.add(newColumn);
			}

			else if (column.matches(R.Layer.FOUR_ARITHMETIC)) {
				List<String> groups = RegexUtil.getMatchedGroup4Regex(column, R.OBJECT_PROPERTY_REGEX);
				if (groups != null && groups.size() > 0) {
					Iterator<String> iterator = groups.iterator();

					while (iterator.hasNext()) {
						String key = RegexUtil.getFirstKeyByRegex(iterator.next(), R.OBJECT_PROPERTY_REGEX);

						// 将count(wl.xxx) 放入goupby
						this.sql_groupBy.add(key);
					}
					this.sql_select.add(RegexUtil.takeOffPropertyRegex(column));

				}

			}
			// 是否追加行号
			else if (column.matches(R.Layer.ROW_NO)) {
				this.appendRowNo = true;
			}
			// 直接替换，且拼入sql的select片段
			else if (column.matches(R.SYS_PROPERTY_REGEX)) {

				column = RegexUtil.takeOffSysRegex(column, this.param, true);

				this.sql_select.add(column);
			}

			/*
			 * 替换UI参数，在sql中必然是常量
			 */
			else if (column.matches(R.UI_PROPERTY_REGEX)) {
				column = RegexUtil.takeOffUIRegex(column, this.param, true);
				this.sql_select.add(column);
			}
			/*
			 * 替換分页属性的同胞参数,必然是常量
			 */
			else if (column.matches(R.SHEET_REGEX)) {
				column = RegexUtil.takeOffPagingRegex(column, pagingParams, true);
				this.sql_select.add(column);
			}
			/*
			 * 混合型，可能有UI，sys或对象属性，但是不规则 形如：case when {ui.a} then {wl.b} end
			 */
			else {
				// 替换所有系统参数
				if (column.matches(".*" + R.SYS_PROPERTY_REGEX + ".*")) {
					column = RegexUtil.takeOffSysRegex(column, this.param, true);

				}

				/*
				 * 替换UI参数，在sql中必然是常量
				 */
				if (column.matches(".*" + R.UI_PROPERTY_REGEX + ".*")) {
					column = RegexUtil.takeOffUIRegex(column, this.param, true);
				}
				/*
				 * 替换Paging参数，在sql中必然是常量
				 */
				if (column.matches(".*" + R.SHEET_REGEX + ".*")) {
					column = RegexUtil.takeOffPagingRegex(column, pagingParams, true);
				}

				if (column.matches(".*" + R.OBJECT_PROPERTY_REGEX + ".*")) {
					// 不再单独计入select中
					column = RegexUtil.takeOffPropertyRegex(column);
					this.sql_select.add("(" + column + ")");

				} else {// 就是普通的字符串
					this.sql_select.add(this.createColunmName4fixedValue(column));

				}

			}

		}

	}

	/**
	 * 解析负责的带条件的复杂函数 形如： String v = "sum(distinct {wl.b}+{wl.e}/{wl.f})<{wl.d}
	 * like '%jiang'>/count({wl.a}*{wl.g})+sum({wl.c})<{wl.xx}>20 and {wl.d}
	 * between 1 and 8>"; String v = "sum(distinct {wl.b}+{wl.e}/{wl.f})<{wl.d}
	 * like '%jiang'>/count({wl.a}*{wl.g})+sum({wl.c})<{wl.d} like '%xu'>";
	 * 
	 * @param column
	 * @return
	 */
	private String analyzeRegex_FourAritheticOutOfFunction(String v) {

		/*
		 * 获取所有的函数片段 每个函数片段符合正则：R.layer.FourArithmeticInFunction
		 */
		List<String> functions = RegexUtil.getMatchedGroup4Regex(v, R.Layer.FOUR_ARITHMETIC_IN_FUNCTION);

		for (String f : functions) {// 遍历每个单独函数
			String newf = this.getPieceSelectSqlByFunction(f, null);
			/*
			 * 将单个函数的最终结果替换到总体中,加个括号避免出现a/(b-1)变成a/b-1的情况
			 */
			v = v.replace(f, newf);
		}
		/*
		 * 脱去属性外衣{}
		 */
		v = RegexUtil.takeOffPropertyRegex(v);
		// 考虑到这个函数是整体，如果N/0是会是null，故转换下
		v = "ifnull((" + v + "),0)";

		return v;
	}

	/**
	 * 解析在sum 或count函数中的动态里程碑sql片段,将多个select值并放入select集合中
	 * 
	 * @param string
	 */
	private List<String> analyzeRegex_DynamicMilepostDateInSumOrCountFunction(String v, String milepostTypeRegex,
			List<MilepostDTO> list) {
		/*
		 * 获取条件,形如<...>，符合正则R.layer.Sql_piece
		 */
		List<String> groups = RegexUtil.getMatchedGroup4Regex(v, R.Layer.FOUR_ARITHMETIC_IN_FUNCTION);

		// 函数主体,目前只限只能有一个函数主体
		String functionObject = groups.iterator().next();
		/*
		 * 条件，会放在函数的case when语句中
		 */
		String condition = v.replace(functionObject, "");

		// 去掉函数条件边界
		int left = condition.indexOf(R.Layer.FUNCTION_CONDITION_LEFT);
		int right = condition.lastIndexOf(R.Layer.FUNCTION_CONDITION_RIGHT);

		// 得到去掉边界的真实的函数条件
		condition = condition.substring(left + R.Layer.FUNCTION_CONDITION_LEFT.length(), right);

		// 存储多个select值
		List<String> arr = new ArrayList<String>();
		for (MilepostDTO milepost : list) {
			String piece_condition = condition.replaceAll(milepostTypeRegex, milepost.getMilepostId());
			String milepost_functionObject = this.getPieceSelectSqlByFunction(functionObject, piece_condition);
			/*
			 * 脱去属性外衣{}
			 */
			milepost_functionObject = RegexUtil.takeOffPropertyRegex(milepost_functionObject);
			// 考虑到这个函数是整体，如果N/0是会是null，故转换下
			milepost_functionObject = "ifnull((" + milepost_functionObject + "),0)";
			arr.add(milepost_functionObject);
		}
		return arr;
	}

	/**
	 * 解析一个函数，函数正则R.layer.FourArithmeticInFunction
	 * 解析形如sum(distinct{wl.b}+{wl.e}/{wl.f})<condition>
	 * 将<conditon>去掉，放入前部分的主体中，拼凑形如case when (condition) then (主体) end 结构
	 * 
	 * @param f
	 * @param otherCondition
	 *            额外的条件，放入condition中 @return外围已经包围()
	 */
	private String getPieceSelectSqlByFunction(String f, String otherCondition) {
		/*
		 * 获取条件,形如<...>，符合正则R.layer.Sql_piece
		 */
		List<String> groupsByPerFunction = RegexUtil.getMatchedGroup4Regex(f, R.Layer.SQL_PIECE_REGEX);

		/**
		 * 函数主体
		 */
		String functionObject = f;// 默认是f，认为f是没有条件的，即没有<...>
		String condition = "1=1";// 默认条件
		if (groupsByPerFunction.size() > 0) {
			/*
			 * 条件，会放在函数的case when语句中 只能有一个条件
			 */
			condition = groupsByPerFunction.iterator().next();
			// 获取该函数主体
			functionObject = functionObject.replace(condition, "");
			// 去掉函数条件边界
			int left = condition.indexOf(R.Layer.FUNCTION_CONDITION_LEFT);
			int right = condition.lastIndexOf(R.Layer.FUNCTION_CONDITION_RIGHT);
			// 得到去掉边界的真实的函数条件
			condition = condition.substring(left + R.Layer.FUNCTION_CONDITION_LEFT.length(), right);

		}
		// 是否存在额外的条件
		if (!StringUtils.isBlank(otherCondition)) {

			condition += " and " + otherCondition;

		}

		String fourArithmetic = RegexUtil.getMatchedGroup4Regex(functionObject, R.Layer.FOUR_ARITHMETIC).iterator()
				.next();

		String caseWhen = "case when (" + condition + ") then (" + fourArithmetic + ")  end ";
		/*
		 * 单个函数体的最终结果
		 */
		return "(" + functionObject.replace(fourArithmetic, caseWhen) + ")";
	}

	/**
	 * 在where语句中并入conditon条件 形如 "" 或 A.a='111' 或 A.a is not null and
	 * A.b='2019/10/10'
	 * 
	 * @param condition
	 * @return
	 */
	/*
	 * private String filterStatisticslayer(String condition) {
	 * 
	 * String where_partOfSql4names = this.sql_where.getSql(); if
	 * (StringUtils.isBlank(condition)) return where_partOfSql4names; if
	 * (StringUtils.isBlank(where_partOfSql4names)) return condition;
	 * 
	 * 
	 * 统计类型的层，将where语句加入到count/sum的case when中， 不再拼入总sql的where中
	 * 
	 * if (this.statisticsLayer) { where_partOfSql4names += " and " + condition;
	 * } return where_partOfSql4names; }
	 */

	/**
	 * 获取视图或表的别名
	 * 
	 * @param beWithPoint
	 *            是否带点，如果为空，这个参数无效
	 * @return
	 */
	private String getTableAliasName(boolean beWithPoint) {
		if (StringUtils.isBlank(this.table_AliasName)) {
			return "";
		}
		if (beWithPoint) {
			return this.table_AliasName + ".";
		}
		return this.table_AliasName;
	}

	/**
	 * 拼凑sql
	 * 
	 * @return
	 */
	private String getSql() {

		// 将集合中的str按照逗号分隔拼凑成sql的where片段
		String where_partOfSql4names = this.sql_where.getSql();
		if (!StringUtils.isBlank(where_partOfSql4names)) {
			where_partOfSql4names = " where " + where_partOfSql4names;
		}

		// 将集合中的str按照逗号分隔拼凑成sql的orderby片段
		String orderBy_partOfSql4names = this.sql_orderBy.getSql();
		if (!StringUtils.isBlank(orderBy_partOfSql4names)) {
			orderBy_partOfSql4names = " order by " + orderBy_partOfSql4names;
		}

		// 将集合中的str按照逗号分隔拼凑成sql的group By片段
		String groupBy_partOfSql4names = this.sql_groupBy.getSql();
		if (!StringUtils.isBlank(groupBy_partOfSql4names)) {
			groupBy_partOfSql4names = " group by " + groupBy_partOfSql4names;
		}

		// 将集合中的str按照逗号分隔拼凑成sql的select片段
		String select_partOfSql4names = this.sql_select.getSql();

		/*
		 * 形如：select a.A,a.B from view a group by a.A,a.B
		 * 由于分页属性可能不是对象的主键，select后可能存在重复值， 而distinct无法对多个属性操作，故采用group by
		 */
		String sql = "select " + select_partOfSql4names + " from " + this.table_viewName + " " + this.table_AliasName
				+ where_partOfSql4names + groupBy_partOfSql4names + orderBy_partOfSql4names;

		return sql;
	}

	@Override
	public int writeData(int appendRowNum, Sheet sheet) {
		/*
		 * 计算当前数据写入的行号
		 */
		int currentIndexRow = appendRowNum + this.getIndex();

		/*
		 * 插入空白行 注意startRowIndex也是数据行，故空白行应该少插一行
		 */
		if (this.olist == null || this.olist.size() <= 0) {// 虽然数据没有，但是该层的替换符的位置依然属于本层

			return 0;
		}
		int size = this.olist.size();
		if (size >= 2) {
			POIUtil.insertRowsAfterRowIndex(sheet, currentIndexRow, size - 1, false);
		}

		/*
		 * 将值放入单元格
		 */
		// 找到动态列的列号
		int dynamicColStartCol = this.param.getDynamicField_startCol();
		// 找到复制的样式
		CellStyle modelStyle = null;

		for (int i = 0; i < olist.size(); i++) {
			// 初次获取row值
			Object[] initialArr = olist.get(i);
			// 真正的row
			List<Object> r = new LinkedList<Object>();
			// 是否需要在行首加入行号
			if (this.appendRowNo) {
				r.add(String.valueOf(i + 1));
			}
			r.addAll(Arrays.asList(initialArr));

			Row row = POIUtil.getOrCreateRow(sheet, currentIndexRow++);
			int data4Row = r.size();
			for (int j = 0; j < data4Row; j++) {
				Cell c = POIUtil.getOrCreateCell(row, j);
				c.setCellValue(this.filterValue(r.get(j)));

				// 将单元格样式从左边的单元格复制一下，避免这个单元格是动态字段生成的单元格
				if (j <= dynamicColStartCol && modelStyle == null) {
					modelStyle = c.getCellStyle();
				}
				if (j >= dynamicColStartCol && j < data4Row) {// 当然复制样式也不要超过数据总列数
					CellStyle newstyle = sheet.getWorkbook().createCellStyle();
					POIUtil.copyCellStyle(modelStyle, newstyle);
					c.setCellStyle(newstyle);
				}
			}

		}

		return size - 1;
	}

	/**
	 * 对数据库中的值进行过滤美化
	 * 
	 * @param v
	 * @return
	 */
	private String filterValue(Object v) {
		if (v == null || StringUtils.isBlank(v.toString())) {
			return "";
		}
		if (v instanceof Double) {
			double d = (Double) v;
			if (d == 0) {
				return "0";
			}
			return String.valueOf(DataHelper.formatDouble(Double.valueOf(d)));

		} else if (v instanceof BigDecimal) {
			BigDecimal b = (BigDecimal) v;
			double d = b.doubleValue();
			if (d == 0) {
				return "0";
			}
			return String.valueOf(DataHelper.formatDouble(Double.valueOf(d)));

		} else if (v instanceof Date) {
			Date date = (Date) v;
			return new SimpleDateFormat("yyyy/MM/dd").format(date);
		}

		return String.valueOf(v);
	}

	/**
	 * 标记为组内层
	 * 
	 * @return
	 */
	@Override
	public LoopLayer remarkGroup() {
		this.isGroup = true;
		return this;
	}

	/**
	 * 该层是否为组内层
	 * 
	 * @return
	 */
	@Override
	public boolean isGroup() {
		return this.isGroup;
	}

	/**
	 * 为select 为属性生成别名
	 * 
	 * @return
	 */
	private String createColunmName4fixedValue(String value) {
		value = value.replace("'", "''");
		return "'" + value + "'";
	}

	/**
	 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
	 */
	@Override
	public void clear() {
		this.sql_groupBy.clear();
		this.sql_orderBy.clear();
		this.sql_select.clear();
		if (this.sql_where != null) {
			this.sql_where.clear();
		}
	}

	/**
	 * Layer中sql的where部分解析器
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月16日 上午9:00:56
	 */
	private class WhereAnalyzer {
		private final Param param;
		private final GroupByAnalyzer groupBy;
		private final Map<String, String> pagingParams;
		/**
		 * 记录所有where 片段
		 */
		private List<String> sql_where_columns = new ArrayList<String>();

		public WhereAnalyzer(Param param, GroupByAnalyzer groupBy, Map<String, String> pagingParams) {
			this.param = param;
			this.groupBy = groupBy;
			this.pagingParams = pagingParams;
			
		}
		public void add(String column) {
			this.sql_where_columns.add(column);
		}
		private void init() {
			// 从UI参数中获取where 片段，
			for (Entry<String, String> o : param.uiParams.entrySet()) {
				String name = o.getKey().split("\\.")[1];
				String value = o.getValue();
				if (!StringUtils.isBlank(name) && !StringUtils.isBlank(value)) {
					if (this.isOutOfWhere4UIName(name)) {
						continue;
					}
					if ("whereSql".equals(name)) {
						this.sql_where_columns.add(value);

					} else if ("groupBySql".equals(name)) {
						this.groupBy.add(value);

					} else {
						this.sql_where_columns.add(getTableAliasName(true) + name + "='" + value + "'");

					}

				}

			}

			/*
			 * 从PagingParams中获取where片段
			 */
			if (this.pagingParams == null || this.pagingParams.size() <= 0) {
				return;
			}

			for (Entry<String, String> o : this.pagingParams.entrySet()) {
				String name = o.getKey();
				String value = o.getValue();
				if (!StringUtils.isBlank(name) && !StringUtils.isBlank(value)) {

					this.sql_where_columns.add(getTableAliasName(true) + name + "='" + value + "'");

				}

			}
		}

		/**
		 * 不拼写在where中的UI参数
		 * 
		 * @param name
		 * @return
		 */
		private boolean isOutOfWhere4UIName(String name) {
			if (R.SRING_START_DATE.equalsIgnoreCase(name) || R.SRING_END_DATE.equalsIgnoreCase(name)) {
				/*
				 * 加入格外的where条件
				 */
				// this.sql_where_colunms.add("weldedDate is not null");
				return true;
			}
			if (R.SRING_DAYS.equalsIgnoreCase(name)) {
				return true;
			}
			return false;
		}

		public String getSql() {
			init();
			// 将集合中的str按照逗号分隔拼凑成sql的where片段
			String[] sql_where_arr = this.sql_where_columns.toArray(new String[this.sql_where_columns.size()]);
			String where_partOfSql4names = StringUtils.join(sql_where_arr, " and ");
			return where_partOfSql4names;
		}

		/**
		 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
		 */
		public WhereAnalyzer clear() {
			this.sql_where_columns.clear();
			return this;
		}

	}

	/**
	 * Layer中sql的select部分解析器
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月16日 上午9:00:56
	 */
	private class SelectAnalyzer {
		private final int aliasName_startIndex = 1;
		/**
		 * 属性别名索引 用于系统自动生成属性别名
		 */
		private int aliasNameIndex_column = aliasName_startIndex;
		/**
		 * 记录所有的select片段的属性（不包含带有函数的属性）
		 */
		private List<String> sql_select_columns = new ArrayList<String>();
		/**
		 * 是否已经初始化字段别名
		 */
		private boolean initAliasName = false;

		public void add(String column) {
			this.sql_select_columns.add(column);
		}

		public void addAll(Collection<? extends String> column) {
			this.sql_select_columns.addAll(column);
		}

		/**
		 * 1 为select属性加上别名 2 同时去除尾部很多空的多余字段
		 */
		private void addAliasName2SelectColumns() {
			int length = this.sql_select_columns.size();

			/*
			 * 由于sql字段尾部很多空串的片段，必须去掉，不然样式会有多余的修饰 故要去掉多余的空字段
			 */
			int index = -1;
			for (int i = length - 1; i >= 0; i--) {
				String column = this.sql_select_columns.get(i);
				if (StringUtils.isBlank(column) || "''".equals(column)) {
					index = i;
				} else {
					break;
				}
			}
			List<String> list = new ArrayList<String>();

			for (int i = 0; i < length; i++) {
				if (index == -1 || i < index) {// 空字段起始处，之后的字段都是空字段，删除之，其他的加上别名
					list.add(this.appendAlias4SqlColumn(this.sql_select_columns.get(i)));
				}
			}
			this.sql_select_columns = list;
		}

		/**
		 * 为sql中的select属性加上别名
		 * 
		 * @param value
		 * @return
		 */
		private String appendAlias4SqlColumn(String value) {

			return value + " as " + this.createAlias4Column();
		}

		/**
		 * 创建个属性名别名
		 * 
		 * @return
		 */
		private String createAlias4Column() {
			return "'" + (this.aliasNameIndex_column++) + "'";
		}

		/**
		 * 获取整个select片段
		 * 
		 * @return
		 */
		public String getSql() {
			if (!this.initAliasName) {
				this.initAliasName = true;
				this.addAliasName2SelectColumns();
			}
			// 将集合中的str按照逗号分隔拼凑成sql的select片段
			String[] sql_select_arr = this.sql_select_columns.toArray(new String[this.sql_select_columns.size()]);
			String select_partOfSql4names = StringUtils.join(sql_select_arr, ",");

			return select_partOfSql4names;
		}

		/**
		 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
		 */
		public SelectAnalyzer clear() {
			this.sql_select_columns.clear();
			this.initAliasName = false;
			this.aliasNameIndex_column = aliasName_startIndex;
			return this;
		}

	}

	/**
	 * Layer中sql的group by部分解析器
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月16日 上午9:00:56
	 */
	private class GroupByAnalyzer {
		/**
		 * 记录所有group by 片段,不能重复
		 */
		private Set<String> sql_groupBy_columns = new HashSet<String>();

		public void add(String column) {
			this.sql_groupBy_columns.add(column);
		}

		public String getSql() {
			String groupBy_partOfSql4names = StringUtils
					.join(this.sql_groupBy_columns.toArray(new String[this.sql_groupBy_columns.size()]), ",");

			return groupBy_partOfSql4names;
		}

		/**
		 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
		 */
		public GroupByAnalyzer clear() {
			this.sql_groupBy_columns.clear();
			return this;
		}

	}

	/**
	 * Layer中sql的order by部分d解析器
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月16日 上午9:00:56
	 */
	private class OrderByAnalyzer {
		/**
		 * 记录所有order by 片段
		 */
		private List<String> sql_orderBy_columns = new ArrayList<String>();

		public void add(String column) {
			this.sql_orderBy_columns.add(column);
		}

		public String getSql() {
			// 将集合中的str按照逗号分隔拼凑成sql的orderby片段
			String[] sql_orderBy_arr = this.sql_orderBy_columns.toArray(new String[this.sql_orderBy_columns.size()]);
			String orderBy_partOfSql4names = StringUtils.join(sql_orderBy_arr, ",");
			return orderBy_partOfSql4names;
		}

		/**
		 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
		 */
		public OrderByAnalyzer clear() {
			this.sql_orderBy_columns.clear();
			return this;
		}
	}

}
