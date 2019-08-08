package com.fulong.utils.v2.report.dealer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;

import com.fulong.utils.v2.report.Param;
import com.fulong.utils.v2.report.bean.Layer;
import com.fulong.utils.v2.report.tool.POIUtil;
import com.fulong.utils.v2.report.tool.R;
import com.fulong.utils.v2.report.tool.RegexUtil;

/**
 * sheet处理器 这里依然存在系统替换符、UI替换符、分页替换符、普通属性替换符
 * 
 * @see
 * @author jx
 * @date 2017年5月7日 上午9:42:54
 */
public class SheetDealer {
	/**
	 * sheet页签序号（从0开始）
	 */
	public final int indexSheet;

	/**
	 * 默认的sheet名
	 */
	public static final String DEFAULT_SHEET_NAME = "model";

	/**
	 * 每个sheet自己的模板，这个模板将分页替换符变为常量 如果 这些常量具有“参考”用途，都加{常量}包裹起来
	 */
	private final Sheet modelsheetOfMine;
	private final Param param;
	private final Map<String, String> pagingParamValues;
	/**
	 * 在层写入数据时，需要格外追加的行数，本行不计算在内
	 */
	private int currentAppendedRowNum = 0;

	public SheetDealer(int indexSheet, Sheet modelsheetOfMine, Map<String, String> pagingParamValues, Param param) {
		this.indexSheet = indexSheet;
		this.modelsheetOfMine = modelsheetOfMine;// 自己的模板
		this.param = param;// 全局公共参数
		this.pagingParamValues = pagingParamValues==null?new HashMap<String, String>(1):pagingParamValues;// 分页属性的同胞属性和其值
	}

	/**
	 * 执行方法
	 */
	public void runing(Session session) {
		// 记录最后一层的行号
		for (Entry<Integer, Layer> e : this.param.layers.entrySet()) {
			Layer layer = e.getValue();

			// 获取数据
			layer.receiveData(session, this.pagingParamValues);
			// 写数据
			this.currentAppendedRowNum += layer.writeData(this.currentAppendedRowNum, this.modelsheetOfMine);

		}

		// 为sheet页签命名
		POIUtil.renameSheet(modelsheetOfMine, String.valueOf(this.indexSheet+1));

		// 替换UI参数、替换分页参数（变成常量），将组替换符和排序替换符变为空
		for (int i = 0; i <= this.modelsheetOfMine.getLastRowNum(); i++) {
			Row row = this.modelsheetOfMine.getRow(i);
			if (row == null) {
				continue;
			}

			for (int j = 0; j < row.getLastCellNum(); j++) {
				Cell c = row.getCell(j);
				if (c == null) {
					continue;
				}
				String v = POIUtil.getCellValue(c);
				if (StringUtils.isBlank(v)) {
					continue;
				}

				/*
				 * 替換排序替换符置空
				 */
				if (v.matches(R.Layer.ORDER_BY_REGEX)) {
					c.setCellValue("");
					continue;

				}
				/*
				 * 替換where替换符置空
				 */
				if (v.matches(R.Layer.WHERE_BY_REGEX)) {
					c.setCellValue("");
					continue;
				}
				/*
				 * 替換groupBy替换符置空
				 */
				if (v.matches(R.Layer.GROUP_BY_REGEX)) {
					c.setCellValue("");
					continue;

				}

				// 替换所有系统参数
				v = RegexUtil.takeOffSysRegex(v, this.param, false);
				/*
				 * 替换UI参数
				 */
				v = RegexUtil.takeOffUIRegex(v, this.param, false);
				/*
				 * 替換分页属性的同胞参数
				 */
				v = RegexUtil.takeOffPagingRegex(v, this.pagingParamValues);

				/*
				 * 此时依然能够匹配的便是无法处理的替换符了
				 */
				if (v.matches(".*" + R.OBJECT_PROPERTY_REGEX + ".*")) {
					v = "";
				}
				/*
				 * 将剩下的替換同胞属性替换符置空 （这些都是无效的，可能是有雨数据集是空的导致sheet无法渲染）
				 */
				if (v.matches(R.SHEET_REGEX)) {
					v = "";

				}
				c.setCellValue(v);
			}
		}

	}

	/**
	 * 清空上一页缓存，便于下一页使用 为了处理不同分页使用同一结构时，出现缓存的现象
	 */
	public SheetDealer clearCache() {
		for (Layer layer : this.param.layers.values()) {
			layer.clear();
		}
		return this;
	}

	/**
	 * 将sheet变成blank sheet
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月4日 上午9:07:47
	 */
	public static class BlankSheet {
		private String msg;
		private Sheet sheet;

		public BlankSheet(String msg, Sheet sheet) {
			this.msg = msg == null ? "" : msg;
			this.sheet = sheet;
			this.makeAllCellsBlank();
		}

		/**
		 * 使sheet内部所有的单元格的值，将带有替换符的都变为空
		 */
		private void makeAllCellsBlank() {
			int rowNum = sheet.getLastRowNum();
			for (int i = 0; i < rowNum; i++) {
				Row r = POIUtil.getOrCreateRow(sheet, i);

				int colNum = r.getRowNum();
				for (int j = 0; j <= colNum; j++) {
					Cell cell = POIUtil.getOrCreateCell(r, j);

					String v = POIUtil.getCellValue(cell);

					if (!StringUtils.isBlank(v) && v.matches(R.OBJECT_PROPERTY_REGEX)) {
						cell.setCellValue("");
					}
					if (i == 0 && j == 0) {// 将错误信息显示在第一个单元格内
						cell.setCellValue(msg);
					}
				}
			}

		}

	}

}
