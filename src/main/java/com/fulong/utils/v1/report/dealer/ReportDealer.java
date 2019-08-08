package com.fulong.utils.v1.report.dealer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;

import com.fulong.utils.v1.poi.DomPOI;
import com.fulong.utils.v1.report.Param;
import com.fulong.utils.v1.report.bean.BaseLayer;
import com.fulong.utils.v1.report.bean.GroupLayer;
import com.fulong.utils.v1.report.bean.LoopLayer;
import com.fulong.utils.v1.report.bean.MergingLayer;
import com.fulong.utils.v1.report.bean.RangeCell;
import com.fulong.utils.v1.report.tool.MergingCellUtil;
import com.fulong.utils.v1.report.tool.POIUtil;
import com.fulong.utils.v1.report.tool.R;
import com.fulong.utils.v1.report.tool.RegexUtil;
import com.fulong.utils.v1.tool.BaseHelper;
import com.fulong.utils.v1.tool.BooleanMsg;
import com.fulong.utils.v1.tool.DataHelper;
import com.fulong.utils.v1.tool.PathUtil;
import com.fulong.utils.v1.tool.SQLUtil;
import com.fulong.utils.v2.bean.KeyValue;
import com.fulong.utils.v2.error.MyReportException;
import com.fulong.utils.v2.report.dto.LayerDTO;
import com.fulong.utils.v2.report.dto.MilepostDTO;

import net.sf.json.JSONObject;

/**
 * 报表处理总入口
 * 
 * @see
 * @author jx
 * @date 2017年4月26日 下午2:10:15
 */
public class ReportDealer {


	/*
	 * 模板文件
	 */
	private final File modelFile;
	/**
	 * 这个贯穿全剧的公共参数 存储了每个模块都会使用的参数
	 */
	private final Param param;

	private Session session;

	private Workbook wk;
	public File outputFile;

	public ReportDealer(Session session, File modelFile, Param param) throws Exception {
		this.modelFile = modelFile;
		this.param = param;
		this.session = session;
		init();
	}

	/**
	 * 初始化
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		try {
			// 读取Excel模板
			this.wk = DomPOI.readExcel(modelFile);

			// 默认获取第一个sheet作为模板sheet,并保存到公共变量param里
			this.param.mainSheet = wk.getSheetAt(0);

			new Painter(this.param.uiParams, this.param.mainSheet).draw();
			
			// 初始化里程碑信息
			this.initMilepostInfo();

			ParamAnalyzer paramAnalyzer = new ParamAnalyzer(this.param, this.session).filterMergingByLayer()
					.filterGroup4Layer();
			PagingAnalyzer pagingAnalyzer = new PagingAnalyzer(this.param, paramAnalyzer.pagingColumns);

			List<SheetDealer> sheets = pagingAnalyzer.page2Sheet(this.session);
			// 循环处理每个sheet
			for (SheetDealer sheetDealer : sheets) {
				// 先清理上一页的缓存，在处理下一页
				sheetDealer.clearCache().runing(this.session);
			}
			// 删除主模板....
			this.wk.removeSheetAt(0);
			System.out.println("恭喜您!!");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			String msg = DataHelper.filterValueBy(e.getMessage());
			// 用空串替换所有的替换符，
			// 并将错误信息显示在文件的第一个空白单元格中
			new SheetDealer.BlankSheet(msg, POIUtil.renameSheet(this.wk.cloneSheet(0), SheetDealer.DEFAULT_SHEETNAME));
			e.printStackTrace();
		} finally {

			try {
				this.finishInServer();
				// this.finishInClient();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 将里程碑的id和标题都初始化到公共参数param中 因为这是固定值，提前加载好后方便以后读取， 这样就不必要传session了
	 */
	private void initMilepostInfo() {
		String hql_ps_yz = "select new com.fulong.utils.v2.report.dto.MilepostDTO(m.milepostID,m.milepostName)  from Milepost m where  m.milepostClass.milepostClassID='PipeSection_DL' order by m.milepostOrderIndex asc ";
		String hql_ps_xc = "select new com.fulong.utils.v2.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='PipeInstall_DL' order by m.milepostOrderIndex asc ";
		String hql_ps = "select new com.fulong.utils.v2.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='PipeSection_DL' or  m.milepostClass.milepostClassID='PipeInstall_DL'  order by m.milepostOrderIndex asc ";
		String hql_wl = "select new com.fulong.utils.v2.report.dto.MilepostDTO(m.milepostID,m.milepostName) from Milepost m where  m.milepostClass.milepostClassID='WeldLine_DL' order by m.milepostOrderIndex asc";

		this.param.milepost_ps_yz = session.createQuery(hql_ps_yz).list();
		this.param.milepost_ps_xc = session.createQuery(hql_ps_xc).list();
		this.param.milepost_ps = session.createQuery(hql_ps).list();
		this.param.milepost_wl = session.createQuery(hql_wl).list();

	}

