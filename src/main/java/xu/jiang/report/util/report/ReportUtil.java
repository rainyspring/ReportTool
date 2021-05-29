package xu.jiang.report.util.report;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTemplate;

import xu.jiang.report.Param;
import xu.jiang.report.R;
import xu.jiang.report.dealer.ReportDealer;
import xu.jiang.report.util.file.FileUtil;

/**
 * 报表专用对外辅助类
 * @see
 * @author jx
 * @date 2018年1月18日 下午3:35:13
 */
public class ReportUtil {
	/**
	 * 解析UI参数
	 * 创建一个报表参数实例
	 * @param mapParams
	 * @return
	 */
	public static Param createReportParam(Map<String, String> mapParams){
		Param p = new Param();

		p.sysParams.put("sys.currentDate", new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
		p.sysParams.put("sys.currentTime", new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
		p.sysParams.put("sys.currentDateTime", new SimpleDateFormat("yyyy/MM/dd").format(new Date()));

		if(mapParams==null||mapParams.size()<=0) {
			return p;
		}
		/*
		 * 获取request的UI参数
		 */
		Map<String, String> mapUI = mapParams;
		
		for (Entry<String, String> e : mapUI.entrySet()) {
			String name = e.getKey();
			String value = e.getValue();
			if (value != null && !StringUtils.isBlank(value)) {
				if (R.STRING_MODELFILENAME.equals(name)) {
					// 获取模板文件名,很重要,废弃
					p.modelFileName = value;
					
					continue;
				}
				
				p.uiParams.put("ui." + name, value);

			}
		}
		// 获取时间范围的跨度
		String startDate = p.uiParams.get("ui." + R.SRING_START_DATE);
		String endDate = p.uiParams.get("ui." + R.SRING_END_DATE);
		if (!StringUtils.isBlank(startDate) && !StringUtils.isBlank(endDate)) {
			p.uiParams.put("ui." + R.SRING_DAYS, String.valueOf(ReportDealer.getRangeDate(startDate, endDate).size()));
		}
		
		return p;
	}
	/**
	 * 核心处理过程
	 * 
	 * @param project
	 * @param user
	 * @param outDir
	 *            输出目录，如果为null 则为temp目录
	 * @throws Exception
	 */
	public static File useReportDealer(Param param, Path outDir,HibernateTemplate hibernateTemplate) {
		File outf = null;
		
		if(hibernateTemplate==null ){
			return outf;
		}
		Session session = null;
		try {
			session = hibernateTemplate.getSessionFactory().openSession();
			ReportDealer dealer = new ReportDealer(session, param);
			if (outDir != null) {
				dealer.setOutDir(outDir);
			}else{
				//使用临时目录
				dealer.setOutDir(FileUtil.getOrCreateTempPath());
			}
			outf = dealer.setMilepost(false).start();

			session.close();
		} catch (Exception e) {
			if (session != null) {
				session.close();
			}
			e.printStackTrace();

		}
		return outf;
	}
	
	
}
