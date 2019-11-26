package com.fulong.utils.v2.report.dealer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

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
@ContextConfiguration(locations = { "classpath:config/applicationContext.xml"
		,"classpath:config/app-hibernate.xml"
		/* ,"file:src/main/webapp/WEB-INF/dispatcherServlet-servlet.xml" */ })
public class A {

	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Test
	public void testDealer() throws Exception {
		
		Param p = new Param();
		
		
//		p.templet = new File("F:\\a\\handoverTemplet\\sh\\SH3503-J113表-合格焊工登记表.xlsx");
		p.templet = new File("F:\\a\\handoverTemplet\\sh\\SH3543-G401表-管道焊接接头无损检测日委托单.xlsx");
		
		File f = this.createReport(p);
		
		Files.move(f.toPath(), Paths.get("F:/a.xlsx"));
		System.out.println("success");
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
