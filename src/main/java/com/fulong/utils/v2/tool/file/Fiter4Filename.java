package com.fulong.utils.v2.tool.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.apache.commons.lang3.StringUtils;
/**
 * 
 * @description:TODO(过滤文件 名)   
 * @author:  jx
 * @date:   2019年3月22日 下午3:50:37   
 *
 */
public class Fiter4Filename implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		if (!StringUtils.isBlank(name) && name.endsWith("tmp")) {
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		File dir = new File("D:/");
		

		String fileName = "aa";
		//method 1
		dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if(name.equalsIgnoreCase(fileName)) {
					return true;
				}
				return false;
			}});
		
		
		
		
		
		File[] files  = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(pathname.getName().equalsIgnoreCase(fileName)) {
					return true;
				}
				return false;
			}
			
		});
		
	}

}
