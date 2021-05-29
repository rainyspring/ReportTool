package xu.jiang.report.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import org.apache.commons.lang3.StringUtils;
/**
 * 
 * @see
 * @author jx
 * @date 2018年1月10日 上午10:56:24
 */
public class SQLUtil {

	/**
	 * 将sql参数中的非法字符过滤,
	 * 外层不额外添加''
	 * @param param
	 * @return
	 */
	public static String filterSqlParam(String param){
		if(StringUtils.isBlank(param)) {
			return param;
		}
		
		return param.replace("\'", "\'\'");
		
	}
	/**
	 * 将sql参数中的非法字符过滤,
	 * 外层额外添加''
	 * 
	 * @param value
	 * @return
	 */
	public static String replaceEmpty2Null4SQL(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return "'" + value.replace("\'", "\'\'") + "'";

	}
	/**
	 * @deprecated :instead by filter2SqlOfIn(List<Object> list)
	 * 将数组形式的值拼凑成'value1','value2','value3'....(可以放在sql的in()里面)
	 * 缺点是arr1数组也被更改了
	 * @param arr1[值内部不能有null]
	 * @return
	 */
	public static String filter2SqlOfIn(Object[] arr1){
		Arrays.parallelSetAll(arr1, new IntFunction<String>() {
			@Override
			public String apply(int i) {
				return "'" + SQLUtil.filterSqlParam(arr1[i].toString()) + "'";
			}

		});
		String sql4AreaCode = Arrays.toString(arr1);
		return sql4AreaCode.substring(1, sql4AreaCode.length() - 1);

	}
	
	/**
	 * 将数组形式的值拼凑成'value1','value2','value3'....(可以放在sql的in()里面)
	 * list不会被更改
	 * @param arr1[值内部不能有null]
	 * @return
	 */
	public static <T> String filter2SqlOfIn(List<T> list){
		List<String> olist = new ArrayList<String>(list.size());
		for (T o : list) {
			olist.add("'" + o + "'");
		}
		return StringUtils.join(olist.toArray(),",");
	}

}
