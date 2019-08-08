package com.fulong.utils.v1.report.tool;

/**
 * 正则中的常用静态变量
 * @see R ：Regex
 * @author jx
 * @date 2017年5月11日 上午9:39:50
 */
public class R {
	
	public final static String STRING_MODELFILENAME="modelFileName";
	public final static String SRING_START_DATE="startDate";
	public final static String SRING_END_DATE="endDate";
	public final static String SRING_DAYS="days";
	
	public final static String SRING_DYNAMIC_CURRENT_MILEPOST_PIECE="dynamicCurrentMilepost";
	public final static String SRING_DYNAMIC_CURRENT_MILEPOST="\\{(wl|ps)\\."+SRING_DYNAMIC_CURRENT_MILEPOST_PIECE+"\\}";
	
	public final static String SRING_DYNAMIC_CURRENT_DATE_PIECE="dynamicCurrentDate";
	public final static String SRING_DYNAMIC_CURRENT_DATE="\\{ui\\."+SRING_DYNAMIC_CURRENT_DATE_PIECE+"\\}";
	/**
	 * 系统参数的正则
	 */
	public final static String SYS_PROPERTY_REGEX = "\\{sys\\.([\\w]+)\\}";
	/**
	 * 页面参数的正则
	 */
	public final static String UI_PROPERTY_REGEX = "\\{ui\\.([\\w]+)\\}";
	/**
	 * 页的正则
	 */
	public final static String SHEET_REGEX = "\\{sheet\\.([\\w]+)\\}";
	
	/**
	 * 对象属性的正则
	 */
	public final static String OBJECT_PROPERTY_REGEX = "\\{((wl|ps|pl|fx|nde|kt|tb)\\.[\\w]+)\\}";
	/**
	 * 包含UI、sys、对象属性的正则，和其他用{}内部括住的东东，不包含分页属性
	 */
	private final static String ANY_PROPERTY_REGEX = "\\{[^\\{\\}\\(\\)]+\\}";
	
	
	public static class Layer{
	
		/**
		 * 针对函数追加特有条件的左边界
		 */
		public final static String FUNCTION_CONDITION_LEFT = "<";
		/**
		 * 针对函数追加特有条件的右边界
		 */
		public final static String FUNCTION_CONDITION_RIGHT = ">";
		
		/**
		 * 层首部追加行号的正则
		 */
		public final static String ROW_NO = "\\{sys\\.rowno\\}";
		
		/**
		 * 普通的条件sql中允许包含的字符的正则
		 * 不能包含count sum 等字符串集合
		 */
		public final static String SQL_PIECE = "((?!(count)|(><)|milepostDate_wl|milepostDate_ps|(sum)|(\\[)|(\\])|(>\\+)|(>-)|(>/)|(>\\*)).)+";
		
		/**
		 * 普通的动态里程碑-管段
		 */
		public final static String DYNAMIC_MILEPOST_DATE_PS ="\\{(wl|ps)\\.milepostDate_ps\\}";
		/**
		 * 普通的动态里程碑-焊缝
		 */
		public final static String DYNAMIC_MILEPOST_DATE_WL ="\\{(wl|ps)\\.milepostDate_wl\\}";
		
		/**
		 * sql中包含的字符的正则
		 */
		public final static String SQL_PIECE_REGEX = FUNCTION_CONDITION_LEFT+SQL_PIECE+FUNCTION_CONDITION_RIGHT;
		
		/**
		 * 属性的四则运算 ==0时，认为是自运算
		 */
		public final static String FOUR_ARITHMETIC = OBJECT_PROPERTY_REGEX + "([\\*+-/]((" + OBJECT_PROPERTY_REGEX + ")|("+UI_PROPERTY_REGEX+")|("+OBJECT_PROPERTY_REGEX+")|([\\d]+))){0,}";
		/**
		 * 公式正则，函数内部包含四则运算
		 */
		public final static String FOUR_ARITHMETIC_IN_FUNCTION = "(count|sum)\\((distinct )?" + FOUR_ARITHMETIC + "\\)("+SQL_PIECE_REGEX+"){0,1}";
		/**
		 * 公式正则，函数外部进行四则运算 完整
		 */
		public final static String FOUR_ARITHMETIC_OUT_OF_FUNCTION = FOUR_ARITHMETIC_IN_FUNCTION+"([\\*+-/](("+FOUR_ARITHMETIC_IN_FUNCTION+")|("+UI_PROPERTY_REGEX+")|("+OBJECT_PROPERTY_REGEX+")|([\\d]+))){0,}";
		/**
		 * 动态字段-带函数的焊缝里程碑公式正则
		 * 形如：
		 * sum({wl.precastInch_wl})<{wl.milepostDate_wl} between ({ui.startDate},{ui.endDate})>
		 */
		public final static String DYNAMIC_MILEPOST_DATE_WITH_FUNCTION_WL = FOUR_ARITHMETIC_IN_FUNCTION +FUNCTION_CONDITION_LEFT+ DYNAMIC_MILEPOST_DATE_WL+SQL_PIECE+FUNCTION_CONDITION_RIGHT;
		
