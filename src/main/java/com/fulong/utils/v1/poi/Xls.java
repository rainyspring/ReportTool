package com.fulong.utils.v1.poi;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

/**
 * 专门处理xlsx格式的97-2003的Excel文件 不计算公式
 * @deprecated -> ExcelHelper
 * @author 姜旭
 * 
 */
@Deprecated
public class Xls {
	/**
	 * Excel 公式的正则
	 */
	private static Pattern pattern = Pattern
			.compile("^[a-zA-Z]+\\(([A-Z]+[0-9]+):([A-Z]+[0-9]+)\\)$");

	/**
	 * 计算公式
	 * 
	 * @param wb
	 * @param cell
	 * @return
	 */
	public static String getCellFormatValue(Workbook wb, HSSFCell cell) {
		HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
		String cellvalue = null;
		if (cell != null) {

			switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_STRING: {
				cellvalue = cell.getRichStringCellValue().getString();
				break;
			}

			case HSSFCell.CELL_TYPE_NUMERIC: {
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

					cellvalue = format.format(cell.getDateCellValue());

				} else {

					double d = cell.getNumericCellValue();
					if (d - (int) d < Double.MIN_VALUE) {

						cellvalue = Integer.toString((int) d);

					} else {
						cellvalue = Long.toString(new Double(cell
								.getNumericCellValue()).longValue());

					}

				}
				break;
			}
			case HSSFCell.CELL_TYPE_FORMULA: {
				// Date
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					if (cell.getDateCellValue() == null
							|| "".equals(cell.getDateCellValue())) {
						return "";
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
					cellvalue = sdf.format(cell.getDateCellValue());

				} else {

					if (cell.getDateCellValue() == null
							|| "".equals(cell.getDateCellValue())) {
						return "";
					}
					cellvalue = String.valueOf(eval.evaluateInCell(cell));
				}

				break;
			}
			default:
				cellvalue = "";

			}
		} else {
			cellvalue = "";
		}
		return cellvalue.trim();
	}

	/**
	 * 单元格复制
	 * 
	 * @param srcCell
	 * @param distCell
	 * @param formulaChanged
	 *            [true:代表公式要自动平移；false:原样复制]
	 */
	public static void copyCell(HSSFCell srcCell, HSSFCell distCell,
			boolean formulaChanged) {
		// 复制样式
		distCell.setCellStyle(srcCell.getCellStyle());

		if (srcCell.getCellComment() != null) {
			distCell.setCellComment(srcCell.getCellComment());
		}
		int srcCellType = srcCell.getCellType();
		distCell.setCellType(srcCellType);

		if (srcCellType == HSSFCell.CELL_TYPE_NUMERIC) {
			if (HSSFDateUtil.isCellDateFormatted(srcCell)) {
				distCell.setCellValue(srcCell.getDateCellValue());
			} else {
				distCell.setCellValue(srcCell.getNumericCellValue());
			}
		} else if (srcCellType == HSSFCell.CELL_TYPE_STRING) {
			distCell.setCellValue(srcCell.getRichStringCellValue());
		} else if (srcCellType == HSSFCell.CELL_TYPE_BLANK) {
			// nothing
		} else if (srcCellType == HSSFCell.CELL_TYPE_BOOLEAN) {
			distCell.setCellValue(srcCell.getBooleanCellValue());
		} else if (srcCellType == HSSFCell.CELL_TYPE_ERROR) {
			distCell.setCellErrorValue(srcCell.getErrorCellValue());
		} else if (srcCellType == HSSFCell.CELL_TYPE_FORMULA) {// 公式
			if (formulaChanged) {
				distCell.setCellFormula(srcCell.getCellFormula());
				String oldFormula = srcCell.getCellFormula();
				System.out.println("-旧的公式--" + oldFormula);
				
				Matcher matcher = pattern.matcher(oldFormula);
				if (matcher.find()) {
					String group1 = matcher.group(1);
					String group2 = matcher.group(2);

					// 获取目标单元格索引
					CellReference re = new CellReference(distCell);
					System.out.println("获取目标单元格索引:" + re.formatAsString());
					// 提取英文部分
					String reStr = re.formatAsString().replaceAll("[0-9]+", "");
					// 将旧的串的英文部分换成新的
					String new_group1 = group1.replaceAll("[A-Z]+", reStr);
					String new_group2 = group2.replaceAll("[A-Z]+", reStr);

					// 将新的串放回公式里，作为平移后的新公式
					String new_formula = oldFormula.replaceAll(group1,
							new_group1).replaceAll(group2, new_group2);
					System.out.println("--新的公式--" + new_formula);
					distCell.setCellFormula(new_formula);
				}

			} else {
				distCell.setCellFormula(srcCell.getCellFormula());

			}
		} else {
			// nothing
		}
	}

	/**
	 * 通过POI的color对象转成html的RGB模式的十六进制色彩字符串:如#AAFF98
	 * 
	 * @param color
	 * @return
	 */
	public static String getRGBHexString(HSSFColor color) {
		short[] t = color.getTriplet();
		String a = Integer.toHexString(t[0]);
		String b = Integer.toHexString(t[1]);
		String c = Integer.toHexString(t[2]);
		if (a.length() == 1) {
			a = "00";
		}
		if (b.length() == 1) {
			b = "00";
		}
		if (c.length() == 1) {
			c = "00";
		}
		return "#" + a + b + c;

	}


}
