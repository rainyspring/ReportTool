package com.fulong.utils.v2.report.dealer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;

import com.fulong.utils.v2.bean.KeyValue;
import com.fulong.utils.v2.error.MyReportException;
import com.fulong.utils.v2.poi.DomPOI;
import com.fulong.utils.v2.report.Param;
import com.fulong.utils.v2.report.bean.BaseLayer;
import com.fulong.utils.v2.report.bean.GroupLayer;
import com.fulong.utils.v2.report.bean.LoopLayer;
import com.fulong.utils.v2.report.bean.RangeCell;
import com.fulong.utils.v2.report.dto.LayerDTO;
import com.fulong.utils.v2.report.dto.MilepostDTO;
import com.fulong.utils.v2.report.tool.MergingCellUtil;
import com.fulong.utils.v2.report.tool.POIUtil;
import com.fulong.utils.v2.report.tool.R;
import com.fulong.utils.v2.report.tool.RegexUtil;
import com.fulong.utils.v2.tool.BooleanMsg;
import com.fulong.utils.v2.tool.DataHelper;
import com.fulong.utils.v2.tool.PathUtil;
import com.fulong.utils.v2.tool.SQLUtil;
import com.fulong.utils.v2.tool.file.FileUtil;

import net.sf.json.JSONObject;

/**
 * 报表处理总入口
 * 
 * @see
 * @author jx
 * @date 2017年4月26日 下午2:10:15
 */
public class ReportDealer {

	/**
	 * 这个贯穿全剧的公共参数 存储了每个模块都会使用的参数
	 */
	private final Param param;

	private Session session;

	private Workbook workbook;
	private File outputFile;

	/**
	 * 报表输出路径
	 */
	private Path outDir = null;
	private boolean isMilepost = false;

	/**
	 * 模板的sheet名称
	 * Excel中sheet命名有如下规则：
	 *（1）sheet名称不能多于31个（包含英文、汉字、| 、（）等，但是不能包含： 、/、？、*、[]等 )，
	 * 程序中使用poi工具来生成的时候，传进去大于31个长度的字符串时，会被自动截取，
	 * 便会导致两个名字变为一样的，出现sheet同名异常
	 * （2）sheet名字不能为空，如果是null 或者""也会报错。
	 */
	private String sheetname_modelSheet = null;

	
	public ReportDealer(Session session, Param param) throws Exception {
		this.param = param;
		this.session = session;

	}

	/**
	 * 开始执行
	 * 
	 * @throws Exception
	 * @throws Exception
	 */
	public File start() throws Exception {
		try {

			// 读取Excel模板
			this.workbook = DomPOI.readExcel(this.param.templet);

			// 默认获取第一个sheet作为模板sheet,并保存到公共变量param里
			this.param.mainSheet = workbook.getSheetAt(0);
			/*
			 * 将模板固化
			 */
			new Painter(this.param.uiParams, this.param.mainSheet).draw();

			// 初始化里程碑信息
			if (isMilepost) {

				this.initMilepostInfo();
			}
			/*
			 * 解析模板参数
			 */
			new ParamAnalyzer(this.param).filterMergingByLayer().filterGroup4Layer();
			/*
			 * 分页处理，一个sheet算作一页
			 */
			PagingAnalyzer pagingAnalyzer = new PagingAnalyzer(this.param);

			List<SheetDealer> sheets = pagingAnalyzer.page2Sheet(this.session);
			// 循环处理每个sheet
			for (SheetDealer sheetDealer : sheets) {
				// 先清理上一页的缓存，在处理下一页
				sheetDealer.clearCache().runing(this.session);
			}
			// 删除主模板....
			this.workbook.removeSheetAt(0);
			/*
			 * 如果没有模板了，要留个空白页
			 */
			if (sheets.isEmpty()) {
				this.workbook.createSheet("no data");
			}
			System.out.println("恭喜您!!");

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			String msg = DataHelper.filterValueBy(e.getMessage());
			// 用空串替换所有的替换符，
			// 并将错误信息显示在文件的第一个空白单元格中
			SheetDealer.BlankSheet.makeAllCellsBlank(msg,
					POIUtil.renameSheet(this.workbook.cloneSheet(0), SheetDealer.DEFAULT_SHEET_NAME));
			
			e.printStackTrace();
		} finally {
			this.finishInServer();
		}
		return this.outputFile;

	}

	/**
	 * 将里程碑的id和标题都初始化到公共参数param中 因为这是固定值，提前加载好后方便以后读取， 这样就不必要传session了
	 */
	private void initMilepostInfo() {
		String hql_ps_yz = "select new com.fulong.utils.report.dto.MilepostDTO(m.milepostID,m.milepostName)  from Milepost m where  m.milepostClass.milepostClassID='PipeSection_DL' order by m.milepostOrderIndex asc ";
		String hql_ps_xc = "select new com.fulong.utils.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='PipeInstall_DL' order by m.milepostOrderIndex asc ";
		String hql_ps = "select new com.fulong.utils.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='PipeSection_DL' or  m.milepostClass.milepostClassID='PipeInstall_DL'  order by m.milepostOrderIndex asc ";
		String hql_wl = "select new com.fulong.utils.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='WeldLine_DL' order by m.milepostOrderIndex asc";

		this.param.milepost_ps_yz = session.createQuery(hql_ps_yz).list();
		this.param.milepost_ps_xc = session.createQuery(hql_ps_xc).list();
		this.param.milepost_ps = session.createQuery(hql_ps).list();
		this.param.milepost_wl = session.createQuery(hql_wl).list();

	}

