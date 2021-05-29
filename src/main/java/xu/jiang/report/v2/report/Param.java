package xu.jiang.report.v2.report;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;

import xu.jiang.report.v2.report.bean.Layer;
import xu.jiang.report.v2.report.bean.PagingColumnContainer;
import xu.jiang.report.v2.report.dto.MilepostDTO;

/**
 * 全局报表公共参数，管理所有和报表相关的参数
 * 
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:07:38
 */
public class Param {

	/**
	 * @note 报表名称（包含后缀名）
	 */
	public String modelFileName = null;
	
	public File templet = null;

	public Param() {

	}

	/**
	 * @deprecated
	 * @author: jx  
	 * @Title:  Param   
	 * @Description:    TODO(这里用一句话描述这个方法的作用)   
	 * @param:  @param modelFileName  
	 * @throws
	 */
	public Param(String modelFileName) {
		this.modelFileName = modelFileName;
		init();
	}
	
	public Param(File templet) {
		this.templet = templet;
		this.modelFileName = this.templet.getName();
		init();
	}
	
	
	private void init() {

		/*
		 * 管线
		 */
		uiParams_defaultValue.put("ui.subContractorName_yz", "全部");
		uiParams_defaultValue.put("ui.subContractorName_xc", "全部");
		uiParams_defaultValue.put("ui.areaCode", "全部");
		uiParams_defaultValue.put("ui.isoCode", "全部");
		uiParams_defaultValue.put("ui.unitCode", "全部");
		uiParams_defaultValue.put("ui.pipeLineCode", "全部");
		uiParams_defaultValue.put("ui.pipeGradeCode", "全部");

		/*
		 * 管段
		 */
		uiParams_defaultValue.put("ui.subContractorName_ps", "全部");
		uiParams_defaultValue.put("ui.pipeSectionCode", "全部");
		/*
		 * 焊缝
		 */
		uiParams_defaultValue.put("ui.subContractorName_wl", "全部");
		uiParams_defaultValue.put("ui.weldLineType", "全部");
		uiParams_defaultValue.put("ui.ddhgCode", "全部");
		uiParams_defaultValue.put("ui.gmhgCode", "全部");
		/*
		 * 探伤
		 */
		uiParams_defaultValue.put("ui.flawBatchId", "全部");
		uiParams_defaultValue.put("ui.flawBatchStateName", "全部");
		uiParams_defaultValue.put("ui.flawTypeRate", "全部");

		/*
		 * 试压
		 */
		uiParams_defaultValue.put("ui.sysCode", "全部");
		uiParams_defaultValue.put("ui.subSysCode", "全部");

	}

	/*
	 * 客户端的请求参数(属性名：值)
	 */
	public Map<String, String> uiParams = new HashMap<String, String>();
	/**
	 * UI参数的默认值，比如类似 全部、无、这类的文字
	 */
	public final Map<String, String> uiParams_defaultValue = new HashMap<String, String>();

	/*
	 * 系统参数属(性名：值)
	 */
	public Map<String, String> sysParams = new HashMap<String, String>();

	/**
	 * 主模板
	 */
	public Sheet mainSheet;

	/**
	 * 模板解析结构 记录所有的层（层行号,层内值的集合），全部存储，先不区分是组还是单层(循环层或汇总层)
	 */
	public TreeMap<Integer, Layer> layers = new TreeMap<Integer, Layer>();
	/**
	 * 是否分页
	 */
	public boolean isPaging = false;
	/**
	 * 分页属性管理器
	 */
	public PagingColumnContainer pagingColumnContainer = new PagingColumnContainer();

	/**
	 * 里程碑信息
	 */
	// 全部管段里程碑
	public List<MilepostDTO> milepost_ps = null;
	// 管段预制里程碑
	public List<MilepostDTO> milepost_ps_yz = null;
	// 管段安装里程碑
	public List<MilepostDTO> milepost_ps_xc = null;
	// 焊缝里程碑
	public List<MilepostDTO> milepost_wl = null;

	/*
	 * 记录存在动态字段的开始行号 由于动态列生成的新列时，在新增行时， 这些新列的单元格的样式是白色的， 必须让其复制所属行前边的样式
	 */
	public final int maxCol = 1000;// 最大报表数据列号

	private int dynamicField_startCol = maxCol;

	/*
	 * 记录存在动态字段的开始行号
	 * 
	 * @note 由于动态列生成的新列时，在新增行时， 这些新列的单元格的样式是白色的， 必须让其复制所属行前边的样式
	 */
	public void modifyDynamicFieldStartCol(int c) {

		if (c < this.dynamicField_startCol && c > 0) {
			this.dynamicField_startCol = c;
		}
	}

	public int getDynamicField_startCol() {
		return this.dynamicField_startCol;
	}

}
