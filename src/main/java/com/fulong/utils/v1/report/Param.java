package com.fulong.utils.v1.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;

import com.fulong.utils.v1.report.bean.Layer;
import com.fulong.utils.v2.report.dto.MilepostDTO;
/**
 * 全局报表公共参数，管理所有和报表相关的参数
 * @see
 * @author jx
 * @date 2017年5月5日 上午11:07:38
 */
public class Param {
	/*
	 * 客户端的请求参数(属性名：值) 
	 */
	public Map<String, String> uiParams = new HashMap<String,String>();
	/**
	 * UI参数的默认值，比如类似 全部、无、这类的文字
	 */
	public final static Map<String,String> UI_PARAMS_DEFAULT_VALUE = new HashMap<String,String>();
	
	static {
		/*
		 * 管线
		 */
		UI_PARAMS_DEFAULT_VALUE.put("ui.subContractorName_yz", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.subContractorName_xc", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.areaCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.isoCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.unitCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.pipeLineCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.pipeGradeCode", "全部");
		
		/*
		 * 管段
		 */
		UI_PARAMS_DEFAULT_VALUE.put("ui.subContractorName_ps", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.pipeSectionCode", "全部");
		/*
		 * 焊缝
		 */
		UI_PARAMS_DEFAULT_VALUE.put("ui.subContractorName_wl", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.weldLineType", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.ddhgCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.gmhgCode", "全部");
		/*
		 * 探伤
		 */
		UI_PARAMS_DEFAULT_VALUE.put("ui.flawBatchId", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.flawBatchStateName", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.flawTypeRate", "全部");
		
		/*
		 * 试压
		 */
		UI_PARAMS_DEFAULT_VALUE.put("ui.sysCode", "全部");
		UI_PARAMS_DEFAULT_VALUE.put("ui.subSysCode", "全部");
	}
	
	/*
	 * 系统参数属(性名：值) 
	 */
	public Map<String, String> sysParams= new HashMap<String,String>();
	
	/**
	 * 主模板
	 */
	public Sheet mainSheet;
	/**
	 * 是否分页
	 */
	public boolean isPaging = false;
	/*
	 * 不包含对象前缀
	 */
	public String pagingColumn= null;
	
	
	/**
	 * 模板解析结构
	 * 记录所有的层（层行号,层内值的集合），全部存储，先不区分是组还是单层(循环层或汇总层)
	 */
	public TreeMap<Integer,Layer> layers = new TreeMap<Integer,Layer>() ;
	
	/**
	 * 里程碑信息
	 */
	//全部管段里程碑
	public List<MilepostDTO> milepost_ps =null;
	//管段预制里程碑
	public List<MilepostDTO> milepost_ps_yz =null;
	//管段安装里程碑
	public List<MilepostDTO> milepost_ps_xc = null;
	//焊缝里程碑
	public List<MilepostDTO> milepost_wl = null;
	
	
	/*
	 * 记录存在动态字段的开始行号
	 * 由于动态列生成的新列时，在新增行时，
	 * 这些新列的单元格的样式是白色的，
	 * 必须让其复制所属行前边的样式
	 */
	public final int maxCol = 1000;//最大报表数据列号
	
	private int dynamicField_startCol = maxCol;
	/*
	 * 记录存在动态字段的开始行号
	 * @note 由于动态列生成的新列时，在新增行时，
	 * 		这些新列的单元格的样式是白色的，
	 * 		必须让其复制所属行前边的样式
	 */
	public void modifyDynamicFieldStartCol(int c){
		
		if(c<this.dynamicField_startCol && c>0){
			this.dynamicField_startCol = c;
		}
	}

	public int getDynamicField_startCol(){
		return this.dynamicField_startCol;
	}

}
