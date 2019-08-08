package com.fulong.utils.v2.report.dealer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class A {

	
	public static void exportDataByPdf(List<Object[]> data,File outFile) {
		
		
	}
	
	@Test
	public void test2() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		
		String[] arr = list.toArray(new String[0]);
		System.out.println(arr.length);
	}
}
