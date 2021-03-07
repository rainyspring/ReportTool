package com.fulong.utils.v2.report.tool;

import org.apache.commons.lang3.StringUtils;

public class MathUtil {
	
	/**
	 * /// <summary>
	/// 将指定的自然数转换为26进制表示。映射关系：[1-26] ->[A-Z]。
	/// </summary>
	/// <param name="n">自然数（如果无效，则返回空字符串）。</param>
	/// <returns>26进制表示。</returns>
	
	 */
	public static String toNumberSystem26(int n){
		String s = "";
	    while (n > 0){
	        int m = n % 26;
	        if (m == 0) {
	        	m = 26;
	        }
	        s = (char)(m + 64) + s;
	        n = (n - m) / 26;
	    }
	    return s;
	} 

	/**
	 * <summary>
	 将指定的26进制表示转换为自然数。映射关系：[A-Z] ->[1-26]。
	 </summary>
	 <param name="s">26进制表示（如果无效，则返回0）。</param>
	 <returns>自然数。</returns>
	 */
	public static int fromNumberSystem26(String s){
	    if (StringUtils.isBlank(s)) {
	    	return 0; 
	    }
	    s = s.toUpperCase();
	    int n = 0;
	    char[] arr = s.toCharArray();
	    for (int i = arr.length - 1, j = 1; i >= 0; i--, j *= 26){
	        char c = arr[i];
	        if (c < 'A' || c > 'Z') {
	        	return 0;
	        }
	        //A的ASCII值为65
	        n += ((int)c - 64) * j;
	    }
	    return n;
	}


    public static void main(String[] args) {
		System.out.println(fromNumberSystem26("aa"));
		System.out.println(toNumberSystem26(27));
	}
}