	/**
	 * 模板绘制器(主要解决ui.layer重绘模板的需求) 在读取模板内容之前，先绘制模板内容
	 * 
	 * @see
	 * @author jx
	 * @date 2017年6月1日 上午11:58:59
	 */
	private class Painter {
		private final Map<String, String> uiParams;
		private final Sheet model;
		private static final String KEY = "ui.layer";
		/**
		 * 考虑Excel的页签字数长度受限，无法写入复杂语句，故启用该属性，涂改模板默认页签值
		 */
		private static final String COMPLEX_PAGE_COLUMN = "ui.complexPageColumn";

		public Painter(Map<String, String> uiParams, Sheet model) {
			this.uiParams = uiParams;
			this.model = model;
		}

		/**
		 * 模板绘制器 在读取模板内容之前，先绘制模板内容
		 * 
		 * @see
		 * @author jx
		 * @date 2017年6月1日 上午11:58:59
		 */
		public void draw() {

			if (uiParams == null || uiParams.size() <= 0) {
				return;
			}
			/*
			 * 更改模板页签默认值
			 */
			String customPageColumn = uiParams.get(COMPLEX_PAGE_COLUMN);
			if (!StringUtils.isBlank(customPageColumn)) {
				/*
				 * 经过测试，字段太长还是无法写入，故废弃
				 */
				// POIUtil.renameSheet(this.model,customPageColumn);
				sheetname_modelSheet = customPageColumn;
				// 移除complexPageColumn
				this.uiParams.remove(COMPLEX_PAGE_COLUMN);
			}

			/*
			 * 变更模板动态layer
			 */
			String jsonStr = uiParams.get(KEY);
			if (StringUtils.isBlank(jsonStr)) {
				return;
			}

			// init params
			Map<String, Class> classMap = new HashMap<String, Class>();
			classMap.put("columns", KeyValue.class);

			JSONObject json = JSONObject.fromObject(jsonStr);
			LayerDTO layerDto = (LayerDTO) JSONObject.toBean(json, LayerDTO.class, classMap);

			int index = layerDto.getIndex() < 0 ? 0 : layerDto.getIndex();
			// 绘制
			// 遍历整个主模板Sheet

			for (int i = 0; i <= index; i++) {// 遍历行

				Row r = POIUtil.getOrCreateRow(this.model, i);
				if (i != index) {
					continue;
				}
				Row nextR = POIUtil.getOrCreateRow(this.model, i + 1);
				/*
				 * 将列信息追加到该行中
				 */
				for (int j = 0; j < layerDto.getColumns().size(); j++) {// 遍历单元格

					KeyValue v = layerDto.getColumns().get(j);
					Cell c = POIUtil.getOrCreateCell(r, j);
					Cell downC = POIUtil.getOrCreateCell(nextR, j);
					if (StringUtils.isNotBlank(v.key)) {
						c.setCellValue(v.key);
					}
					if (StringUtils.isNotBlank(v.value)) {
						downC.setCellValue(v.value);
					}

				}
			}

			// 移除key
			this.uiParams.remove(KEY);

		}

	}

	/**
	 * 参数解析器 识别模板参数和提取分层的功能 统一遍历一次主模板，找到分页参数，且如果存在层，将层提取出来
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月5日 下午3:20:38
	 */
	public static class ParamAnalyzer {
		private final Param param;
		// private final Session session;
		/**
		 * 存储分页属性的同胞属性
		 */
		// public final Set<String> pagingBrothersColumns = new
		// HashSet<String>();
		/**
		 * 简单的模板解析结构 内容：记录所有的层（层行号,层内值的集合），全部存储，先不区分是组还是单层(循环层或汇总层)
		 */
		private TreeMap<Integer, LoopLayer> simpleLayers = new TreeMap<Integer, LoopLayer>();

		/**
		 * simpleLayers经过了组过滤后的数据
		 */
		private TreeMap<Integer, BaseLayer> filteredLayers = new TreeMap<Integer, BaseLayer>();
		/**
		 * 范围形式的Report级别参数
		 */
		private RangeParam rangeParamAnalyzer = null;

		/**
		 * 任务有2个： 1 找到分页属性的同胞属性名称 2 找到所有的层或组
		 */
		public ParamAnalyzer(Param param) throws Exception {

			this.param = param;
			// this.session = session;
			this.rangeParamAnalyzer = new RangeParam(this.param);
			this.init();
		}

