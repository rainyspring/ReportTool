package xu.jiang.report.paging.render;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据集合是List<Bean>行的记录形式 这里的Bean必须有get方法获取属性值
 * 
 * @author jx
 *
 */
public  class BeanRender<E extends BeanRenderMethod> extends AbstractSimallDataRender<E> {

	public BeanRender(List<E> allData, int maxCol) {
		super(allData, maxCol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public final void transferPerObject(List<Object> r, E instance) throws Exception {

		Class<?> object = instance.getClass();

		Method myM = object.getMethod("getMethod4ExportProperties");
		if (myM == null) {
			throw new Exception("您的实体没有实现BeanRenderMethod接口，无法使用BeanRender数据获取器");

		}
		// 获取值的方法名
		String[] methodNames = myM.invoke(instance).toString().split(",");

		for (String name : methodNames) {
			if(!StringUtils.isBlank(name)){
				Method method = object.getMethod(name);
				Object value = method.invoke(instance);
				r.add(value);
			}

		}

		// 无序的，
		/*
		 * Method[] methods = object.getMethods(); List<String> vList = new
		 * ArrayList<String>(); for(Method m:methods){
		 * if(m.getName().startsWith("get")&&!"getClass".equals(m.getName())){
		 * Method method=object.getMethod(m.getName());
		 * 
		 * Object value = method.invoke(instance); r.add(value); } }
		 */
	}

}
