package com.fulong.utils.v2.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fulong.utils.MyConstant;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 这个帮助类专门处理 数据格式上的转换问题--多以json和其他格式互转
 * 
 * @author yhr
 * 
 */
/**
 * @author 姜旭
 * @date 2015-08-25
 */
public final class DataHelper {
	// private static Log log = LogFactory.getLog(DataHelper.class);
	/**
	 * 系统配置文件
	 */
	//private static Properties sysProperty;
	/**
	 * 语言配置文件
	 */
	private static Properties languageProperty;
	
	private static Map<String,String> languagePath;
	static {
		languagePath = new HashMap<String,String>();
		languagePath.put("zh_CN", "config/UI_zh_CN.properties");
		languagePath.put("en_US", "config/UI_en_US.properties");
		
	}
	/**
	 * 系统配置文件
	 */
	private static Properties reportProperty;
	/**
	 * 认证文件
	 */
	private static Properties licenseProperty;
	
	/**
	 * 将key=value的形式转成{"key":value}的json格式
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> String getJsonObject(String key, T value) {
		if (value instanceof Integer) {
			return "{\"" + key + "\":" + value + "}";
		} else if (value instanceof Boolean) {
			return "{\"" + key + "\":" + value + "}";
		} else {
			return "{\"" + key + "\":\"" + value + "\"}";
		}

	}

	/**
	 * 将key=value的形式转成"key":value的json格式片段,不包含逗号
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> String getJsonString(String key, T value) {
		if (value instanceof Integer) {
			return " \"" + key + "\":" + value + " ";
		} else if (value instanceof Boolean) {
			return " \"" + key + "\":" + value + " ";
		} else {
			return " \"" + key + "\":\"" + value + "\" ";
		}

	}

	/**
	 * 将bean、map转换成对象json串
	 * 
	 * @param t
	 * 			@return{"key":false,"key2":"ddd","key3":99
	 */
	public static <T> String getJsonObject(T t) {
		/*
		 * StringBuffer jsonObject = new StringBuffer("{"); for(String key:
		 * map.keySet()){ String value = map.get(key); String
		 * json_o="\""+key+"\":\""+value+"\","; jsonObject.append(json_o); } int
		 * index = jsonObject.toString().lastIndexOf(","); String newS
		 * =jsonObject.toString().substring(0, index)+"}";
		 */
		return JSONObject.fromObject(t).toString();
	}

	/**
	 * 将list<bean>转换成数组对象的json串
	 * 
	 * @param t
	 * @return [{},{},{}]
	 */
	public static <T> String getJsonArray(T t) {
		return JSONArray.fromObject(t).toString();
	}

	/**
	 * 经过测试 在新增和编辑无需转换，而datagrid 显示需要转换
	 * 
	 * 专门针对datagrid的展现，如果json数据中不能包含html代码 将json串中的不合法字符转义：即双引号 ":\"
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static String filterJsonHtmlStr4Datagrid(String jsonStr) {
		// &apos;----单引号
		// &quot;-----双引号

		// 万全之策：全部转换
		if (jsonStr == null) {
			return "";
		}
		// 最大集合
		// return
		// jsonStr.replaceAll("\n","<br />").replaceAll("\t\r","<br
		// />").replaceAll("\r\t","<br
		// />").replaceAll("\r","&nbsp;").replaceAll("\r","&nbsp;").replaceAll("\"",
		// "&quot;").replaceAll("'", "&apos;").replaceAll("<",
		// "&lt;").replaceAll(">",
		// "&gt;").replaceAll("\t\r","&nbsp;").replaceAll("\r\t","&nbsp;").replaceAll("
		// ",
		// "&nbsp;");
		// 最小集合
		// \s 匹配任何空白字符，包括空格、制表符、换页符等等。等价于 [ \f\n\r\t\v]。
		return jsonStr.replace("\\", "\\\\").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\n", "<br />").replaceAll("[\\s]", "&nbsp;&nbsp;");

	}

	



	
	/**
	 * 获取指定格式的整数
	 * @param number 整数>=0
	 * @param level 总位数；
	 * 		如果number总位数小于level,自动前缀补齐0，使总位数到达level；
	 * 		如果number超出位数，则直接返回number
	 * 	
	 * 
	 * @return 
	 * 异常情况：
	 * if number<0 then return ""
	 * if level<=0 then return number
	 * 
	 */
	public static String getFormatInteger(int number,int level) {
		if(number<0) {
			return "";
		}
		if(level<=0) {
			return String.valueOf(number);
		}
		
		String str = String.valueOf(number);
		int len = str.length();
		if(len>=level) {
			return String.valueOf(number);
		}
		
		int cha = level-len;//差值
		char[] chars = new char[cha];
		for(int i=0;i<cha;i++){
			chars[i] = '0';
		}
		
		return String.valueOf(chars)+str;
	}
	
	
	/**
	 * 获取安装目录
	 * @return
	 */
	@Deprecated
	public static Path getInstallHome(String... dir ){
		Path home = PathUtil.getRootPath().getParent().getParent().getParent();
		//String home = getRootPath().toString().replace("construction", "").replace("webapps", "").replace("tomcat", "");
		return Paths.get(home.toString(),dir);
		
	}

	
	