		private void init() throws Exception {

			// 遍历整个主模板Sheet
			int lastR = this.param.mainSheet.getLastRowNum();

			for (int i = 0; i <= lastR; i++) {// 遍历行

				Row r = this.param.mainSheet.getRow(i);
				if (r == null) {// blank row ,pass it !!
					continue;
				}

				// 存储层的属性
				List<String> columns = new LinkedList<String>();
				// 临时存储合并单元格,不重复
				Set<CellRangeAddress> mergingColumns = new HashSet<CellRangeAddress>();

				boolean isLayer = false;
				boolean isGroup = false;

				for (int j = 0; j < r.getLastCellNum(); j++) {// 遍历单元格

					Cell c = POIUtil.getOrCreateCell(r, j);// cell肯定不是null
					String cellValue = POIUtil.getCellValue(c);// cellValue肯定不是null
					/*
					 * 将层放入这一层的列中
					 * 
					 */
					columns.add(cellValue);

					// 保存合并单元格
					BooleanMsg<RangeCell> status = MergingCellUtil.isMergingCell(c);
					if (status.isOk()) {
						if (!MergingCellUtil.exists(mergingColumns, status.otherObject.range)) {
							mergingColumns.add(status.otherObject.range);

						}
					}

					if (StringUtils.isBlank(cellValue)) {
						continue;
					}
					cellValue = cellValue.trim();

					// 是否为分页属性的同胞属性
					if (cellValue.matches(".*" + R.SHEET_REGEX + ".*")) {

						this.param.pagingColumnContainer.add(cellValue);

					}

					// 是否为组内层
					if (cellValue.matches(R.Report.GROUP_REGEX_COMPLETE)) {
						isGroup = true;

					}

					if (cellValue.matches(R.Report.RANGE_REGEX)) {

						rangeParamAnalyzer.work(c);

					}

					// 是否为普通替换符（可能是循环层或统计层）
					if (cellValue.matches(".*" + R.OBJECT_PROPERTY_REGEX + ".*")) {
						/*
						 * 单元格的内容全部存储起来，显然这里可能包含其他替换符和运算符
						 */
						isLayer = true;
					}

				}
				/*
				 * 只将是层的属性封装为层结构
				 */
				if (isLayer) {// 将该层的属性的值添加到Layers中

					LoopLayer layer = new LoopLayer(i, columns, mergingColumns, this.param);

					if (isGroup) {
						layer.remarkGroup();// 标记为组内层
					}

					this.simpleLayers.put(i, layer);
				}

			}

		}

		/**
		 * 先处理合并单元格，在处理组 (目前合并单元格暂不处理，故这里先把流程的位置摆上，没做什么特别的事)
		 * 
		 * @return
		 */
		public ParamAnalyzer filterMergingByLayer() {

			// 都是单独层
			for (LoopLayer layer : this.simpleLayers.values()) {
				this.filteredLayers.put(layer.getIndex(), layer);
			}

			return this;
		}

		/**
		 * 对层集合进行格式化 内容：对filteredLayers进行统一的进一步处理， 为SheetDealer实际解析sql时提供方便
		 * 1将组中的层重新封装:GroupLayer 2 将合并单元格涉及的层放在一起:MergingLayer 3
		 * 其他的层不变:LoopLayer
		 */
		public ParamAnalyzer filterGroup4Layer() {
			if (this.filteredLayers.size() <= 0) {
				return this;
			}

			TreeSet<BaseLayer> treeSet = null;
			for (Entry<Integer, BaseLayer> r : this.filteredLayers.entrySet()) {

				BaseLayer layer = r.getValue();

				if (treeSet == null) {// 第一次访问
					treeSet = new TreeSet<BaseLayer>();
					treeSet.add(layer);
				} else if (layer.isGroup()) {
					treeSet.add(layer);
				} else {// 不是组内层，则将之前的层封装
					this.saveGroupOrLayer(treeSet);
					treeSet = new TreeSet<BaseLayer>();
					treeSet.add(layer);
				}

				// 上面的逻辑会剩下最后一个group
				this.saveGroupOrLayer(treeSet);

			}
			return this;
		}

		/**
		 * 将临时生成的组或单独层保存在公共参数param中
		 * 
		 * @param treeSet
		 */
		private void saveGroupOrLayer(TreeSet<BaseLayer> treeSet) {
			if (treeSet == null) {
				return;
			}

			// TODO Auto-generated method stub
			if (treeSet.size() == 1) {
				BaseLayer singerLayer = treeSet.first();
				this.param.layers.put(singerLayer.getIndex(), singerLayer);

			} else if (treeSet.size() > 1) {
				int groupIndex = treeSet.first().getIndex();
				GroupLayer group = new GroupLayer(groupIndex, treeSet, this.param);
				this.param.layers.put(groupIndex, group);

			}
		}

		/**
		 * 范围形式的Report级别参数
		 * 
		 * @see
		 * @author jx
		 * @date 2017年5月17日 上午9:33:50
		 */
		public class RangeParam {

			private final Param param;

			public RangeParam(Param param) {
				this.param = param;
			}

