package com.fulong.utils.v2.tool;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class PathUtil {
	/**
	 * 获取临时目录temp中的子目录
     * 
	 * @param dir （dir==null or 空  则获取temp目录）
	 * @return
	 */
	public static Path getTempSavePath(String dir) {
		dir = dir==null?"":dir;
		return getRootPath().resolve("temp").resolve(dir);

	}

	/**
	 * @deprecated
	 * 获取保存服务器保存文件的基础路径（目录末不包含分隔符） 通过classLoader获取
	 * 
	 * @param sc
	 * @return
	 */
	public static Path getTempSavePath4Base(String dir) {
		
		return getTempSavePath("base").resolve(dir);
	}
	/**
	 * @deprecated
	 * 获取保存服务器保存文件的项目总目录（目录末不包含分隔符） 通过classLoader获取
	 * @param projectId 
	 * @return
	 */
	public static Path getTempSavePath4Project(String datasourcId) {
		datasourcId = StringUtils.isBlank(datasourcId)?"systemdb":datasourcId;
		
		return getTempSavePath(datasourcId);

	}
	/**
	 * @deprecated
	 * 获取保存服务器保存文件的报表模板路径（目录末不包含分隔符） 通过classLoader获取
	 * @param projectId 
	 * @return
	 */
	public static Path getTempSavePath4ReportTemplet(String datasourcId) {
	
		return getTempSavePath4Project(datasourcId).resolve("reportTemplet");

	}
	/**
	 * @deprecated
	 * 获取保存服务器保存文件的图档路径（目录末不包含分隔符） 通过classLoader获取
	 * @param projectId 
	 * @return
	 */
	public static Path getTempSavePath4Blueprint(String datasourcId) {
	
		return getTempSavePath4Project(datasourcId).resolve("blueprint");

	}
	/**
	 * @deprecated
	 * 获取旧的报表模板保存路径（目录末不包含分隔符）
	 * @return
	 */
	public static Path getTempSavePath4OldReportTemplet() {
		return PathUtil.getRootPath().resolve("excelTemp").resolve("temp") ;

	}
	/**
	 * 获取保存服务器根路径（目录末不包含分隔符） 通过classLoader获取
	 * 
	 * @param sc
	 * @return
	 */
	public static Path getRootPath() {
		//String path = DataHelper.class.getResource("/").getPath();
		String path = DataHelper.class.getClassLoader().getResource("").getPath();
		//System.out.println("@@@@@--"+path);
		if (BaseHelper.isWindowsOS() && path.startsWith("/")) {// window系统
			path = path.substring(1, path.length());
		}
		Path root = Paths.get(path.replace("WEB-INF", "").replace("classes", ""));
		//String tem = path.replace("WEB-INF", "").replace("classes", "");
		System.out.println("rootPath:-----"+root);
		return root;

	}
	/**
	 * 获取文件存储目录中的子目录
	 * @param dir （dir==null or 空  则获取file目录）
	 * @return
	 */
	public static Path getFilePath(String dir) {
		dir = dir==null?"":dir;
		return getRootPath().resolve("file").resolve(dir);

	}
	/**
	 * 获取文件存储目录中的子目录
	 * @param dir （dir==null or 空  则获取file目录）
	 * @return
	 */
	public static Path getImgPath(String dir) {
		dir = dir==null?"":dir;
		return getRootPath().resolve("images").resolve(dir);

	}
	/**
	 * 获取系统报表模板
	 * @return
	 */
	public static Path getReportTempletPath4Sys() {
		return getReportTempletPath("system");

	}
	/**
	 * 获取系统模板目录
	 * @return
	 */
	public static Path getReportTempletPath(String branch) {
		branch = branch==null?"":branch;
		return getFilePath("reportTemplet").resolve(branch);

	}
	/**
	 * 获取规范化的path
	 * 
	 * @param path
	 * @return
	 */
	public static String getFormatPath(String path) {
		if (StringUtils.isBlank(path)) {
			return null;
		}
		String temp = new File(path).toPath().toString();
		// System.out.println("formatPath:"+temp);
		return temp;
	}
	/**
	 * 获取服务器文件访问路径（目录末包含分隔符）
	 * 
	 * @param request
	 * @return
	 */
	public static String getServerPath(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/temp/";
	}
}
