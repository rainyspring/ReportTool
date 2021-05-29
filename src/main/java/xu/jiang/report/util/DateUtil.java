package xu.jiang.report.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @ClassName: DateUtil
 * @Description: TODO(常用的日期工具类)
 * @author jx
 * @date 2019年1月4日
 *
 */
public class DateUtil {
	/**
	 * 比较日期是否相同
	 * @author jx
	 * 形如：d1==null && d2==null -> false
	 * 	   d1!=null && d2==null-> false
	 * 	   d1==null && d2!=null-> false
	 * 	当d1、d2 都不为null 日期完全相同,如d1:2019/12/11,d2:2019/12/11 ->true
	 * 	当d1、d2 都不为null,日期相同，格式不同，但表示的日期相同 ,如d1:2019/12/11,d2:2019-12-11 ->true
	 * 	当d1、d2 都不为null，日期大致相同，格式不同，表示的日期精度略微不同 ,如d1:2019/12/11 23:12:09,d2:2019-12-11 ->true
	 * 	当d1、d2 都不为null，表示的日期完全不同 ,如d1:2019/12/11,d2:2018-01-10 ->false
	 * 
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean sameDate(Date d1, Date d2) {
		if(null == d1 || null == d2) {
			return false;
		}
		//return getOnlyDate(d1).equals(getOnlyDate(d2));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(d1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(d2);
		return  cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
	}
	
}