	/**
	 * 模板绘制器
	 * 在读取模板内容之前，先绘制模板内容
	 * @see
	 * @author jx
	 * @date 2017年6月1日 上午11:58:59
	 */
	public static class Painter {
		private final Map<String, String> uiParams;
		private final Sheet model;
		public static  final String KEY = "ui.layer";

		public Painter(Map<String, String> uiParams, Sheet model) {
			this.uiParams = uiParams;
			this.model = model;
		}

		public void draw() {
			if (uiParams == null || uiParams.size() <= 0) {
				return;
			}

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

					c.setCellValue(v.key);
					downC.setCellValue(v.value);

				}
			}
			
			//移除key
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
		private final Session session;
		/**
		 * 存储分页属性的同胞属性
		 */
		public final Set<String> pagingColumns = new HashSet<String>();
		/**
		 * 简单的模板解析结构 内容：记录所有的层（层行号,层内值的集合），全部存储，先不区分是组还是单层(循环层或汇总层)
		 */
		private TreeMap<Integer, LoopLayer> simpleLayers = new TreeMap<Integer, LoopLayer>();

		/**
		 * simpleLayers经过了组过滤后的数据
		 */
		private TreeMap<Integer, BaseLayer> filteredLayers = new TreeMap<Integer, BaseLayer>();

		/**
		 * 存储所有的合并单元格 (行号,合并单元格)
		 */
		private TreeMap<Integer, CellRangeAddress> ranges = new TreeMap<Integer, CellRangeAddress>();

		private RangeParam rangeParamAnalyzer = null;

		public ParamAnalyzer(Param param, Session session) throws Exception {

			this.param = param;
			this.session = session;
			this.rangeParamAnalyzer = new RangeParam(this.param);
			this.init();
		}

