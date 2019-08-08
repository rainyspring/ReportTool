package com.fulong.utils.v1.paging.render;
/**
 * 由于虽然BeanRender 能通过反射自动获取每个getXX 的值，
 * 但是无法控制值的顺序，故要导出的Bean实现此接口
 * @author jx
 *
 */
public interface BeanRenderMethod {
	
	/**
	 * 方法名逗号分隔
	 * @return
	 */
	String getMethod4ExportProperties();
}