		/**
		 * 动态字段-带函数的管段里程碑公式正则
		 */
		public final static String DYNAMIC_MILEPOST_DATE_WITH_FUNCTION_PS = FOUR_ARITHMETIC_IN_FUNCTION +FUNCTION_CONDITION_LEFT+  DYNAMIC_MILEPOST_DATE_PS+SQL_PIECE+FUNCTION_CONDITION_RIGHT;
		
		/**
		 * 排序正则（order by语句）
		 */
		public final static String ORDER_BY_REGEX = "orderBy\\((" + OBJECT_PROPERTY_REGEX + "[-+])(,("
				+ OBJECT_PROPERTY_REGEX + "[-+])){0,}\\)";
		/**
		 * 排序正则的sql片段
		 */
		public final static String ORDER_BY_REGEX_PIECE = "^" + OBJECT_PROPERTY_REGEX + "[-+]$";
		/**
		 * where正则
		 */
		public final static String WHERE_BY_REGEX="whereBy\\("+SQL_PIECE+"\\)";
		/**
		 * 分组正则（group by语句）
		 */
		public final static String GROUP_BY_REGEX="groupBy\\("+SQL_PIECE+"\\)";
		
	}
	public static class Report{
		
		/**
		 * 组的正则
		 */
		public final static String GROUP_REGEX_COMPLETE = "^group\\(" + OBJECT_PROPERTY_REGEX + ",(" + OBJECT_PROPERTY_REGEX
				+ "),([0-9]+)\\)$";
		/**
		 * 参考的正则
		 */
		public final static String REFER_REGEX = "^refer\\{([0-9]+)\\}$";
		
		/**
		 * 延伸且合并
		 */
		public final static String REGEX_RANGE_ONE = "\\([0-9]+~[0-9]+\\)";
		public final static String REGEX_RANGE_TWO = "\\([0-9]+(,[0-9]+){0,}\\)";
		/**
		 * 仅仅延伸
		 */
		public final static String REGEX_RANGE_THREE = "\\[[0-9]+~[0-9]+\\]";
		public final static String REGEX_RANGE_FOUR = "\\[[0-9]+(,[0-9]+){0,}\\]";
		
		/**
		 * 合并平移 
		 */
		public final static String RANGE_PIECE_PUSH_MERGING = "(("+REGEX_RANGE_ONE+")|("+REGEX_RANGE_TWO+")){0,1}";
		/**
		 * 平移复制
		 */
		public final static String RANGE_PIECE_PUSH_COPYING = "(("+REGEX_RANGE_THREE+")|("+REGEX_RANGE_FOUR+")){0,1}";
		
		/**
		 * 基本的管段动态里程碑标题-
		 */
		public final static String MILEPOST_PS_NAME_PIECE = "\\{milepost_ps\\.names\\}";
		/**
		 * 基本的焊缝动态里程碑标题-
		 */
		public final static String MILEPOST_WL_NAME_PIECE = "\\{milepost_wl\\.names\\}";
		/**
		 * 管段动态里程碑标题1
		 */
		public final static String MILEPOST_PS_NAME_REGEX_ONE = MILEPOST_PS_NAME_PIECE+"(("+REGEX_RANGE_ONE+")|("+REGEX_RANGE_TWO+")){0,1}";
		
		
		/**
		 * 焊缝动态里程碑标题1
		 */
		public final static String MILEPOST_WL_NAME_REGEX_ONE = MILEPOST_WL_NAME_PIECE+"(("+REGEX_RANGE_ONE+")|("+REGEX_RANGE_TWO+")){0,1}";
		
		public final static String UI_WELDED_DATE_RANGE_PIECE = "\\{ui."+R.SRING_START_DATE+"~ui."+R.SRING_END_DATE+"\\}";
		/**
		 * 时间范围
		 */
		public final static String UI_WELDED_DATE_RANGE = UI_WELDED_DATE_RANGE_PIECE+"(("+REGEX_RANGE_THREE+")|("+REGEX_RANGE_FOUR+")){0,1}";
		
		/**
		 * 范围
		 */
		public final static String RANGE_REGEX = ANY_PROPERTY_REGEX+RANGE_PIECE_PUSH_MERGING+RANGE_PIECE_PUSH_COPYING;
		
	}
	
}
