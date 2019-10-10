package com.fulong.utils.v2.report.dealer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fulong.utils.v2.report.Param;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:applicationContext.xml"
		/* ,"file:src/main/webapp/WEB-INF/dispatcherServlet-servlet.xml" */ })
public class A {

	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Test
	public void test2() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		
		String[] arr = list.toArray(new String[0]);
		System.out.println(arr.length);
	}
	@Test
	public void testDealer() throws Exception {
		
		Param param = new Param();
		
		File f = this.createReport(param);
	
	}
	
	private File createReport(Param param) throws Exception {
		Session session = null;
		try {
			session = this.hibernateTemplate.getSessionFactory().openSession();

			ReportDealer dealer = new ReportDealer(session, param);

			File outf = dealer.setMilepost(false).start();
			session.close();
			return outf;
		} catch (Exception e) {
			if (session != null) {
				session.close();
			}
			throw e;
		}
		
	}
}
