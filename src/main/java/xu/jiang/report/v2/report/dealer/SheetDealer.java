package xu.jiang.report.v2.report.dealer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;

import xu.jiang.report.Param;
import xu.jiang.report.v2.report.bean.Layer;
import xu.jiang.report.v2.report.tool.POIUtil;
import xu.jiang.report.v2.report.tool.R;
import xu.jiang.report.v2.report.tool.RegexUtil;

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
	 * 模板的sheet名称的根值
	 * 根值仅仅代表客户希望的名称，但现实很残酷，Excel对字符长度和种类都有限制.
	 * Excel中sheet命名有如下规则：
	 *（1）sheet名称不能多于31个（包含英文、汉字、| 、（）等，但是不能包含： 、/、？、*、[]等 )，
	 * 程序中使用poi工具来生成的时候，传进去大于31个长度的字符串时，会被自动截取，
	 * 便会导致两个名字变为一样的，出现sheet同名异常
	 * （2）sheet名字不能为空，如果是null 或者""也会报错。
	 */
	private String sheetNameRoot = null;
	/**
	 * 真正的sheet名称
	 */
	private String realSheetName = null;
	/*
	 * 当sheetDealer内部Layer获取的数据只需一个sheet就能装下时，这个值没用。
	 * 但当layer获取数据超出某些限制，比如A4值高度，即需要再次分页，还需要生成多个sheet，
	 * 此时的sheet名称需要再原名称的基础上追加realSheetNameIndex即可
	 */
	private int realSheetNameIndex = 1;

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

		// 为sheet页签命名,默认123456...
		this.realSheetName = this.getRealSheetName();
		POIUtil.renameSheet(modelsheetOfMine,this.realSheetName);


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
	 * 获取sheet的真正名称
	 */
	private String getRealSheetName() {
		if(StringUtils.isBlank(this.sheetNameRoot)) {
			return String.valueOf(this.indexSheet+1);
		}
		String name = this.sheetNameRoot.replaceAll("[\\/?*\\[\\]]", "-");
		
		if(name.length()>=25) {
			name = name.substring(0, 25);
		}
		return name;
		
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
		

		/**
		 * 使sheet内部所有的单元格的值，将带有替换符的都变为空
		 */
		public static Sheet makeAllCellsBlank(String msg, Sheet sheet) {
			int rowNum = sheet.getLastRowNum();
			for (int i = 0; i < rowNum; i++) {
				Row r = POIUtil.getOrCreateRow(sheet, i);

				int colNum = r.getRowNum();
				for (int j = 0; j <= colNum; j++) {
					Cell cell = POIUtil.getOrCreateCell(r, j);

					String v = POIUtil.getCellValue(cell);

					if (i == 0 && j == 0) {// 将错误信息显示在第一个单元格内,便于显示提醒用户为什么是白板
						cell.setCellValue(msg);
					}
					
					if (!StringUtils.isBlank(v) && v.matches(R.OBJECT_PROPERTY_REGEX)) {
						cell.setCellValue("");
					}
					
				}
			}
			
			return sheet;

		}

	}

	public String getSheetNameRoot() {
		return sheetNameRoot;
	}

	public void setSheetNameRoot(String sheetNameRoot) {
		this.sheetNameRoot = sheetNameRoot;
	}

}
