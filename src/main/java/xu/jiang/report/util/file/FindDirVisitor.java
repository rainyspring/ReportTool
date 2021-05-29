package xu.jiang.report.v2.tool.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * 文件夹遍历器，将文件复制到目标目录 注意：目标目录不能使文件的子目录（不然会无限递归）
 * 
 * @see
 * @author jx
 * @date 2018年2月1日 下午5:46:24
 */
public class FindDirVisitor extends SimpleFileVisitor<Path> {
	public Path srcDir;
	public Path distDir;
	
	private int srcLength = 0;
	/**
	 * 
	 * @param srcDir
	 * @param distDir
	 * @param isCopySrcDir 是否拷贝源目录名
	 */
	public FindDirVisitor(Path srcDir,Path distDir,boolean isCopySrcDir) {
		this.srcDir = srcDir;
		this.srcLength = this.srcDir.toFile().getAbsolutePath().length();
		
		
		this.distDir = distDir;
		if(isCopySrcDir){
			this.distDir = distDir.resolve(srcDir.getFileName());
		}
		if (!this.distDir.toFile().exists()) {
			this.distDir.toFile().mkdirs();
		}
	}

	/**
	 *
	 * @param file
	 *            都是带路径的文件，文件夹不在遍历当中
	 * @param attrs
	 * @return
	 * @throws IOException
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

		/*
		 * 获取文件的目录
		 */
		Path parent = file.getParent();
		Path temp = this.distDir;
		if(!parent.toFile().getAbsolutePath().equals(this.srcDir.toFile().getAbsolutePath())){
			/*
			 * 截取相对目录
			 */
			Path subpath = Paths.get(parent.toFile().getAbsolutePath().substring(this.srcLength+1));
			
			/*
			 * 文件的复制
			 */
			temp = this.distDir.resolve(subpath);	
		}
		
		if(!temp.toFile().exists()){
			temp.toFile().mkdirs();
		}
		Files.copy(file, temp.resolve(file.getFileName()));

		return super.visitFile(file, attrs);
	}
	/**
	 * 案例
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * 文件夹的复制
		 */
		Path src = Paths.get("e:/a");
		Path dist = Paths.get("e:/b");
		Files.walkFileTree(src, new FindDirVisitor(src,dist,false));
		
		Path a = Paths.get("e:/a");
		Files.delete(a);
		
	
		Path tempDrawingDir = Paths.get("D:/");
		Path drawingDir = Paths.get("E:/");
		
		//第一层：遍历每个图档文件
		Files.walkFileTree(tempDrawingDir, new SimpleFileVisitor<Path>() {
			
			//当访问每个文件时
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				
				
				//遍历最终的图档目录，查看文件列表是否与file重名，如果重名，就把file改名
				Files.walkFileTree(drawingDir, new SimpleFileVisitor<Path>() {
					
					//当访问每个文件时
					@Override
					public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
						
						
						
						
						if (file. toFile().getName().equalsIgnoreCase(f.toFile().getName())) {
							
							File newFile = getAvaliableFileName(drawingDir.toFile(),file.toFile());
							//真正的改名了！
							file.toFile().renameTo(newFile);
							
							return FileVisitResult.TERMINATE; // 找到了就终止
						}
						return FileVisitResult.CONTINUE; // 没找到继续找
					}
				});
				
				
				/*if (file. toFile().getName().equalsIgnoreCase("Test.java")) {
					
					
					
					
					return FileVisitResult.TERMINATE; // 找到了就终止
				}*/
				return FileVisitResult.CONTINUE; // 没找到继续找
			}
		});
		
		/*File f = new File("");
		List<File> list = new ArrayList<>();
		String prefixName = FileUtil.getPrefix(f.getName());
		String suffixName = FileUtil.getSuffix(f.getName(), true);
		for(File file:list) {
			String tempPrefixFile = FileUtil.getPrefix(file.getName());
			if(tempPrefixFile.equalsIgnoreCase(prefixName)) {
				File tempNewFileName = f.toPath().getParent().resolve(prefixName+"_bak"+suffixName).toFile();
				
			}
		}*/
		

	}
	
	/**
	 * 
	 * @author: jx   
	 * @Title: getAvaliableFileName   
	 * @Description: TODO(判断f是否在list中重名，如果重名添加固定后缀"_bak")   
	 * @param list
	 * @param f 如果重名，返回新的不重名文件，如果不重名，则源文件返回即可
	 * @return      
	 * @throws
	 */
	private static File getAvaliableFileName(File outDir,File f) {
		String prefixName = FileUtil.getPrefix(f.getName());
		String suffixName = FileUtil.getSuffix(f.getName(), true);
		
		for(File file:outDir.listFiles()) {
			String tempPrefixFile = FileUtil.getPrefix(file.getName());
			if(tempPrefixFile.equalsIgnoreCase(prefixName)) {
				File tempNewFile = f.toPath().getParent().resolve(prefixName+"_bak"+suffixName).toFile();
				return getAvaliableFileName(outDir,tempNewFile);
			}
		}
		return f;
	}

}