			/**
			 * 核心处理逻辑
			 * 
			 * @param mainValue
			 *            去掉范围后的真正实体串， 即{xxx}[1~9]-->{xxx}
			 *            或{xxx}(1~9)[3,9,8]-->{xxx}
			 * @param pushMerging
			 *            小括号的部分,形如(0~9)或(2,9,8)
			 * @param pushCoping
			 *            中括号的部分,形如[0~9]或[2,9,8]
			 * @throws Exception
			 */
			public void work(Cell c) throws Exception {

				// Sheet s = c.getSheet();
				// Row r = c.getRow();
				// 获取值
				String cellValue = POIUtil.getCellValue(c);

				String mainValue = cellValue.replaceAll(R.Report.RANGE_PIECE_PUSH_COPYING, "")
						.replaceAll(R.Report.RANGE_PIECE_PUSH_MERGING, "");

				String range = cellValue.replace(mainValue, "");

				// 平移复制 的数字范围
				List<Integer> nums_pushCopying = RegexUtil
						.analyzeIntegers(range.replaceAll(R.Report.RANGE_PIECE_PUSH_MERGING, ""));
				// 平移合并的数字范围
				List<Integer> nums_pushMerging = RegexUtil
						.analyzeIntegers(range.replaceAll(R.Report.RANGE_PIECE_PUSH_COPYING, ""));

				// 是否存在带范围的管段动态里程碑标题
				if (mainValue.matches(R.Report.MILEPOST_PS_NAME_PIECE)) {
					String pipeSecPosId = this.param.uiParams.get("ui.pipeSecPosId");

					List<MilepostDTO> list = null;
					if ("Shop".equalsIgnoreCase(pipeSecPosId)) {
						list = this.param.milepost_ps_yz;

					} else if ("Field".equalsIgnoreCase(pipeSecPosId)) {
						list = this.param.milepost_ps_xc;

					} else {
						list = this.param.milepost_ps;

					}
					// 标题名称
					List<String> milepostNames = new ArrayList<String>();
					List<String> milepostIds = new ArrayList<String>();
					for (MilepostDTO m : list) {
						milepostNames.add(m.getMilepostName());
						milepostIds.add(m.getMilepostId());
					}

					// 先处理pushMerging,因为替换符本身占据一个cell，故需要新增的blankCell应该减一
					putBlankCellAndMerge4Rows(c, nums_pushMerging, milepostNames.size() - 1);
					// 处理本行的值，边推边插即可
					POIUtil.insertCellAndPutValue(c, milepostNames);
					// 再处理pushCopying
					replaceDynamicValue4RowsByRegex(c, nums_pushCopying, R.SRING_DYNAMIC_CURRENT_MILEPOST, milepostIds,
							false);

				}
				// 是否存在带范围的焊缝动态里程碑标题
				else if (mainValue.matches(R.Report.MILEPOST_WL_NAME_PIECE)) {

					// 标题名称
					List<String> milepostNames = new ArrayList<String>();
					List<String> milepostIds = new ArrayList<String>();
					for (MilepostDTO m : this.param.milepost_wl) {
						milepostNames.add(m.getMilepostName());
						milepostIds.add(m.getMilepostId());
					}

					// 先处理pushMerging,因为替换符本身占据一个cell，故需要新增的blankCell应该减一
					putBlankCellAndMerge4Rows(c, nums_pushMerging, milepostNames.size() - 1);
					// 处理本行的值，边推边插即可
					POIUtil.insertCellAndPutValue(c, milepostNames);

					// 再处理pushCopying
					replaceDynamicValue4RowsByRegex(c, nums_pushCopying, R.SRING_DYNAMIC_CURRENT_MILEPOST, milepostIds,
							false);

				}
				// 形如：{ui.weldedDate_start~ui.weldedDate_end}(10~19)
				else if (mainValue.matches(R.Report.UI_WELDED_DATE_RANGE_PIECE)) {

					// this.replaceDynamicWeldedDate(c);

					String weldedDate_start = this.param.uiParams.get("ui." + R.SRING_START_DATE);
					String weldedDate_end = this.param.uiParams.get("ui." + R.SRING_END_DATE);

					List<String> dates = ReportDealer.getRangeDate(weldedDate_start, weldedDate_end);

					// 先处理pushMerging,因为替换符本身占据一个cell，故需要新增的blankCell应该减一
					putBlankCellAndMerge4Rows(c, nums_pushMerging, dates.size() - 1);
					// 平移并插入本行
					POIUtil.insertCellAndPutValue(c, dates);
					// 再处理pushCopying
					replaceDynamicValue4RowsByRegex(c, nums_pushCopying, R.SRING_DYNAMIC_CURRENT_DATE, dates, true);
				}
			}

		}

	}

	/**
	 * 分页解析器
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月4日 下午5:39:42
	 */
	public class PagingAnalyzer {

		private final Param param;

		// 分页对象
		private String objectName = null;
		// 分页DB视图
		private String view4DB = null;
		
		private int pageIndex = 0;

		public PagingAnalyzer(Param param) {
			this.param = param;
			// this.pagingBrothersColumns = pagingColumns;
		}

