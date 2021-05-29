package xu.jiang.report.v2.report.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import xu.jiang.report.v2.report.Param;

/**
 * 报表中关于正则的帮助类
 * 
 * @see
 * @author jx
 * @date 2017年5月10日 上午9:16:10
 */
public class RegexUtil {

	/**
	 * 将column中脱去普通属性的外套 形如 {wl.isoCode}) --> wl.isoCode
	 * sum({wl.precastInch}+{wl.fieldDiam}) --> sum(wl.precastInch+wl.fieldDiam)
	 * 
	 * @param column
	 * @param syspropertyregex2
	 * @return
	 */
	public static String takeOffPropertyRegex(String column) {
		if (StringUtils.isBlank(column)) {
			return "";
		}

		List<String> sysParams = RegexUtil.getMatchedGroup4Regex(column, R.OBJECT_PROPERTY_REGEX);
		if (sysParams == null || sysParams.size() <= 0) {
			return column;
		}
		for (String p : sysParams) {// 遍历每个参数
			String key = RegexUtil.getFirstKeyByRegex(p, R.OBJECT_PROPERTY_REGEX);
			column = column.replace("{" + key + "}", key);
		}
		return column;
	}

	/**
	 * 将column中脱去系统属性的外套,并将真实的常量填充 形如 count({sys.isoCode}) --> count(12345)
	 * 
	 * @param column
	 * @param syspropertyregex2
	 * @param isPieceOfSql      是否为sql片段，是则加上'' ,否则不加
	 * @return
	 */
	public static String takeOffSysRegex(String column, Param param, boolean isPieceOfSql) {
		List<String> sysParams = RegexUtil.getMatchedGroup4Regex(column, R.SYS_PROPERTY_REGEX);
		if (sysParams == null || sysParams.size() <= 0) {
			return column;
		}
		for (String key : sysParams) {// 遍历每个参数
			key = key.replace("{", "").replace("}", "");
			String value = param.sysParams.get(key);
			value = value == null ? "" : value;
			if (isPieceOfSql) {
				value = "'" + value + "'";
			}
			column = column.replace("{" + key + "}", value);
		}

		return column;
	}
	
	/**
	 * 将column中脱去UI属性的外套,并将真实的常量填充 形如 count({sys.isoCode}) --> count(12345)
	 * 如果替换符无法获取数据，默认是空值
	 * 
	 * @param column
	 * @param syspropertyregex2
	 * @param isPieceOfSql      是否为sql片段，是则加上'' ,否则不加
	 * @return
	 */
	public static String takeOffUIRegex(String column, Param param, boolean isPieceOfSql) {
		List<String> sysParams = RegexUtil.getMatchedGroup4Regex(column, R.UI_PROPERTY_REGEX);
		if (sysParams == null || sysParams.size() <= 0) {
			return column;
		}
		for (String key : sysParams) {// 遍历每个参数
			key = key.replace("{", "").replace("}", "");
			String value = param.uiParams.get(key);
			if (StringUtils.isBlank(value)) {
				value = param.uiParams_defaultValue.get(key);

			}
			value = value == null ? "" : value;
			if (isPieceOfSql) {
				value = "'" + value + "'";
			}
			column = column.replace("{" + key + "}", value);
		}
		return column;
	}

	/**
	 * 将column中脱去分页属性的外套,并且将真实的常量填充 形如 count({sys.isoCode}) --> count(12345)
	 * 
	 * @param column
	 * @param        Map<String, String> pagingParams 分页参数
	 * @param        boolean isPieceOfSql 是否处在sql中
	 * @return
	 */
	public static String takeOffPagingRegex(String column, Map<String, String> pagingParams) {
		if (!column.matches(".*" + R.SHEET_REGEX + ".*")) {
			return column;
		}

		if (pagingParams == null || pagingParams.size() <= 0) {
			return column;
		}
		//获取分页值的集合的key
		String key = column.replace("{", "").replace("}", "").replace(R.SHEET_REGEX_CONTENT_PREFIX_STRING, "");
		//获取分页value
		String value = pagingParams.get(key);
		value = value == null ? "" : value;

		//column = column.replace(key, value);
		return value;
	}

