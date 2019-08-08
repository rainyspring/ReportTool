package com.fulong.utils.v1.report.bean;

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;

/**
 * 层  数据处理的最基本单位
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:29:32
 */
public interface Layer extends Comparable<Layer>{
	/**
	 * 获取层索引
	 * @return
	 */
	int getIndex();
	/**
	 * 接收数据
	 * @param session
	 */
	void receiveData(Session session,Map<String,String> pagingParams);
	
	/**
	 * 输出数据
	 * @param appendRowNum 已经格外追加的行数
 	 * @return int 在层写入数据时，需要格外追加的行数，本行不计算在内
	 */
	int writeData(int appendRowNum ,Sheet sheet);
	
	/**
	 * 清空上一页缓存，便于下一页使用
	 * 为了处理不同分页使用同一结构时，出现缓存的现象
	 */
	void clear();

}