		/**
		 * 循环替换分页参数， 通过分页替换符将每个sheet页替换为能独立处理的sheet，替换完毕后，每个sheet与sheet参数无关
		 * 
		 * @param param
		 * @param pagingBrothersColumns
		 * @throws IOException
		 */
		public List<SheetDealer> page2Sheet(Session session) throws MyReportException, IOException {

			/*
			 * 检测是否已经获取了模板sheet名称
			 * 当sheetname_modelSheet不为空时，说明ui.complexPageColumn涂改了模板初始sheetname；
			 * 原因：由于Excel的sheetName有字数限制，建议采用ui.complexPageColumn彻底涂改模板最初的默认值
			 * 这个where部分是要拼凑在分页sql中的 sheetname_modelSheet形如：{w.a},{w.b}<{w.b} is
			 * null>
			 */
			if (StringUtils.isBlank(sheetname_modelSheet)) {

				sheetname_modelSheet = this.param.mainSheet.getSheetName();
			}

			if (!sheetname_modelSheet.matches(R.OBJECT_PROPERTY_REGEX + ".*")) {// 没有分页

				return this.getSheets4noPaging();
			}
			// -------解析sheetname------start--------------------------

			/*
			 * 实际存储分页属性 ，初始时给与默认值sheetname_modelSheet（形如{w.a},{w.b}<{w.b} is
			 * null>） 故我们后面要去掉<>后面部分，且分页属性可能包含多个分页属性，逗号分隔
			 */
			String keys4Sheet = sheetname_modelSheet;

			// 准备获取分页属性后面的sql部分，即<>部分，这部分是分页的where语句
			String pagingSql4WherePiece = "";
			// 判断是否存在where语句的条件：即存在<>
			if (sheetname_modelSheet.contains("<") && sheetname_modelSheet.contains(">")) {
				// 已经去除where部分的分页属性字符串
				keys4Sheet = sheetname_modelSheet.substring(0, sheetname_modelSheet.indexOf("<"));
				pagingSql4WherePiece = sheetname_modelSheet.replace(keys4Sheet, "");
				// 去掉首尾的<>
				pagingSql4WherePiece = pagingSql4WherePiece.substring(1, pagingSql4WherePiece.length() - 1);
				//剥去替换符外衣，变成真正的wheresql
				pagingSql4WherePiece = " and " + this.takeOffRegex(pagingSql4WherePiece, param);
			}
			// ------解析sheetname------end----------------

			String sheetNameKey = null;
			// 将包含多值的分页属性分隔开来
			String[] array4Key = keys4Sheet.split(",");
			int length = array4Key.length;
			for (int i = 0; i < length; i++) {
				String pageColumn = array4Key[i];

				// 放入分页属性集合中(这里将分页属性的形如{ne.code}->{sheet.code}的原因是避免后期存在重复的{sheet.code},属性重复会造成sql错误)
				this.param.pagingColumnContainer.add(pageColumn.replaceAll(R.OBJECT_PROPERTY_REGEX_CONTENT_PREFIX,
						R.SHEET_REGEX_CONTENT_PREFIX_STRING));

				// 存储分页时使用的对象
				if (i == 0) {
					// 剥去外衣后的值：{w.code}->w.code
					String pureColumn = RegexUtil.takeOffPropertyRegex(pageColumn);
					String[] tempValue = pureColumn.split("\\.");
					// 取第一个值来获取分页对象名称
					this.objectName = tempValue[0];
				}
				//默认去最后一个作为分页名称的默认根值
				if(i==length-1) {
					String pureColumn = RegexUtil.takeOffPropertyRegex(pageColumn);
					String[] tempValue = pureColumn.split("\\.");
					sheetNameKey = tempValue[1];
				}
			}

			// 找到对应的真实的数据库表名或视图（去属性文件里找到对应关系）
			this.view4DB = LoopLayer.getRealViewName(this.objectName);
			// this.view4DB = this.view4DB.contains(" ")?" ("+this.view4DB+")
			// ":this.view4DB;

			// 获取分页数据
			List<Map<String, String>> dataList_paging = this.getPagingData(session, pagingSql4WherePiece);
			if (dataList_paging == null || dataList_paging.size() <= 0) {// 没数据，那么没有分页；
				return getBlankSheet();
			}

			// 标记分页，这回真的确定要分页了
			this.param.isPaging = true;

			List<SheetDealer> sheetDealers = new ArrayList<SheetDealer>();


			// List<String> pageColumns = this.param.pagingColumnContainer.getOrInitializeCachePigingColumns();
			// 获取每一页的数据集,每一页就是一个sheet
			for (; pageIndex < dataList_paging.size(); pageIndex++) {
				
				Map<String, String> pagingParamValues = dataList_paging.get(pageIndex);
				
				
				// test
//				for (Map.Entry<String, String> entry : pagingParamValues.entrySet()) {
//					System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//				}

				/*
				 * 创建sheetDealer，并添加到集合中 注意：sheetDealer中的模板是主模板的克隆sheet
				 */
				SheetDealer sheetDealer = new SheetDealer(pageIndex, ReportDealer.cloneSheet(this.param.mainSheet), pagingParamValues,
						this.param);
				sheetDealer.setSheetNameRoot(pagingParamValues.get(sheetNameKey));
				sheetDealers.add(sheetDealer);
			}

			return sheetDealers;
		}

