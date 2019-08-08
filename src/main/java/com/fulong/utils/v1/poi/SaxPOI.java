package com.fulong.utils.v1.poi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fulong.utils.v1.tool.BaseHelper;
import com.fulong.utils.v1.tool.PathUtil;
import com.fulong.utils.v2.error.MyExcelException;

public class SaxPOI {
	private  static SaxPOI instance = null;
	public final static int TITLELINE_ROW_INDEX = 0;
	
	private SaxPOI() {

	}
	public static SaxPOI getInstance() {
		if(instance==null){
			instance = new SaxPOI();
		}
		return instance;
	}

	/**
	 * @deprecated
	 * 利用POI的sax方式分页读取2007的Excel,
	 * 一次性取出所有行，性能差
	 * 适用于第一行是标题，之后是数据列的标准Excel模板
	 * @param file
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public  static  List<List<String>> getPagingData(File file, int page,
			int rows) throws Exception {

		// 总记录行数（除去标题）
		int sumOfRows = (int) SaxPOI
				.getSize4TheFile(file.getPath());

		return SaxPOI.getPagingData(file, page, sumOfRows, sumOfRows,1);
	}
	/**
	 * 获取文件标题
	 * @param file
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	public static List<List<String>> getTitles(File file,int rows) throws Exception{
		
		return SaxPOI.getPagingData(file, 1, rows, rows,0);
	}
	/**
	 * 利用POI的sax方式分页读取2007的Excel
	 * 
	 * @param file
	 * @param page 页号 从1开始
	 * @param rows 页内行数
	 * @param totalRows 实际的总行数（去除标题）
	 * @param outOfTitleRow 忽略标题行的行数，负数忽略，0代表包含标题，正值代表跳过的标题数
	 * @return
	 * @throws Exception
	 */
	public  static  List<List<String>> getPagingData(File file, int page,
			int rows,int totalRows,int outOfTitleRow) throws Exception {

		// 总记录行数（除去标题）
		int sumOfRows = totalRows;

		// SAX解析方式第一行是1，不是0
		int headLineRowNum = SaxPOI.TITLELINE_ROW_INDEX;
		if(outOfTitleRow>0){
			headLineRowNum += outOfTitleRow;
		}
		// 循环的默认起始和结束
		int startRow = headLineRowNum + 1;
		int endRow = startRow + rows - 1;
		// 最后一行的行号
		int lastRowNum = sumOfRows + headLineRowNum;
		int odd = sumOfRows - ((page - 1) * rows);// 本页之前的所有记录
		if ((page - 1) * rows >= sumOfRows) {// 本页之前的所有记录已经超过总数了
			return new LinkedList<List<String>>();
		} else if (odd > 0 && odd <= rows) {// 剩下的行数不够本页显示数

			startRow = (headLineRowNum + 1) + (page - 1) * rows;
			endRow = lastRowNum;
		} else {// 所剩行数充足
			startRow = headLineRowNum + (page - 1) * rows + 1;
			endRow = headLineRowNum + page * rows;
		}

		return new MyExcel2007ForPaging_high(file.getPath(), startRow, endRow).getMyDataList();
	}
	/**
	 * 将Request中的文件上传到服务器，返回文件信息(安全模式)
	 * @param request
	 * @param fileTaskState
	 * @return
	 * @throws Exception
	 */
	/*private  static  FileTask createUploadFileBySafe(HttpServletRequest request,
			FileTaskState fileTaskState) throws Exception {
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (!multipartResolver.isMultipart(request))
			throw new MyExcelException("spring 解析上传组件出现异常");
		// 转换成多部分request
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

		// 取得request中的第一个文件名
		Iterator<String> iter = multiRequest.getFileNames();

		FileTask task = null;
		// while (iter.hasNext()) {
		if (iter.hasNext()) {
			// 取得上传文件,默认只处理一个文件
			MultipartFile file = multiRequest.getFile(iter.next());
			if (BaseHelper.isNullorEmpty(file))
				throw new MyExcelException("文件为空");

			task = new FileTask();
			task.setId(BaseHelper.getGuid());
			task.setFileTaskState(fileTaskState);
			task.setImportType("safe");

			// 取得当前上传文件的文件名称
			String myFileName = file.getOriginalFilename();
			task.setFileName(myFileName);

			// 生成真实的文件名
			String fileNameInServer = SaxPOI.createFileNameInServer(
					myFileName, task.getId());
			// 保存真实的文件名
			task.setFileNameInServer(fileNameInServer);

			// 保存服务器保存文件的路径
			String pathInServer = request.getSession().getServletContext()
					.getRealPath("")
					+ File.separator + "temp" + File.separator;
			task.setPathInServer(pathInServer);

			// 在服务器创建上传的文件
			String theWholePathFile = task.getPathInServer()
					+ task.getFileNameInServer();
			File localFile = new File(theWholePathFile);
			file.transferTo(localFile);

			// 保存文件行数
			long size = SaxPOI.getSize4TheFile(theWholePathFile);
			task.setSum((int) size);
			// 估计时间（存储为毫秒）
			if (task.getSum() != null) {
				double estimatedTime = 0.024 * task.getSum() * 60 * 1000;// 每条记录使用0.024分钟
				task.setEstimatedTime((long) estimatedTime);
			}

		}
		return task;
	}*/

	

	/**
	 * 采用SAX读取2007版的xlsx形式的Excel的最大行
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static long getSize4TheFile(String path) throws Exception {
		
		MyExcel2007ForMaxRow reader = new MyExcel2007ForMaxRow(path);
		return reader.maxRow - (SaxPOI.TITLELINE_ROW_INDEX + 1);

	}

	/**
	 * 将文件保存在server中
	 * 
	 * @param request
	 * @param file
	 * @return
	 * @throws MyExcelException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private  static  String createFileNameInServer(String myFileName, String id)
			throws MyExcelException, IllegalStateException, IOException {

		// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
		if (BaseHelper.isNullorEmpty(myFileName.trim())) {
			throw new MyExcelException("获取文件名为空");
		}

		// 为文件名字加上guid，如果没有后缀名，就直接加上guid
		int beginIndex = myFileName.lastIndexOf(".");
		String separator = "_";
		if (beginIndex > -1) {
			myFileName = myFileName.substring(0, beginIndex) +separator+ id
					+ myFileName.substring(beginIndex);

		} else {
			myFileName =separator+ id;
		}

		return myFileName;
	}

	
	/**
	 * 上传文件
	 * 规则： 将上传的文件临时保存服务器,此类文件处理完后必须删除
	 * @param request
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public  static  List<File> createUploadFile(HttpServletRequest request) throws Exception {
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (!multipartResolver.isMultipart(request)) {
			throw new MyExcelException("spring 解析上传组件出现异常");
		}
		// 转换成多部分request
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

		// 取得request中的第一个文件名
		Iterator<String> iter = multiRequest.getFileNames();

		List<File> files = new ArrayList<File>();
		Path path = PathUtil.getTempSavePath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		if (iter.hasNext()) {
			// 取得上传文件,默认只处理一个文件
			MultipartFile file = multiRequest.getFile(iter.next());
			if (BaseHelper.isNullorEmpty(file)) {
				throw new MyExcelException("文件为空");
			}

			// 取得当前上传文件的文件名称
			String myFileName = file.getOriginalFilename();

			// 生成真实的文件名
			String fileNameInServer = SaxPOI.createFileNameInServer(
					myFileName, sdf.format(new Date()));

			// 在服务器创建上传的文件
			File f = path.resolve(fileNameInServer).toFile();
			file.transferTo(f);
			files.add(f);
		}
		return files;
	}
}
