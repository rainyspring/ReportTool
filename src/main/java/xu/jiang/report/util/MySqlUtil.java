package xu.jiang.report.v2.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.internal.FileHelper;

/**
 * 处理java执行sql文件的辅助类
 * 
 * @see
 * @author jx
 * @date 2017年4月10日 上午8:33:22
 */
public final class MySqlUtil {
	private static final String EXPORT_PATH = "database.sql";
	public static final String ZHONGWEN_REGEX = ".*[\u4E00-\u9FA5]+.*";
	/**
	 * 执行一条命令
	 * @deprecated
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int execOneCommand_bak(String command) throws IOException, InterruptedException {
		if (StringUtils.isBlank(command)) {
			return 0;
		}
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command);// 这里简单一点异常我就直接往上抛
		InputStream stderr = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stderr);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		// System.err.println("<msg>----------------------------");
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		int exitVal = proc.waitFor();
		/**
		 * exitVal==0正常，非0不正常
		 */
		System.err.println("Process exitValue[0正常，非0不正常]: " + exitVal);
		br.close();
		isr.close();
		stderr.close();
		// System.err.println("----------------------------</msg>");
		return exitVal;
	}
	/**
	 * 执行一条命令
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int execOneCommand(String command) throws IOException, InterruptedException {
		if (StringUtils.isBlank(command)) {
			return 0;
		}
		final Process proc = Runtime.getRuntime().exec(command);// 这里简单一点异常我就直接往上抛
		
		printMessage(proc.getInputStream());
	    printMessage(proc.getErrorStream());
		int exitVal = proc.waitFor();
		/**
		 * exitVal==0正常，非0不正常
		 */
		System.err.println("Process exitValue[0正常，非0不正常]: " + exitVal);
		
		// System.err.println("----------------------------</msg>");
		return exitVal;
	}
	/**
	 * 现在来讲讲exitValue()，当线程没有执行完毕时调用此方法会跑出IllegalThreadStateException异常，
	 * 最直接的解决方法就是用waitFor()方法代替。
	 * 但是waitFor()方法也有很明显的弊端，因为java程序给进程的输出流分配的缓冲区是很小的，有时候当进程输出信息
	 * 很大的时候回导致缓冲区被填满，如果不及时处理程序会阻塞。如果程序没有对进程的输出流处理的会就会导致执行exec()
	 * 的线程永远阻塞，进程也不会执行下去直到输出流被处理或者java程序结束。
	 * 解决的方法就是处理缓冲区中的信息，开两个线程分别去处理标准输出流和错误输出流
	 * @param input
	 */
	private static void printMessage(final InputStream input) {
	    new Thread(new Runnable() {
	    	@Override
	       public void run() {
		        Reader reader = new InputStreamReader(input);
		        BufferedReader bf = new BufferedReader(reader);
		        String line = null;
				try {
					while((line=bf.readLine())!=null) {
						System.out.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
	       }
		}).start();
	 }
	/**
	 * 执行2条命令 exitVal==0正常，非0不正常
	 * 
	 * @param properties
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int execTwoCommands(String[] cmdArray) throws IOException, InterruptedException {
		if (cmdArray == null || cmdArray.length <= 0) {

			return 0;
		}
		if (cmdArray.length == 1) {
			return MySqlUtil.execOneCommand(cmdArray[0]);
		}
		Runtime runtime = Runtime.getRuntime();

		Process process = runtime.exec(cmdArray[0]);
		// 执行了第一条命令以后已经登录到mysql了，所以之后就是利用mysql的命令窗口
		// 进程执行后面的代码
		OutputStream os = process.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(os);
		// 命令2和命令1的交互
		writer.write(cmdArray[1] + "\r\n");

		writer.flush();
		writer.close();
		os.close();

		InputStream stderr = process.getErrorStream();
		InputStreamReader isr = new InputStreamReader(stderr);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		System.out.println("<msg>-------importData-----start----------------");
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		int exitVal = process.waitFor();
		/**
		 * exitVal==0正常，非0不正常
		 */
		System.out.println("Process exitValue[0正常，非0不正常]: " + exitVal);
		br.close();
		isr.close();
		stderr.close();

		// long end = System.currentTimeMillis();
		System.out.println("-------importData------end---------------</msg>");

		return exitVal;
	}

	/**
	 * 利用属性文件提供的配置来拼装命令语句 在拼装命令语句的时候有一点是需要 注意的：一般我们在命令窗口直接使用命令来
	 * 进行导出的时候可以简单使用“>”来表示导出到什么地方， 即mysqldump -uusername -ppassword databaseName
	 * > exportPath， 但在Java中这样写是不行的，它需要你用-r明确的指出导出到什么地方，如： mysqldump -uusername
	 * -ppassword databaseName -r exportPath。
	 * 
	 * @param properties
	 * @param filterZhongWen
	 *            是否过滤中文 如果发现中文，则使用根路径（对应盘符）作为导出路径
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Deprecated
	private static int backupDB() throws IOException, InterruptedException {
		// Properties properties = DataHelper.loadSysProperty();

		String username = DataHelper.loadSysProperty("backup.username");// 用户名
		String password = DataHelper.loadSysProperty("backup.password");// 用户密码
		String exportDatabaseName = DataHelper.loadSysProperty("backup.dbName");
		String host = DataHelper.loadSysProperty("backup.host");// 从哪个主机导出数据库，如果没有指定这个值，则默认取localhost
		String port = DataHelper.loadSysProperty("backup.port");// 使用的端口号
		// 应该存在服务器// 导出路径
		Path path = PathUtil.getTempSavePath(EXPORT_PATH);
		Files.deleteIfExists(path);

		/*
		 * //如果发现中文，则使用根路径（对应盘符）作为导出路径
		 * if(filterZhongWen&&path.matches(zhongWenRegex)){ path = new
		 * File(path).toPath().getRoot().toString()+exportPath; }
		 */

		// 注意哪些地方要空格，哪些不要空格
		String command = getPrexCommond4Mysql("mysqldump")+" -u" + username + " -p" + password + " -h" + host + " -P" + port
				+ " --default-character-set=utf8 " + exportDatabaseName + " -r\"" + path+"\"";
		
		System.err.println("backupDB准备执行mysql命令:" + command);
		return MySqlUtil.execOneCommand(command);
	}
	
	/**
	 * 建库命令
	 * 
	 * @param propertiesms
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Deprecated
	private static int createDB(String dbName) throws IOException, InterruptedException {

		// Properties pro = DataHelper.loadSysProperty();
		String username = DataHelper.loadSysProperty("importDB.username");// 用户名
		String password = DataHelper.loadSysProperty("importDB.password");// 密码
		String host = DataHelper.loadSysProperty("importDB.host");// 导入的目标数据库所在的主机
		String port = DataHelper.loadSysProperty("importDB.port");// 使用的端口号
		String importDatabaseName = dbName;// 导入的目标数据库的名称

		if (StringUtils.isBlank(importDatabaseName)) {
			importDatabaseName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		}
		String command = getPrexCommond4Mysql("mysql")+" -u" + username + " -p" + password + " -h" + host + " -P" + port
				+ " -e\"create database " + importDatabaseName
				+ " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci \"";

		
		System.err.println("createDB准备执行mysql命令:" + command);
		return MySqlUtil.execOneCommand(command);
	}

	/**
	 * 利用命令mysql -uroot -proot -DdbName -e "sql" source 命令是mysql内部执sql文件的命令
	 * 
	 * @param map
	 * @param filterZhongWen4Path
	 *            路径是否过滤中文 如果发现中文，则使用根路径（对应盘符）作为导出路径
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Deprecated
	private static int executeSqlFile(String dbName, String fileNameWithPath, boolean filterZhongWen4Path)
			throws IOException, InterruptedException {
		MySqlParams i = getMysqlParams();		
		//System.out.println(username+"--"+password+"--"+host+"--"+port);
		return executeSqlFile(dbName, fileNameWithPath, filterZhongWen4Path, i.username, i.pwd, i.host, i.port);

	}
	
	private static MySqlParams mysqlParamsInstance= null; 
	public static synchronized MySqlParams getMysqlParams(){
		if(mysqlParamsInstance==null){
			String username = DataHelper.loadSysProperty("main.username");// 用户名
			String pwd = DataHelper.loadSysProperty("main.password");// 密码

			String url = DataHelper.loadSysProperty("main.url");
			String temp1 = url.replace("jdbc:mysql://", "");
			String[] arr = temp1.split("\\?")[0].split(":");

			String host = arr[0];// 导入的目标数据库所在的主机
			String[] portAndDBName = arr[1].split("/");
			String port = portAndDBName[0];
			String dbName = portAndDBName[1];
			
			mysqlParamsInstance = new MySqlParams();
			mysqlParamsInstance.username = username;
			mysqlParamsInstance.pwd = pwd;
			mysqlParamsInstance.url = url;
			mysqlParamsInstance.host = host;
			mysqlParamsInstance.port = port;
			mysqlParamsInstance.dbName=dbName;
		}
		return mysqlParamsInstance;
	}
	public static class MySqlParams{
		public String username;
		public String pwd;
		public String url;
		public String host;
		public String port;
		public String dbName;
		
	}
	/**
	 * 利用命令mysql -uroot -proot -DdbName -e "sql" source 命令是mysql内部执sql文件的命令
	 * 
	 * @param map
	 * @param filterZhongWen4Path
	 *            路径是否过滤中文 如果发现中文，则使用根路径（对应盘符）作为导出路径
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Deprecated
	private static int executeSqlFile(String dbName, String fileNameWithPath, boolean filterZhongWen4Path,String username,String password,String host,String port)
			throws IOException, InterruptedException {
		// Properties pro = DataHelper.loadSysProperty();
		username = username==null?DataHelper.loadSysProperty("importDB.username"):username;// 用户名
		password = password==null?DataHelper.loadSysProperty("importDB.password"):password;// 密码
		host = host==null?DataHelper.loadSysProperty("importDB.host"):host;// 导入的目标数据库所在的主机
		port = port==null?DataHelper.loadSysProperty("importDB.port"):port;// 使用的端口号

		
		String importDatabaseName = dbName;// 导入的目标数据库的名称
		// 应该存在服务器
		String importPath = fileNameWithPath;
		if (StringUtils.isBlank(fileNameWithPath)) {
			importPath = PathUtil.getTempSavePath(EXPORT_PATH).toString();// 导入的目标文件所在的位置
		}

		// 如果发现中文，则使用根路径（对应盘符）作为导出路径
		if (filterZhongWen4Path && importPath.matches(ZHONGWEN_REGEX)) {
			File oldFile = new File(importPath);
			// 获取根盘符+文件名
			importPath = oldFile.toPath().getRoot().toString() + oldFile.getName();
			//有修改。。。？？？？
			FileHelper.copyFile(oldFile,new File(importPath));
			
		}
		String command = getPrexCommond4Mysql("mysql")+" -u" + username + " -p" + password + " -h" + host + " -P" + port
				+ " --default-character-set=utf8  -D" + importDatabaseName;

		// 第二步，获取导入的命令语句 (由于mysql 中source命令的规则，
		// \后边不能是. ；所以如果未见名带有点就麻烦了)，故将\改为/
		command += " -e \"source " + importPath.replace("\\", "/") + "\"";
		System.err.println("executeSqlFile准备执行mysql命令:" + command);
		return MySqlUtil.execOneCommand(command);

	}
	/**
	 * 命令前缀
	 * 原因：(在win7 、win10都不用加前缀，只有XP系统必须加前缀)
	 * 之所以xp无法直接使用环境变量的值，是因为inno-setup安装后写入环境变量（注册表）的值
	 * 无法立即生效，必须重启，如果不重启的话，必须使用指定目录的命令
	 * @return
	 */
	public static String getPrexCommond4Mysql(String dir){
		/*Path mysqlHome = DataHelper.getInstallHome("").resolve("mysql").resolve("bin");
		return "cmd /c "+Paths.get(mysqlHome.toString(),dir);
		*/
		
		//还是决定使用环境变量的值
		return dir;
		
	}
	
	/**
	 * 执行一个sql文件
	 * @param sqlFile
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int executeSqlFile(File sqlFile)
			throws IOException, InterruptedException {
		if(!sqlFile.exists()) {
			return -99;
		}
		MySqlParams i = getMysqlParams();		
		
		// 应该存在服务器
		String importPath = sqlFile.getAbsolutePath();

		// 如果发现中文，则使用根路径（对应盘符）作为导出路径
		if (importPath.matches(ZHONGWEN_REGEX)) {
			File oldFile = new File(importPath);
			// 获取根盘符+文件名
			importPath = oldFile.toPath().getRoot().toString() + oldFile.getName();
			//有修改。。。？？？？
			FileHelper.copyFile(oldFile,new File(importPath));
			
		}
		String command = getPrexCommond4Mysql("mysql")+" -u" + i.username + " -p" + i.pwd + " -h" + i.host + " -P" + i.port
				+ " --default-character-set=utf8  -D" + i.dbName;

		// 第二步，获取导入的命令语句 (由于mysql 中source命令的规则，
		// \后边不能是. ；所以如果未见名带有点就麻烦了)，故将\改为/
		command += " -e \"source " + importPath.replace("\\", "/") + "\"";
		//System.err.println("executeSqlFile准备执行mysql命令:" + command);
		return MySqlUtil.execOneCommand(command);

	}
	
}