		/**
		 * 没有分页
		 * 
		 * @return 返回没有分页的Report的sheet集合
		 */
		private List<SheetDealer> getSheets4noPaging() {
			/*
			 * 更加模板克隆一个新的模板作为数据填写的蓝本
			 */
			SheetDealer oneSheet = new SheetDealer(pageIndex, ReportDealer.cloneSheet(this.param.mainSheet), null, this.param);
			List<SheetDealer> olist = new ArrayList<SheetDealer>();
			olist.add(oneSheet);
			return olist;
		}
		
		/**
		 * 没有分页
		 * 
		 * @return 返回没有分页的Report的sheet集合
		 */
		private List<SheetDealer> getBlankSheet() {
			ArrayList<SheetDealer> blankSheets = new ArrayList<SheetDealer>(1);
			
			Sheet sheet = ReportDealer.cloneSheet(this.param.mainSheet);
			//插入空模板
			Sheet blankSheet  = SheetDealer.BlankSheet.makeAllCellsBlank("(以下是空白行)", sheet);
			blankSheets.add(new SheetDealer(pageIndex,blankSheet, null, this.param));
			return blankSheets;
		}

		/**
		 * 获取分页数据
		 * 
		 * @param session
		 * @param sql
		 * @return
		 * @throws MyReportException
		 */
		private List<Map<String, String>> getPagingData(Session session, String whereOfGroup) throws MyReportException {
			/*
			 * 将同胞属性集合拼凑成sql的select片段
			 */
			List<String> allPagingColumns = this.param.pagingColumnContainer.getOrInitializeCachePigingColumns();
			String[] arr_columns = allPagingColumns.toArray(new String[allPagingColumns.size()]);
			String partOfSql4names = StringUtils.join(arr_columns, ",");

			/*
			 * 模板上的分页属性和UI参数匹配时的where部分
			 */
			StringBuffer sql_param_paging = new StringBuffer();

			// 如果分页属性在ui参数中存在值，那么应该将分页属性的ui值也放入where部分，这里我们使用in语句实现
			List<String> pagingColumnsOfFiltering = this.param.pagingColumnContainer
					.getOrInitializeCachePigingColumnsOfwhereSQL4layer();
			for (String column : pagingColumnsOfFiltering) {

				// 找到ui中的where部分
				String pageColumn4UI = "ui." + column;
				String param_paging4Where = this.param.uiParams.get(pageColumn4UI);

				if (!StringUtils.isBlank(param_paging4Where)) {
					sql_param_paging.append(" and " + column + " in("
							+ SQLUtil.filter2SqlOfIn(Arrays.asList(param_paging4Where.split(","))) + ")");
					// 必须移除被分页属性使用过的ui属性
					this.param.uiParams.remove(pageColumn4UI);
				}
			}

			/*
			 * 形如：select distinct a.A,a.B from view a group by a.A,a.B
			 * 第一个值默认为分页属性，后面的为同胞属性 由于分页属性可能不是对象的主键，select后可能存在重复值，
			 * 而distinct无法对多个属性操作，故采用group by
			 * 
			 * 之所以增加个冗余列xyz123，是为了hibernate的数据集大于1个， 这样就可以统一用List<Object[]>接收了
			 * 
			 */
			StringBuffer sql = new StringBuffer("select distinct " + partOfSql4names + ",'' as xyz123  from "
					+ this.view4DB + " " + this.objectName);

			/*
			 * where部分
			 */
			sql.append(" where 1=1 " + sql_param_paging.toString() + whereOfGroup);
			// UI端的 where by部分
			String whereBySql = this.param.uiParams.remove("ui." + R.Sheet.WHERE_SQL);
			if (!StringUtils.isBlank(whereBySql)) {

				sql.append(" and " + this.takeOffRegex(whereBySql, param));

			}

			/*
			 * group by部分
			 */
			String groupBySql = this.param.uiParams.remove("ui." + R.Sheet.GROUP_BY_SQL);
			if (!StringUtils.isBlank(groupBySql)) {

				sql.append(" group by " + this.takeOffRegex(groupBySql, param));
			}

			/*
			 * order by部分
			 */
			String orderSql = this.param.uiParams.remove("ui." + R.Sheet.ORDER_BY_SQL);
			if (!StringUtils.isBlank(orderSql)) {

				sql.append(
						" order by " + this.takeOffRegex(orderSql, param).replace("+", " asc ").replace("-", " desc "));
			}
			/*
			 * 获取分页的数据集
			 */
			List<Object[]> dataList_paging = session.createSQLQuery(sql.toString()).list();

			/*
			 * 拼凑结构体
			 */
			List<Map<String, String>> mapResult = new ArrayList<>();
			for (Object[] dataByPage: dataList_paging) {
				/*
				 * 存储每页（sheet）的分页属性和同胞属性的(key-value)值对
				 */
				Map<String, String> pagingParamValues = new HashMap<String, String>();

				for (int i = 0; i < allPagingColumns.size(); i++) {
					//分页属性的值
					String columnValue = dataByPage[i] == null ? "" : String.valueOf(dataByPage[i].toString());
					//分页属性的名称
					String columnName = allPagingColumns.get(i);
					// 将值填到预先存好的同胞属性（包括分页属性）
					pagingParamValues.put(columnName, columnValue);
				}
				mapResult.add(pagingParamValues);
			}
			return mapResult;
		}

