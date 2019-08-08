package com.fulong.utils.v1.poi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollableResults;

public class TXTWriter {
	private static final String SEPARATOR_COL="|";
	private File modelFile;
	private File outFile;
	private FileWriter writer;
	private List<String> title;
	public TXTWriter(File modelFile, File outFile) throws IOException {
		this.modelFile = modelFile;
		this.outFile = outFile;
		//先清掉遗留的旧的执行记录
		if(this.outFile.exists()){
			this.outFile.delete();
			this.outFile.createNewFile();
		}
		
		this.writer = new FileWriter(outFile,true);

	}
	public TXTWriter(List<String> title, File outFile) throws IOException {
		this.title = title;
		this.outFile = outFile;
		//先清掉遗留的旧的执行记录
		if(this.outFile.exists()){
			this.outFile.delete();
			this.outFile.createNewFile();
		}
		
		this.writer = new FileWriter(outFile,true);

	}
	/**
	 * 向txt中写入标题
	 * @throws IOException 
	 */
	private void printTitle() throws IOException{
		if(this.modelFile!=null&&this.modelFile.exists()){
			//将模板文件的中的内容写入到输出文件中
			FileReader  fr = new FileReader (modelFile);
			BufferedReader br=new BufferedReader(fr);
			String line="";
			String[] arrs=null;
	        while ((line=br.readLine())!=null) {
	           this.writer.append(line+"\r\n");
	        }
	        br.close();
	        fr.close();   
		}else if(this.title!=null&&this.title.size()>0){
			String str ="";
			for(String s:title){
				str += (s+TXTWriter.SEPARATOR_COL);
			}
			this.writer.append(str.substring(0, str.lastIndexOf(TXTWriter.SEPARATOR_COL))+"\r\n");
		}
		
	}
	/**
	 * 分页读取数据，大数据性能极差
	 * @param myPaging
	 * @throws IOException 
	 */
	public void putData2FileByList(MyPaging myPaging)  {
		try{
			this.printTitle();
			
			//从DB中分页读取
			int page=MyPaging.STARTPAGE;
			int rows =myPaging.getSpan();
			long sumOfPages = myPaging.getSumOfPages();
			int indexRow =1;//第0行放置标题
			
			//分页从DB中读取
			for(int p=page;p<=sumOfPages;p++){
				List data = myPaging.getDataByPaging4NestedList(p, rows);
				//System.out.println("####---"+data);
				// 将数据写入Excel
				if (data != null && data.size() > 0) {
					//判断数据结构
					boolean isArray = false;
					if(data.get(0) instanceof Object[]){
						isArray = true;
					}
					
					int size = data.size();
					
					for (int index = 0; index < size; index++) {
						
						//遍历每一行
						StringBuffer str = new StringBuffer();
						int length = 0;
						if(isArray){
							Object[] rowData = (Object[]) data.get(index);
							length = rowData.length;
							for(Object s :rowData){
								if(s!=null&&!"null".equalsIgnoreCase(s.toString())&&!StringUtils.isBlank(s.toString())){
									str.append(s.toString());
								}
								str.append(TXTWriter.SEPARATOR_COL);
							}
							
						}else{
							
							List<Object> rowData = (List<Object>) data.get(index);
							length = rowData.size();
							for(Object s :rowData){
								if(s!=null&&!"null".equalsIgnoreCase(s.toString())&&!StringUtils.isBlank(s.toString())){
									str.append(s.toString());
								}
								str.append(TXTWriter.SEPARATOR_COL);
							}
							
						}
						//如果数据列数小于最大行数，要自动补空
						if(myPaging.getMaxCol()>length){
							//计算补空数
							int supplementary4blank= myPaging.getMaxCol()-length;
							for(int i=0;i<supplementary4blank;i++){
								str.append(TXTWriter.SEPARATOR_COL);
							}
						}
						//去掉最后一个分隔符
						String tem = str.substring(0, str.lastIndexOf(TXTWriter.SEPARATOR_COL));
						this.writer.append(tem+"\r\n");
					}
	
				}
			}		
			//关闭流
			this.writer.flush();
			this.writer.close();
		}catch(Throwable t){
			//关闭流
			try {
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	/**
	 * 分页读取数据，大数据性能非常好
	 * @param myPaging
	 * @throws IOException 
	 */
	public void putData2FileByScroll(MyPaging myPaging)  {
		try{
			this.printTitle();
			
			//从DB中分页读取
			int page=MyPaging.STARTPAGE;
			int rows =myPaging.getSpan();
			long sumOfPages = myPaging.getSumOfPages();
			int indexRow =1;//第0行放置标题
			
			//分页从DB中读取
			for(int p=page;p<=sumOfPages;p++){
				ScrollableResults data = myPaging.getDataByPaging4Scroll(page, rows);
				// 将数据写入Excel
				while(data.next()){
					Object[] rowData = data.get();
					//遍历每一行
					StringBuffer str = new StringBuffer();
					int length = rowData.length;
					for(Object s :rowData){
						if(s!=null) {
							String tem = s.toString();
							if(!"null".equalsIgnoreCase(tem)&&!StringUtils.isBlank(tem)){
								str.append(tem);
							}
						}
						str.append(TXTWriter.SEPARATOR_COL);
					}
						
					
					//如果数据列数小于最大行数，要自动补空
					if(myPaging.getMaxCol()>length){
						//计算补空数
						int supplementary4blank= myPaging.getMaxCol()-length;
						for(int i=0;i<supplementary4blank;i++){
							str.append(TXTWriter.SEPARATOR_COL);
						}
					}
					//去掉最后一个分隔符
					String tem = str.substring(0, str.lastIndexOf(TXTWriter.SEPARATOR_COL));
					this.writer.append(tem+"\r\n");
				}

					
			}		
			//关闭流
			this.writer.flush();
			this.writer.close();
		}catch(Throwable t){
			//关闭流
			try {
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	public static void main(String[] args) {
		System.out.println(new Date().toString());
	}
}
