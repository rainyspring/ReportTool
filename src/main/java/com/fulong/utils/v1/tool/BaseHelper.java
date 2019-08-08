package com.fulong.utils.v1.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**日常工具类
 * @author 姜旭
 * @date 2015-08-25
 */
public final class BaseHelper {
	
	/**
	 * 将fileName第一个字母大写：处理反射的工具方法
	 * @param fileName
	 * @return
	 */
	public static String getMethodName1(String fileName){
		char[] chars =fileName.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);


	}
	/**
	 * 将fileName第一个字母大写：处理反射的工具方法
	 * @param fileName
	 * @return
	 */
	public static String getMethodName2(String fildeName){
		char[] chars =fildeName.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);


	}
	
	/**
	 * @note 	内置的前台页面都是内置格式：[yyyy-MM-dd HH:mm:ss]
	 * 			其中，coolink内置格式为：[yyyy-MM-dd HH:mm:ss]
	 * 			coobase前台页面内置格式为：[yyyy-MM-dd]
	 * @return 	返回一个[yyyy-MM-dd HH:mm:ss]格式的当日起始时间，
	 * 			即形如：2015-01-12 00:00:00
	 */
	public static long getStartTimeByFormatDate(){
		java.util.Date date = new java.util.Date();
		SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
		String date_string = sdf_day.format(date);
		SimpleDateFormat sdf_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long start =0;
		
		try {
			start = sdf_all.parse(date_string+" 00:00:00").getTime();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return start;
	}
	/**
	 * @note 	内置的前台页面都是内置格式：[yyyy-MM-dd HH:mm:ss]
	 * 			其中，coolink内置格式为：[yyyy-MM-dd HH:mm:ss]
	 * 			coobase前台页面内置格式为：[yyyy-MM-dd]
	 * @return 	返回一个[yyyy-MM-dd HH:mm:ss]格式的当日起始时间，
	 * 			即形如：2015-01-12 23:59:59
	 */
	public static long getEndTimeByFormatDate(){
		java.util.Date date = new java.util.Date();
		SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
		String date_string = sdf_day.format(date);
		SimpleDateFormat sdf_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		long end =0;
		try {
			
			end = sdf_all.parse(date_string+" 23:59:59").getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return end;
	}
	public static long getStartTimeByWeek(){
		// 本周
		SimpleDateFormat df_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal =Calendar.getInstance();
        //获取本周一的日期
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); 
        String monday = df_date.format(cal.getTime());
        long num =0;
        try {
			num = df_all.parse(monday+" 00:00:00").getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}
	public static long getEndTimeByWeek(){
		// 本周末
		SimpleDateFormat df_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal =Calendar.getInstance();
		//这种输出的是上个星期周日的日期，因为老外那边把周日当成第一天
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        //增加一个星期，才是我们中国人理解的本周日的日期
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        String sunday = df_date.format(cal.getTime());
        long num = 0;
		try {
			num= df_all.parse(sunday+" 23:59:59").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return num;
	}
	public static long getStartTimeByYear(){
		// //获取本月的最小日
		SimpleDateFormat df_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");
		
		Calendar cal =Calendar.getInstance();		 
		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE)); 
        String minOfMonth = df_date.format(cal.getTime());
        long num =0;
        try {
			num = df_all.parse(minOfMonth+" 00:00:00").getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return num;
	}
	public static long getEndTimeByYear(){
		  //获取本月的最大日
		SimpleDateFormat df_all = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal =Calendar.getInstance();		 //获取本月的最小日
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));  
        String maxOfMonth = df_date.format(cal.getTime());
        long num = 0;
		try {
			num= df_all.parse(maxOfMonth+" 23:59:59").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return num;
	}
	

	public static String getMD5(String str){
		return MD5Util.create(str);
	}
	public static String getEmpty2Null(Object o){
		return o==null?"":o.toString();
	}
	/**
	 * 从newData中挑出在oldData不存在的值，需要新增
	 * @param oldData
	 * @param newData
	 * @return
	 */
	public static List<String> getNeedAddData(String[] oldData, String[] newData) {
		int nl = newData.length;
		int ol = oldData.length;
		List<String> needAdd = new LinkedList<String>();
		for (int i = 0; i < nl; i++) {
			//默认不追加
			boolean add = false;
			for (int j = 0; j < ol; j++) {
				if (newData[i].equals(oldData[j])) {
					add = false;
					break;
				} else {
					add = true;
				}
			}
			if (add) {
				needAdd.add(newData[i]);
			}

		}

		return needAdd;
	}

	/**
	 * 从oldData中挑出在newData不存在的值,需要删除
	 * @param oldData
	 * @param newData
	 * @return
	 */
	public static List<String> getNeedDelData(String[] oldData, String[] newData) {
		int nl = newData.length;
		int ol = oldData.length;
		List<String> needDel = new LinkedList<String>();
		for (int j = 0; j < ol; j++) {
			boolean del = false;
			for (int i = 0; i < nl; i++) {
				if (newData[i].equals(oldData[j])) {
					del = false;
					break;
				} else {
					del = true;
				}
			}
			if (del) {
				needDel.add(oldData[j]);
			}

		}

		return needDel;
	}
	/**
	 * 找出相同的
	 * @param oldData
	 * @param newData
	 * @return
	 */
	public static List<String> getTheSameData(String[] oldData, String[] newData) {
		int nl = newData.length;
		int ol = oldData.length;
		List<String> sameData = new LinkedList<String>();
		for (int j = 0; j < ol; j++) {
			boolean same = false;
			for (int i = 0; i < nl; i++) {
				if (newData[i].equals(oldData[j])) {
					same = true;
					break;
				} 
			}
			if (same) {
				sameData.add(newData[j]);
			}

		}
		return sameData;
	}
	public static boolean isNullorEmpty(Object o){
		if(o==null){
			return true;
		}else if(o instanceof String){
			return "".equals(o.toString())?true:false;
		}else if(o instanceof Map){
			Map map = (Map)o;
			return 0==map.size()?true:false;
		}else if(o instanceof List){
			List list = (List)o;
			return 0==list.size()?true:false;
		}else if(o instanceof Set){
			Set set = (Set)o;
			return 0==set.size()?true:false;
		}else{
			return "".equals(o.toString())?true:false;
		}
	}
	public static boolean isBlank(String o){
		if(StringUtils.isBlank(o)||o.equalsIgnoreCase("null")){
			return true;
		}
		return false;
	}
	public static boolean isWindowsOS() {
		String os = System.getProperty("os.name");  
		if(os.toLowerCase().startsWith("win")){  
			return true;
		}  
		return false;

	}
	/**
	 * 将文件名称加入日期
	 * @param fileName
	 * @return
	 */
	public static String getNewFileNameWithDate(String fileName){
		if(StringUtils.isBlank(fileName)) {
			return "";
		}
		int separator = fileName.indexOf(".");
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return fileName.substring(0, separator)+"-"+date+fileName.substring(separator);
	}
	
}