		/**
		 * 脱掉UI替换符、系统替换符、属性替换符
		 * 
		 * @param column
		 * @param param
		 * @param isPieceOfSql
		 * @return
		 */
		private String takeOffRegex(String column, Param param) {
			// 替换所有系统参数
			String c1 = RegexUtil.takeOffSysRegex(column, this.param, false);
			/*
			 * 替换UI参数
			 */
			String c2 = RegexUtil.takeOffUIRegex(c1, this.param, false);
			/*
			 * 替换对象属性
			 */
			String c3 = RegexUtil.takeOffPropertyRegex(c2);
			return c3;

		}

		
	}

	/**
	 * 在服务器上收尾，将修改后的Workbook输出到新的文件
	 * 
	 * @throws IOException
	 */
	private void finishInServer() throws IOException {
		this.outDir = (this.outDir == null ? PathUtil.getTempSavePath(null) : this.outDir);
		Path path = this.outDir
				.resolve(UUID.randomUUID().toString() + FileUtil.getSuffix(this.param.templet.getName(), true));
		this.outputFile = path.toFile();
		FileOutputStream out = new FileOutputStream(this.outputFile);
		workbook.write(out);
		out.close();

	}

	/**
	 * 指定输出路径
	 * 
	 * @param path
	 */
	public ReportDealer setOutDir(Path path) {
		if (path == null) {
			return this;
		}
		this.outDir = path;
		return this;
	}

	/**
	 * 指定输出路径
	 * 
	 * @param path
	 */
	public ReportDealer setMilepost(boolean isMilepost) {
		this.isMilepost = isMilepost;
		return this;
	}

	/**
	 * 获取order By的sql属性
	 * 
	 * @param v
	 *            :形如："orderBy({wl.jiang}+,{wl.xuxu}-)"
	 * @return 形如：List<String> [{wl.jiang}+, {wl.xuxu}-]
	 */
	public static List<String> getColumnsRegex4OrderBySql(String v) {
		List<String> olist = new ArrayList<String>();

		/*
		 * //该方法只能获取2个以内的排序字段，多了就只获取收尾2个 Pattern p =
		 * Pattern.compile(R.Layer.OrderByRegex); Matcher m = p.matcher(v); if
		 * (m.find()) { for (int i = 0; i < m.groupCount(); i++) { //
		 * System.out.println(m.group(i)); String tem = m.group(i); if
		 * (tem.matches(R.Layer.OrderByRegex_piece)) { System.out.println(tem);
		 * olist.add(tem); }
		 * 
		 * } }
		 */
		v = v.substring("orderBy(".length(), v.length() - 1);
		String[] arr = v.split(",");
		for (String a : arr) {
			if (StringUtils.isBlank(a)) {
				continue;
			}
			a = a.trim();
			if (a.endsWith("+") || a.endsWith("-")) {
				a = a.replace("+", " asc").replace("-", " desc ");
			} else {
				a += " asc ";
			}
			olist.add(a);
		}
		return olist;
	}

	/**
	 * 克隆sheet
	 * 
	 * @param sheet
	 * @return
	 */
	public static Sheet cloneSheet(Sheet sheet) {
		if (sheet == null) {
			return null;
		}
		Workbook wk = sheet.getWorkbook();
		// 获取索引
		int index_modelSheet = wk.getSheetIndex(sheet.getSheetName());

		return wk.cloneSheet(index_modelSheet);
	}

	/**
	 * 在c位置右边新增length_newBlankCell个空白单元格，然后与c组成合并单元格 如果c已经是合并单元格，就将它扩展
	 * 
	 * @param c
	 *            当前单元格
	 * @param arr_RowIndex
	 *            是数字数组，里面存储行号，代表需要追加空白格且合并延伸的行
	 * @param length_newBlankCell
	 *            新增的空白单元格
	 */
	public static void putBlankCellAndMerge4Rows(Cell c, List<Integer> arr_RowIndex, int length_newBlankCell) {
		if (c == null || arr_RowIndex == null || length_newBlankCell <= 0 || arr_RowIndex.size() <= 0) {
			return;
		}
		// 当前单元格的列号
		int j = c.getColumnIndex();
		for (int a : arr_RowIndex) {

			// 处理每一行的延伸
			int firstRow = a - 1;// 行号要减1
			int firstCol = j;
			if (firstRow == c.getRowIndex() && firstCol == c.getColumnIndex()) {
				continue;// 提出本行的c,不处理c
			}
			Cell first_relationCell = POIUtil.getOrCreateCell(c.getSheet(), firstRow, firstCol);
			MergingCellUtil.insertBlankAndMergeCell2Cell(first_relationCell, length_newBlankCell);

		}

	}

