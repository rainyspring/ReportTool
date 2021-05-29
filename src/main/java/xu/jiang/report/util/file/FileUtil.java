package xu.jiang.report.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import xu.jiang.report.bean.KeyValue;
import xu.jiang.report.util.PathUtil;

/**
 * 文件帮助类 针对常用的文件操作以下参考类均有提供： 参考1：org.apache.commons.io.FileUtils 删除文件、递归删除文件目录
 * 参考2：java.nio.file.Files.copy复制/move移动
 * 
 * @see
 * @author jx
 * @date 2017年12月25日 下午2:16:24
 */
public class FileUtil {
	/**
	 * 读取文件编码
	 */
	public static final String READ_FILE_CHARSET_NAME = "UTF-8";
	/**
	 * 写文件编码
	 */
	public static final String WRITE_FILE_CHARSET_NAME = "UTF-8";
	/**
	 * 文件格式分割符 形如a.txt中的.
	 */
	public static final String FILE_TYPE_POINT = ".";
	/**
	 * 临时目录
	 */
	private static final Path DIR_TEMP_PATH = PathUtil.getTempSavePath(null);

	/**
	 * 
	 * @author jx
	 * @Title: getTempPath
	 * @Description: randomId==null时 randomId=UUID.randomUUID().toString()
	 * 只会获取一个临时目录，如果不存在不会创建该目录
	 * @param randomId
	 * @return 参数
	 */
	public static Path getTempPath(String randomId) {
		if (randomId == null || randomId.trim().equals("")) {
			randomId = UUID.randomUUID().toString();
		}
		return DIR_TEMP_PATH.resolve(randomId);
	}
	/**
	 * 与该函数getTempPath不同的是，这个目录一定存在，如果不存在就创建一个
	 * @author jx
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return
	 * @throws IOException 
	 */
	public static Path getOrCreateTempPath() throws IOException {
		Path  p = DIR_TEMP_PATH.resolve(UUID.randomUUID().toString());
		File f = p.toFile();
		if(f.exists()&&f.isDirectory()){
			FileUtils.deleteDirectory(f);
		}
		f.mkdirs();
		return p;
	}
	/**
	 * 
	 * @author jx
	 * @Description: 将接收CommonsMultipartFile类型的文件，并放在临时目录
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static File put2TempDir(CommonsMultipartFile f) throws IllegalStateException, IOException {
		String zipName = f.getOriginalFilename();

		File zipf = FileUtil.getTempPath(zipName).toFile();
		/*
		 * 因为是放在临时目录，所以不必担心重名问题，重名就就删了就好
		 */
		if (zipf.exists()) {
			zipf.delete();
		}
		f.transferTo(zipf);
		return zipf;
	}

	/**
	 * 
	 * @author jx
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param fileName
	 * @param withFileTypePoint
	 *            是否带有 . 如果没有后缀名，则return ""
	 *            如果.后面没有任何值，withFileTypePoint==true时，return . ;
	 *            withFileTypePoint==false,return ""
	 * @return
	 */
	public static String getSuffix(String fileName, boolean withFileTypePoint) {
		if (StringUtils.isBlank(fileName)) {
			return "";
		}
		int index = fileName.lastIndexOf(FILE_TYPE_POINT);
		if (index < 0) {
			return "";
		}
		if (withFileTypePoint) {
			return fileName.substring(index);
		}
		return fileName.substring(index + 1);
	}

	/**
	 * 返回名称前缀（不包括分割符）
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getPrefix(String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return "";
		}
		int index = fileName.lastIndexOf(FILE_TYPE_POINT);
		if (index < 0) {
			return fileName;
		}
		String suffix = fileName.substring(0, index);
		return suffix;
	}

	/**
	 * 将insertedValue插入的文件名中，紧贴后缀名 1 fileName==null or "" 时 return "" 2
	 * insertedValue=null or "" ,return fileName; 3
	 * fileName==a.txt，insertedValue==xx时 return axx.txt
	 * 
	 * @param fileName
	 *            文件名
	 * @param insertedValue
	 *            插入的值
	 * @return
	 */
	public static String insertValueIntoFileName(String fileName, String insertedValue) {
		if (StringUtils.isBlank(fileName)) {
			return "";
		}
		if (StringUtils.isBlank(insertedValue)) {
			return fileName;
		}
		return getPrefix(fileName) + insertedValue + getSuffix(fileName, true);
	}

	/**
	 * 统一下载任意类型文件（浏览器自动识别文件类型）
	 * 
	 * @param response
	 * @param file
	 * @param customFileName
	 * @throws UnsupportedEncodingException
	 */
	public static void downloadFile4AnyFile(HttpServletResponse response, File file, String customFileName)
			throws UnsupportedEncodingException {
		if (!file.exists()) {
			return;
		}
		if (StringUtils.isBlank(customFileName)) {
			customFileName = file.getName();
		}
		response.setCharacterEncoding("utf-8");
		// 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
		// response.setContentType("multipart/form-data");
		response.setContentType("application/octet-stream");
		// 2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
		response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(customFileName, "UTF-8"));
		ServletOutputStream out = null;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);

			// 3.通过response获取ServletOutputStream对象(out)
			out = response.getOutputStream();

			int b = 0;
			byte[] buffer = new byte[1024];
			while (b != -1) {
				b = inputStream.read(buffer);
				// 4.写到输出流(out)中
				if (b != -1) {
					out.write(buffer, 0, b);
				}

			}
			inputStream.close();
			out.flush();
			out.close();

		} catch (IOException e) {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/**
	 * 统一下载Excel类型文件
	 * 
	 * @param response
	 * @param file
	 * @param customFileName
	 * @throws UnsupportedEncodingException
	 */
	public static void downloadFile4Excel(HttpServletResponse response, File file, String customFileName)
			throws UnsupportedEncodingException {
		if (!file.exists()) {
			return;
		}
		if (StringUtils.isBlank(customFileName)) {
			customFileName = file.getName();
		}
		String suffix = FileUtil.getSuffix(customFileName, false);

		if (StringUtils.isBlank(suffix)) {
			suffix = "xlsx";
			customFileName += (FileUtil.FILE_TYPE_POINT + suffix);
		}

		response.setCharacterEncoding("utf-8");
		// response.setContentType("application/vnd.ms-excel");
		response.setContentType("application/x-xls");// application/pdf
		/**
		 * 解决下载的中文名无法显示的问题 response.setHeader("Content-Disposition",
		 * "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
		 * 
		 * fileName = fileName.Replace("+", " ");
		 */
		response.setHeader("Content-Disposition",
				"attachment;fileName=" + URLEncoder.encode(customFileName, "UTF-8").replace("+", " "));

		InputStream inputStream = null;
		OutputStream os = null;
		try {

			inputStream = new FileInputStream(file);

			os = response.getOutputStream();
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				os.write(b, 0, length);
			}

			// 这里主要关闭。
			os.close();
			inputStream.close();

		} catch (Exception e) {
			try {
				if (os != null) {
					os.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
		}
		// 返回值要注意，要不然就出现下面这句错误！
		// java+getOutputStream() has already been called for this response

	}

	/**
	 * 删除指定的文件夹
	 * 
	 * 
	 * org.apache.commons.io.FileUtils.deleteDirectory();
	 * @param dirPath
	 *            文件夹路径
	 * @param deleteRootDir
	 *            如果是目录的话，是否删除根
	 * @return 若删除成功，则返回True；反之，则返回False
	 *
	 */
	@Deprecated
	public static void delDir(Path dirPath, boolean deleteRootDir) {

		File file = dirPath.toFile();
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.listFiles() == null || file.listFiles().length == 0) {
			if (deleteRootDir) {

				file.delete();
			}
			return;
		}
		int zfiles = file.listFiles().length;
		File[] delfile = file.listFiles();
		for (int i = 0; i < zfiles; i++) {
			if (delfile[i].isDirectory()) {
				delDir(delfile[i].toPath(), true);
			}
			delfile[i].delete();
		}
		if (deleteRootDir) {
			file.delete();
		}
	}

	/**
	 * 文件名过滤器--去掉文件名后缀是temp的文件(系统临时文件)
	 * 
	 * @see
	 * @author jx
	 * @date 2017年6月5日 上午11:44:35
	 */
	/*public static class Fiter4Filename implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (!StringUtils.isBlank(name) && name.endsWith("tmp")) {
				return false;
			}
			return true;
		}

	}*/
	
	/**
	 * Java NIO包括transferFrom方法,根据文档应该比文件流复制的速度更快 只能是文件的复制 速度比Files.copy还快
	 * 
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	/**
	 * @note 存储离unzipFilePath最远的唯一单独文件夹
	 * @return 返回 “不能只有一个目录作为孩子的最深层的目录 ” 如果srcPath 是空文件夹 返回srcPath 如果srcPath
	 *         不是目录，返回srcPath的上层目录
	 */
	public static Path farthestAloneDirFrom(Path srcPath) {
		if (srcPath == null) {
			return null;
		}

		File farthestAloneDir = srcPath.toFile();
		if (!farthestAloneDir.isDirectory()) {
			return srcPath.getParent();
		}
		if (farthestAloneDir.listFiles() == null || farthestAloneDir.listFiles().length <= 0
				|| farthestAloneDir.listFiles().length > 1) {
			return srcPath;
		}
		/*
		 * 里面只有单独一个文件
		 */
		File oneFile = farthestAloneDir.listFiles()[0];
		return farthestAloneDirFrom(srcPath.resolve(oneFile.toPath()));
	}

	/**
	 * 
	 * @author jx
	 * @Description:读取属性文件
	 * @param config
	 * @return
	 */
	public static Properties getPropertyFile(File config) {
		if (config == null || config.isDirectory()) {
			return null;
		}

		InputStreamReader reader = null;
		FileInputStream in = null;

		try {
			Properties properties = new Properties();
			// GBK或GB2312,GB18030
			in = new FileInputStream(config);
			reader = new InputStreamReader(in, FileUtil.READ_FILE_CHARSET_NAME);
			properties.load(reader);

			reader.close();
			in.close();
			return properties;
		} catch (IOException e) {
			if (reader != null) {
				try {
					// io流是装饰模式，关闭最外层即可
					reader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (in != null) {
				try {
					// io流是装饰模式，关闭最外层即可
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 
	 * @author jx
	 * @Description: 将文件f中的内容替换
	 * @param f
	 * @param list<KeyValue>中 key为旧串；value为替换串
	 */
	public static void replaceStr4File(File f, List<KeyValue> list) {
		if(list==null||list.isEmpty()||f==null||f.isDirectory()){
			return;
		}
		BufferedReader br = null;
		PrintWriter out = null;
		try {
			/*
			 * 先临时生成一个文件
			 */
			File bakF = new File(f.getParent() + File.separator + UUID.randomUUID().toString()+".a");
			if(bakF.exists()){
				bakF.delete();
			}
			InputStreamReader in = new InputStreamReader(new FileInputStream(f), READ_FILE_CHARSET_NAME);
			br = new BufferedReader(in);
			out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bakF), WRITE_FILE_CHARSET_NAME)));

			for (String temp = null; (temp = br.readLine()) != null; out.write(temp + "\r\n")) {
				for(KeyValue v:list){
					if(!StringUtils.isBlank(v.getKey())&&temp.contains(v.getKey())){
						temp = temp.replace(v.getKey(), v.getValue());
					}
					
				}
				
			}

			br.close();
			out.flush();
			out.close();
			f.delete();
			bakF.renameTo(f);
		} catch (IOException e) {
			if(br!=null){
				try {
					br.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if(out!=null){
				out.close();
			}
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @author jx
	 * @Description: TODO(文件名是否包含禁用字符，针对windows而言)
	 *  英文的下列字符为禁用的    *   ?  "  <>  |  \  /   : 
	 * @param filename
	 * @return
	 */
	public static boolean containsForbiddenCharacters(String filename){
		if(filename.contains("*")
				||filename.contains("?")
				||filename.contains("\"")
				||filename.contains("<")
				||filename.contains(">")
				||filename.contains("|")
				||filename.contains("\\")
				||filename.contains("/")
				||filename.contains(":")){
			return true;
			
		}
		return false;
	}
	
}
