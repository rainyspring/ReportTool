package com.fulong.utils.v2.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.fulong.utils.v2.tool.file.FileUtil;

public class ZipUtil {

	/**
	 * 递归压缩文件夹
	 * 
	 * @param file
	 *            当前待压缩的文件或目录对象；
	 * @param zos
	 *            输出--压缩文件存储对象
	 * @throws Exception
	 */
	private static void zip(File file, ZipOutputStream zipOut, String prefix) throws Exception {
		if (file == null) {
			return;
		}

		// 如果是文件，则直接压缩该文件
		if (file.isFile()) {

			int count, bufferLen = 1024;
			byte data[] = new byte[bufferLen];

			// 获取文件相对于压缩文件夹根目录的子路径
			ZipEntry entry = new ZipEntry(prefix + file.getName());
			zipOut.putNextEntry(entry);

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			while ((count = bis.read(data, 0, bufferLen)) != -1) {
				zipOut.write(data, 0, count);
			}
			bis.close();
			zipOut.closeEntry();
		}
		// 空目录
		else if (file.listFiles() == null || file.listFiles().length <= 0) {
			ZipEntry zipEntry = new ZipEntry(prefix + file.getName() + File.separator);
			zipOut.putNextEntry(zipEntry);
			zipOut.closeEntry();
		}
		// 如果是目录，则压缩整个目录
		else {
			File[] childFileList = file.listFiles();

			prefix += file.getName() + File.separator;
			for (int n = 0; n < childFileList.length; n++) {
				// File f = childFileList[n];
				zip(childFileList[n], zipOut, prefix);
			}
		}
	}

	/**
	 * 对文件或文件目录进行压缩
	 * 
	 * @param srcPath
	 *            要压缩的源文件路径。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
	 * @param zipPath
	 *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹
	 * @param zipFileName
	 *            要压缩的文件名
	 * @return File 压缩后的文件
	 * @throws Exception
	 */
	public static File zip(Path srcPath, Path zipPath, String zipFileName) throws Exception {
		if (StringUtils.isEmpty(zipFileName)) {
			zipFileName = "temp.zip";
		}
		CheckedOutputStream cos = null;
		ZipOutputStream zos = null;
		try {
			File srcFile = srcPath.toFile();

			// 判断压缩文件保存的路径是否为源文件路径的子文件夹，如果是，则抛出异常（防止无限递归压缩的发生）
			if (srcFile.isDirectory() && zipPath.toString().indexOf(srcPath.toString()) != -1) {
				throw new Exception("zipPath must not be the child directory of srcPath.");
			}

			// 判断压缩文件保存的路径是否存在，如果不存在，则创建目录
			File zipDir = zipPath.toFile();
			if (!zipDir.exists() || !zipDir.isDirectory()) {
				zipDir.mkdirs();
			}

			// 创建压缩文件保存的文件对象
			Path zipFilePath = zipPath.resolve(zipFileName);
			File zipFile = zipFilePath.toFile();
			if (zipFile.exists()) {
				// 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
				// SecurityManager securityManager = new SecurityManager();
				// securityManager.checkDelete(zipFilePath.toString());
				// 删除已存在的目标文件
				zipFile.delete();
			}

			cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());
			zos = new ZipOutputStream(cos);

			// 调用递归压缩方法进行目录或文件压缩
			zip(srcPath.toFile(), zos, "");
			zos.flush();
			zos.close();
			return zipFile;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (zos != null) {
					zos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解压缩zip包
	 * 只能实现当前zip包解压，无法递归解压
	 * @param zipFile
	 *            zip文件的全路径
	 * @param unzipFilePath
	 *            解压后的文件保存的路径
	 * @param includeZipFileName
	 *            解压后的文件保存的路径是否包含压缩文件的文件名。true-包含；false-不包含
	 * @return  Path 返回不是只有一个目录作为孩子的最深层的目录     
	 */
	@SuppressWarnings("unchecked")
	public static Path unzip(File zipFile, Path unzipFilePath, boolean includeZipFileName) throws Exception {

		// 如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
		if (includeZipFileName) {
			String fileName = zipFile.getName();
			if (StringUtils.isNotEmpty(fileName)) {
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
			}
			unzipFilePath = unzipFilePath.resolve(fileName);
		}
		// 创建解压缩文件保存的路径
		File unzipFileDir = unzipFilePath.toFile();
		if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
			unzipFileDir.mkdirs();
		}
		
		// 开始解压
		int count = 0;
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
//		ZipFile zip = new ZipFile(zipFile,Charset.forName("UTF-8"));
		/*
		 * 获取操作系统的默认字符编码
		 */
		String charSet4OS = System.getProperty("sun.jnu.encoding");
		ZipFile zip = new ZipFile(zipFile,Charset.forName(charSet4OS));
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		// 循环对压缩包里的每一个文件进行解压
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			// 构建压缩包中一个文件解压后保存的文件全路径
			Path entryFilePath = unzipFilePath.resolve(entry.getName());

			// 创建解压文件
			File entryFile = entryFilePath.toFile();
			/**
			 * 看了entry.isDirectory()为endsWith("/")来判断目录
			 */
			if(entry.isDirectory()||entry.getName().endsWith(File.separator)){//是目录,必须使用ZipEntry，使用File无法判断
				
				if(entryFile.exists()){
					entryFile.delete();
				}
				entryFile.mkdirs();
				continue;
			}
			
			if(!entryFile.getParentFile().exists()){
				entryFile.getParentFile().mkdirs();
			}
			if (entryFile.exists()) {
				// 删除已存在的目标文件
				entryFile.delete();
			}

			// 写入文件
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile));
			BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(entry));
			while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
				bos.write(buffer, 0, count);
			}
			bos.flush();
			bos.close();
			bis.close();
		}
		zip.close();
		
		return FileUtil.farthestAloneDirFrom(unzipFilePath);
	}
	
	
	public static void main(String[] args) {

		Path zipPath = Paths.get("d:/a");
		Path dir = Paths.get("d:/b");
		String zipFileName = "test.zip";
		try {
			zip(zipPath, dir, zipFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		 
		File zipFile = dir.resolve(zipFileName).toFile();
		Path unzipFilePath = Paths.get("d:/c");
		try {
			unzip(zipFile, unzipFilePath, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
	}
}