	/**
	 * 遍历每一行，替换各个列中的每个替换符Regex的值
	 * 
	 * 如果c是合并单元格，则可能c的colspan>1,即同列的不同行的单元格包含多个单元格
	 * 故在平移复制时，要保证colspan内的单元格都平移复制，且对regex进行替换
	 * 
	 * @param c
	 *            ，其跨列代表平移范围
	 * @param arr_RowIndex行号数组，代表平移涉及的行
	 * @param regex
	 *            替换正则
	 * @param dynamicValues
	 *            值的数组，数组大小代表平移次数 横向的值的数组，每个值对应一个列,其中包含要通过Regex替换的值,
	 * @param isValue4Sql
	 *            dynamicValues值是sql中的值，如果是加''
	 */
	public static void replaceDynamicValue4RowsByRegex(Cell c, List<Integer> arr_RowIndex, String regex,
			List<? extends Object> dynamicValues, boolean isValue4Sql) {
		if (c == null || arr_RowIndex == null || dynamicValues == null || dynamicValues.size() <= 0
				|| arr_RowIndex.size() <= 0) {
			return;
		}
		int colStepByGroup = 1;// 默认c只覆盖一列
		Sheet s = c.getSheet();
		BooleanMsg<RangeCell> state = MergingCellUtil.isMergingCell(c);
		if (state.isOk()) {
			colStepByGroup = state.otherObject.getColSpan();
		}

		/*
		 * 遍历数据，每个数据是一次平移，会生成一列， 单元格内包含替换符，数据是需要替换的值，
		 * 因为colStepByGroup覆盖的单元格也要放数据的，故需要少移动一次
		 * 
		 * 这里数据采用倒着替换的方式，随着向后推移，就变成正序了
		 */
		for (int i = dynamicValues.size() - 1; i >= 0; i--) {

			if (i == 0) {// 无需平移了

				String last_replaceV = dynamicValues.get(0) == null ? "" : dynamicValues.get(0).toString();

				// 循环每一行，最后一个数值无需平移，
				for (int index = 0; index < arr_RowIndex.size(); index++) {

					int nextRowIndex = arr_RowIndex.get(index) - 1;// 行号要减一

					if (nextRowIndex == c.getRowIndex()) {// 和c相同额行不处理
						continue;
					}
					for (int j = 0; j < colStepByGroup; j++) {

						Cell downCell = POIUtil.getOrCreateCell(s, nextRowIndex, c.getColumnIndex() + j);
						String cellValue = POIUtil.getCellValue(downCell);
						String newReplaceV = "";
						if (isValue4Sql) {// 是值，直接替换正则
							newReplaceV = "'" + last_replaceV + "'";
						} else {
							// 是普通属性,要拼凑成对象属性格式
							// 先从单元格中获取对象
							String objectName = RegexUtil.getObjectName4Value(cellValue);
							newReplaceV = "{" + objectName + "." + last_replaceV + "}";
						}
						downCell.setCellValue(cellValue.replaceAll(regex, newReplaceV));

					}
				}

			} else {
				String replaceV = dynamicValues.get(i) == null ? "" : dynamicValues.get(i).toString();

				// 循环每一行，最后一个数值无需平移，
				for (int index = 0; index < arr_RowIndex.size(); index++) {

					int nextRowIndex = arr_RowIndex.get(index) - 1;// 行号要减一

					if (nextRowIndex == c.getRowIndex()) {// 和c相同额行不处理
						continue;
					}
					Cell first = POIUtil.getOrCreateCell(s, nextRowIndex, c.getColumnIndex());
					// 先平移 必须从第一个单元格开始平移复制即可
					POIUtil.insertAndCopyValue(first, 1, colStepByGroup);
					/*
					 * 每一次平移，步调为colStepByGroup，必须保证colStepByGroup
					 * 范围覆盖的每个一个单元格都是相同的数据
					 */
					for (int j = 0; j < colStepByGroup; j++) {
						// 总是去修改下一个位置的值
						Cell downCell = POIUtil.getOrCreateCell(s, nextRowIndex,
								c.getColumnIndex() + j + colStepByGroup);
						String cellValue = POIUtil.getCellValue(downCell);
						String newReplaceV = "";
						if (isValue4Sql) {// 是值，直接替换正则
							newReplaceV = "'" + replaceV + "'";
						} else {
							// 是普通属性,要拼凑成对象属性格式
							// 先从单元格中获取对象
							String objectName = RegexUtil.getObjectName4Value(cellValue);
							newReplaceV = "{" + objectName + "." + replaceV + "}";

						}
						downCell.setCellValue(cellValue.replaceAll(regex, newReplaceV));
					}

				}
			}

		}
	}

	/**
	 * 获取开始日期和结束日期之间的日期范围
	 * 
	 * @param start
	 * @param end
	 * @return List<String> 永不为null
	 * @throws ParseException
	 */
	public static List<String> getRangeDate(String start, String end) {
		// String start = "2018/02/19";
		// String end = "2018/03/02";
		List<String> arrDate = new ArrayList<String>();
		if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
			return arrDate;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		// 循环日期
		Calendar ca = Calendar.getInstance();
		Date curDate = null;
		Date endDate = null;
		try {
			curDate = sdf.parse(start);
			endDate = sdf.parse(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		if (curDate == null || endDate == null) {

			return arrDate;
		}

		while (curDate.compareTo(endDate) <= 0) {
			// 放入值集合
			arrDate.add(sdf.format(curDate));

			ca.setTime(curDate);
			// 业务处理...
			ca.add(Calendar.DATE, 1);
			curDate = ca.getTime();
		}
		return arrDate;
	}

}