	/**
	 * 将所有正则替换符 变为空
	 * 暂时没啥用
	 * @param column
	 */
	public static String takeOffAllRegex2Blank(String column){
		return column.replaceAll(R.UI_PROPERTY_REGEX, "").replaceAll( R.OBJECT_PROPERTY_REGEX, "")
		.replaceAll( R.SYS_PROPERTY_REGEX, "");
	}
	/**
	 * 解析[10~12]或[10,19,1]或(10~19)或(10,18,1) 提取其中的整数值
	 * 
	 * @return List<Integer> 永不为null
	 */
	public static List<Integer> analyzeIntegers(String value) {
		List<Integer> nums = new ArrayList<Integer>();
		if (StringUtils.isBlank(value)) {
			return nums;
		}

		String v = value.replace("[", "").replace("]", "").replace("(", "").replace(")", "");

		// 去掉][
		if (value.matches(R.Report.REGEX_RANGE_ONE) || value.matches(R.Report.REGEX_RANGE_THREE)) {

			String[] num_arr = v.split("~");
			if (num_arr.length != 2) {
				return nums;
			}

			int first = Integer.valueOf(num_arr[0]);
			int second = Integer.valueOf(num_arr[1]);
			if (first >= second) {
				return nums;
			}

			int length_numArr = second - first + 1;

			for (int k = 0; k < length_numArr; k++) {
				nums.add(first + k);
			}

		} else if (value.matches(R.Report.REGEX_RANGE_TWO) || value.matches(R.Report.REGEX_RANGE_FOUR)) {
			v = v.replace("[", "").replace("]", "").replace("(", "").replace(")", "");

			// 处理关联行
			String[] num_arr = v.split(",");
			Arrays.parallelSetAll(num_arr, new IntFunction<String>() {
				@Override
				public String apply(int i) {
					nums.add(Integer.valueOf(num_arr[i]));
					return num_arr[i];
				}

			});
		}

		return nums;
	}

	/**
	 * 通过正则获取报表中单元格中的第一个key 第0个是自己，忽略
	 * 
	 * @param v
	 * @param regex
	 * @return
	 */
	public static String getFirstKeyByRegex(String v, String regex) {
		Pattern p = Pattern.compile(regex);
		// System.out.println(p.toString());
		Matcher m = p.matcher(v);
		if (m.find()) {
			// System.out.println("the zero one:"+m.group(0));
			return m.group(1);
		}
		return "";
	}

	/**
	 * 通过正则获取报表中单元格中的所有不重复的key
	 * 
	 * @param v
	 * @return List 永不等于null,必须保证内部的值是长度从大到小的顺序， 不然存在包含的现象会很恶心
	 */
	public static List<String> getMatchedGroup4Regex(String v, String regex) {

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(v);
		List<String> olist = new ArrayList<String>();

		// 倒序
		// TreeMap<Integer,String> tree = new TreeMap<Integer,String>();
		while (m.find()) {

			for (int i = 0; i <= m.groupCount(); i++) {
				String group = m.group(i);
				if (!StringUtils.isBlank(group) && group.matches(regex)) {
					// olist.add(group);
					olist.add(group);
				}
			}
		}
		Collections.sort(olist, new Comparator<String>() {

			@Override
			public int compare(String a, String b) {
				if (a == null && b == null) {
					return 0;
				}
				if (a == null) {
					return 1;
				}
				if (b == null) {
					return -1;
				}
				// TODO Auto-generated method stub
				return b.length() - a.length();
			}
		});
		return olist;
	}

	/**
	 * 获取值中过去属性对象，没有则为""
	 * 
	 * @param v
	 * @return
	 */
	public static String getObjectName4Value(String v) {
		if (StringUtils.isBlank(v)) {
			return "";
		}

		Pattern p = Pattern.compile(R.OBJECT_PROPERTY_REGEX);
		Matcher m = p.matcher(v);
		// 倒序
		// TreeMap<Integer,String> tree = new TreeMap<Integer,String>();
		while (m.find()) {

			for (int i = 0; i <= m.groupCount(); i++) {
				String group = m.group(i);
				if (!StringUtils.isBlank(group) && group.matches(R.OBJECT_PROPERTY_REGEX)) {
					return group.replace("{", "").replace("}", "").split("\\.")[0];
				}
			}
		}

		return "";
	}
}