		/*
		 * 任务有2个： 1 找到分页属性的同胞属性名称 2 找到所有的层或组
		 */
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
						mergingColumns.add(status.otherObject.range);
					}

					if (StringUtils.isBlank(cellValue)) {
						continue;
					}
					cellValue = cellValue.trim();

					// 是否为分页属性的同胞属性
					if (cellValue.matches(".*" + R.SHEET_REGEX + ".*")) {
						List<String> arr = RegexUtil.getMatchedGroup4Regex(cellValue, R.SHEET_REGEX);
						for (String key : arr) {
							key = key.replace("{", "").replace("}", "").split("\\.")[1];
							this.pagingColumns.add(key);
						}

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

					this.saveMergingCell(mergingColumns);

					if (isGroup) {
						layer.remarkGroup();// 标记为组内层
					}
					this.simpleLayers.put(i, layer);
				}

			}

		}

		/**
		 * 先处理合并单元格，在处理组
		 * 
		 * @return
		 */
		public ParamAnalyzer filterMergingByLayer() {

			/*
			 * 必须保证组内 不能有合并单元格
			 */
			for (CellRangeAddress range : this.ranges.values()) {

				int indexStart = range.getFirstRow();
				int indexEnd = range.getLastRow();
				List<LoopLayer> list = new ArrayList<LoopLayer>();

				for (int i = indexStart; i <= indexEnd; i++) {
					LoopLayer layer = this.simpleLayers.remove(i);
					if (layer != null) {
						list.add(layer);
					}
				}
				if (list.size() > 1) {
					this.filteredLayers.put(indexStart, new MergingLayer(indexStart, list, this.param));
				} else if (list.size() == 1) {
					this.filteredLayers.put(indexStart, list.get(0));

				}

			}

			// 剩余的都是单独层
			for (LoopLayer layer : this.simpleLayers.values()) {
				this.filteredLayers.put(layer.getIndex(), layer);
			}

			return this;
		}

		/**
		 * 对层集合进行格式化 内容：对simpleLayers进行统一的进一步处理， 为SheetDealer实际解析sql时提供方便 1
		 * 将组中的层重新封装:GroupLayer 2 将合并单元格涉及的层放在一起:MergingLayer 3 其他的层不变:LoopLayer
		 */
		public ParamAnalyzer filterGroup4Layer() {
			if (this.filteredLayers.size() <= 0) {
				return this;
			}

			// LoopLayer hostLayer4Group = null;
			TreeSet<BaseLayer> treeSet = null;
			for (Entry<Integer, BaseLayer> r : this.filteredLayers.entrySet()) {
				// int indexR = r.getKey();
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
		 * 检查合并单元格的合法性
		 * 
		 * @return
		 */
		private boolean checkMergingValid() {
			if (this.ranges.size() <= 0) {
				return true;
			}

			CellRangeAddress first = null;

			for (CellRangeAddress range : this.ranges.values()) {
				if (first == null) {
					first = range;
					continue;
				}
				if (first.getLastRow() >= range.getFirstRow()) {
					return false;
				}
				first = range;
			}
			return true;
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
		 * 临时存储合并单元格 内容：等待层都分出来后统一处理合并单元格导致层的合并问题
		 * 
		 * @param new_mergingCell
		 */
		private void saveMergingCell(Set<CellRangeAddress> set) {
			if (set == null) {
				return;
			}

			for (CellRangeAddress new_mergingCell : set) {
				int firstR = new_mergingCell.getFirstRow();
				CellRangeAddress old_mergingCell = this.ranges.get(firstR);
				if (old_mergingCell == null) {// 不存在就插入
					this.ranges.put(firstR, new_mergingCell);
				} else {
					/*
					 * 存在 比较看那个范围大，就用哪个 由于在同一行下，故比较尾行即可
					 */
					if (old_mergingCell.getLastRow() < new_mergingCell.getLastRow()) {
						this.ranges.put(firstR, new_mergingCell);

					}
				}
			}

		}

		/**
		 * 范围形式的Report级别参数
		 * 
		 * @see
		 * @author jx
		 * @date 2017年5月17日 上午9:33:50
		 */
		public static class RangeParam {

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

				Sheet s = c.getSheet();
				Row r = c.getRow();
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
	 * 分页解析器 因为解析代码太多了，很多私有方法，而且没有复用，故封装个类 之所以是静态内部类，是因为 分页解析器可能会有多种，且都
	 * 隶属于ReportDealer类的业务，故放在此类下（静态内部类和公共类功能完全相同，除了new时，必须加上父类名字）
	 * 
	 * @see
	 * @author jx
	 * @date 2017年5月4日 下午5:39:42
	 */
	public static class PagingAnalyzer {

		private Param param;
		private Set<String> pagingColumns;
		// 分页对象
		private String objectName = null;
		// 分页DB视图
		private String view4DB = null;

		public PagingAnalyzer(Param param, Set<String> pagingColumns) {
			this.param = param;
			this.pagingColumns = pagingColumns;
		}

		/**
		 * 分页
		 * 
		 * @return
		 * @throws MyReportException
		 */
		public List<SheetDealer> page2Sheet(Session session) throws MyReportException {

			String sheetname_modelSheet = this.param.mainSheet.getSheetName();

			if (!sheetname_modelSheet.matches(R.OBJECT_PROPERTY_REGEX)) {// 没有分页

				/*
				 * 更加模板克隆一个新的模板作为数据填写的蓝本
				 */
				SheetDealer oneSheet = new SheetDealer(ReportDealer.cloneSheet(this.param.mainSheet), null, this.param);
				List<SheetDealer> olist = new ArrayList<SheetDealer>();
				olist.add(oneSheet);
				return olist;
			}

			// 必须分页
			this.param.isPaging = true;

			// 获取分页属性
			String key_sheet = RegexUtil.getFirstKeyByRegex(this.param.mainSheet.getSheetName(), R.OBJECT_PROPERTY_REGEX);
			String[] key_sheet_arr = key_sheet.split("\\.");
			// 找到分页对象名称
			this.objectName = key_sheet_arr[0];
			this.param.pagingColumn = key_sheet_arr[1];
			/*
			 * 检查集合中是否已经存在分页属性，避免重复而影响之后的sql Set 是不重复的，呵呵
			 */
			if (!StringUtils.isBlank(key_sheet)) {
				this.pagingColumns.add(this.param.pagingColumn);
			}

			// 找到对应的真实的数据库表名或视图（去属性文件里找到对应关系）
			this.view4DB = DataHelper.loadReportProperty(this.objectName);

			// 变成数组形式
			String[] arr_columns = this.pagingColumns.toArray(new String[this.pagingColumns.size()]);

			// 获取分页数据
			List<Object[]> dataList_paging = this.getPagingData(session, arr_columns);

			List<SheetDealer> sheetDealers = new ArrayList<SheetDealer>();
			// 获取每一页的数据集,每一页就是一个sheet
			int len = arr_columns.length;
			for (Object[] dataByPage : dataList_paging) {

				/*
				 * 存储每页（sheet）的分页属性和同胞属性的(key-value)值对
				 */
				Map<String, String> pagingParam = new HashMap<String, String>();

				for (int i = 0; i < len; i++) {
					String columnValue = dataByPage[i] == null ? "" : String.valueOf(dataByPage[i].toString());
					String columnName = arr_columns[i];
					// 将值填到预先存好的同胞属性（包括分页属性）
					pagingParam.put(columnName, columnValue);
				}

				/*
				 * 创建sheetDealer，并添加到集合中 注意：sheetDealer中的模板是主模板的克隆sheet
				 */
				sheetDealers
						.add(new SheetDealer(ReportDealer.cloneSheet(this.param.mainSheet), pagingParam, this.param));
			}

			return sheetDealers;
		}

		/**
		 * 获取分页数据
		 * 
		 * @param session
		 * @param sql
		 * @return
		 * @throws MyReportException
		 */
		private List<Object[]> getPagingData(Session session, String[] arr_columns) throws MyReportException {
			// 找到ui中的where部分
			String uiKey = "ui." + this.param.pagingColumn;
			String param_paging4Where = this.param.uiParams.get(uiKey);
			String sql_param_paging = "";
			if (!StringUtils.isBlank(param_paging4Where)) {
				sql_param_paging = " where " + this.param.pagingColumn + " in("
						+ SQLUtil.filter2SqlOfIn(param_paging4Where.split(",")) + ")";
				// 必须移除被分页属性使用过的ui属性
				this.param.uiParams.remove(uiKey);
			}
			/*
			 * 探伤类型
			 */
			if ("flawTypeId".equalsIgnoreCase(this.param.pagingColumn)) {
				List<Object[]> list = session.createQuery("select flawTypeId ,flawTypeName from FlawType "+sql_param_paging).list();

				return list;
			}
			// 将集合中的str按照逗号分隔拼凑成sql片段
			String partOfSql4names = StringUtils.join(arr_columns, ",");

			/*
			 * 形如：select a.A,a.B from view a group by a.A,a.B
			 * 由于分页属性可能不是对象的主键，select后可能存在重复值， 而distinct无法对多个属性操作，故采用group by
			 */
			String sql = "select " + partOfSql4names + " from " + this.view4DB + " " + objectName;

			//where部分
			sql+=sql_param_paging;
			
			// group by部分
			sql += " group by " + partOfSql4names;
			/*
			 * 获取分页的数据集
			 */
			List<Object[]> dataList_paging = session.createSQLQuery(sql).list();
			if (dataList_paging == null || dataList_paging.size() <= 0) {
				throw new MyReportException("no data in paging");
			}
			return dataList_paging;
		}

	}

	/**
	 * 在服务器上收尾，将修改后的Workbook输出到新的文件
	 * 
	 * @throws IOException
	 */
	private void finishInServer() throws IOException {
		Path path = PathUtil.getTempSavePath("report").resolve(
				BaseHelper.getNewFileNameWithDate(this.modelFile.getName()));
		this.outputFile = path.toFile();
		FileOutputStream out = new FileOutputStream(this.outputFile);
		wk.write(out);
		out.close();

	}

	/**
	 * 在客户端上收尾，将修改后的Workbook输出到新的文件
	 * 
	 * @throws IOException
	 */
	private void finishInClient() throws IOException {
		String path = "d:/" + BaseHelper.getNewFileNameWithDate(this.modelFile.getName());
		this.outputFile = new File(path);
		FileOutputStream out = new FileOutputStream(this.outputFile);
		wk.write(out);
		out.close();

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

		/*//该方法只能获取2个以内的排序字段，多了就只获取收尾2个
		Pattern p = Pattern.compile(R.Layer.OrderByRegex);
		Matcher m = p.matcher(v);
		if (m.find()) {
			for (int i = 0; i < m.groupCount(); i++) {
				// System.out.println(m.group(i));
				String tem = m.group(i);
				if (tem.matches(R.Layer.OrderByRegex_piece)) {
					System.out.println(tem);
					olist.add(tem);
				}

			}
		}*/
		v = v.substring("orderBy(".length(), v.length() - 1);
		String[] arr = v.split(",");
		for(String a:arr){
			if(StringUtils.isBlank(a)){
				continue;
			}
			a = a.trim();
			if(a.endsWith("+")||a.endsWith("-")){
				a = a.replace("+", " asc").replace("-", " desc ");
			}else{
				a+=" asc ";
			}
			olist.add(a);
		}
		return olist;
	}
	public static void main(String[] args) {
		
		String str="orderBy({wl.jiang}+,{wl.xuxu}-,{wl.jj}+)";
		System.out.println(ArrayUtils.toString(getColumnsRegex4OrderBySql(str)));
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

			if(i==0){//无需平移了
				
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
					
				
				
			}else{
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
						Cell downCell = POIUtil.getOrCreateCell(s, nextRowIndex, c.getColumnIndex() + j + colStepByGroup);
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
	public static List<String> getRangeDate(String start, String end) throws ParseException {
		// String start = "2018/02/19";
		// String end = "2018/03/02";
		List<String> arrDate = new ArrayList<String>();
		if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
			return arrDate;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		// 循环日期
		Calendar ca = Calendar.getInstance();
		Date curDate = sdf.parse(start);
		Date endDate = sdf.parse(end);
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