	/**
	 * 保留三位小数
	 * 
	 * @param n
	 * @return
	 */
	public static double formatDouble(Double n) {
		if (n == null) {
			return 0.000;
		}
		return new BigDecimal(n).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	

	/**
	 * 获取系统配置文件（非单例）
	 * @return
	 */
	public static String loadSysProperty(String sysKey) {
		/*if(sysProperty!=null){
			return sysProperty.getProperty(sysKey);
		}*/
		// 数据库连接类型
		Properties sysProperty = loadProperty(MyConstant.CONFIGFILE_GLOBAL);
		
		return sysProperty.getProperty(sysKey);
	}
	/**
	 * 获取推送配置文件
	 * @return
	 */
	public static Properties loadPropellingProperty() {
		/**
		 * 推送文件
		 */
		Properties	propellingProperty = loadProperty(MyConstant.CONFIGFILE_PROPELLING);
		
		return propellingProperty;
	}
	/**
	 * 获取集群配置文件
	 * @return
	 */
	public static Properties loadClusterProperty() {
		/**
		 * 推送文件
		 */
		Properties	clusterProperty = loadProperty(MyConstant.CONFIGFILE_CLUSTER);
		
		return clusterProperty;
	}
	/**
	 * 判断配置文件是否存在
	 * @param args
	 * @throws IOException
	 */
	public static boolean exist4ConfigFile(String configfile) throws IOException {
		
		//URL url = DataHelper.class.getClassLoader().getResource(MyConstant.configFile_propelling);
		//(1)类的绝对路径：Class.class.getClass().getResource("/").getPath() 
		//结果：/D:/TEST/WebRoot/WEB-INF/classes/pack/ 
		//(2)得到工程的路径：System.getProperty("user.dir") 
		
		String path = DataHelper.class.getResource("/").getPath();
		//String path = Datahelper.class.getResource("/").getPath();
		//System.out.println(path);
		if(new File(path+configfile).exists()){
			return true;
		}
		return false;
		
	}
	/*public static void main(String[] args) throws IOException {
		//boolean a = new File("xxx/jiangx.sldfs").exists();
		System.out.println();
	}*/
	/**
	 * 获取证书配置文件(单例)
	 * @return
	 */
	public static String loadLicenseProperty(String sysKey) {
		if(licenseProperty!=null){
			return licenseProperty.getProperty(sysKey);
		}
		//
		licenseProperty = loadProperty(MyConstant.CONFIGFILE_LICENSE);
		
		return licenseProperty.getProperty(sysKey);
	}
	
	/**
	 * 获取报表配置文件(单例)
	 * @return
	 */
	public static String loadReportProperty(String sysKey) {
		if(reportProperty!=null){
			return reportProperty.getProperty(sysKey);
		}
		// 数据库连接类型
		reportProperty = loadProperty(MyConstant.CONFIGFILE_REPORT);
		
		return reportProperty.getProperty(sysKey);
	}
	/**
	 * 获取配置文件（如果不存在，则返回一个空的property）
	 * @param fileName
	 * @return
	 */
	private static Properties loadProperty(String path) {
		Properties p = new Properties();
		if(StringUtils.isBlank(path)) {
			return p;//放回一个空的property
		}
		
		// 数据库连接类型
		InputStream is = DataHelper.class.getClassLoader().getResourceAsStream(path);
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(is, "UTF-8");
			p.load(reader);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				is.close();
				if(reader!=null){
					reader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return p;
	}
	/**
	 * 获取根据语言文件映射的对应文字
	 * @param key
	 * @return
	 */
	public static String getLanguageValueBy(String key){
		if(StringUtils.isBlank(key)) {
			return "";
		}
		
		if(languageProperty!=null){
			return languageProperty.getProperty(key);
		}
		//加载语言文件
		String languageStyle = loadSysProperty("language.style");
		return loadProperty(languagePath.get(languageStyle)).getProperty(key);
		
	}
	/**
	 * 根据语言获取配置文件的对应key的value值，如果不存在则使用原始值
	 * @param key
	 * @return
	 */
	public static String filterValueBy(String key){
		String s = getLanguageValueBy(key);
		if(StringUtils.isBlank(s)){
			return key;
		}
		return s;
		
	}

	/**
	 * 文件存在就删除
	 * @deprecated
	 * @param fileWithPath
	 */
	@Deprecated
	public static void deleteIfExists(String fileWithPath) {
		if (StringUtils.isBlank(fileWithPath)) {
			return;
		}
		Path p = new File(fileWithPath).toPath();
		try {
			Files.deleteIfExists(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	/**
	 * 获取客户端ip
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientIP(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
															// = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

	// 获取浏览器类型
	public static String getBrowser(HttpServletRequest request) {
		String value = request.getHeader("USER-AGENT");
		if (value.indexOf("MSIE") > -1) {
			value = value.substring(value.indexOf("MSIE"));
			value = value.split(";")[0].substring(2);
		} else if (value.indexOf("Firefox") > -1) {
			value = value.substring(value.indexOf("Firefox"));
		} else if (value.indexOf("Chrome/45.0.2454.93") > -1) {
			value = value.substring(value.indexOf("Chrome"));
		} else if (value.indexOf("Chrome/31.0.1650.63") > -1) {
			value = "360";
		}
		return value;
	}

	/**
	 * 获取操作系统
	 * 
	 * @param request
	 * @return
	 */
	public static String getPlatform(HttpServletRequest request) {
		// 操作系统信息
		String osName = System.getProperty("os.name");
		// 操作系统版本
		String osVersion = System.getProperty("os.version");
		String value = "";
		value = osName + "-" + osVersion;
		return value;
	}

}
