package cn.itcast.demo.test;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redsun.bos.ws.Waybilldetail;
import com.redsun.bos.ws.impl.IWaybillWs;

public class ClientTest {

	@Test
	public void weatherTest(){
		/*ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext_cxf.xml");
		IWaybillWs client = (IWaybillWs)ac.getBean("waybillClient");
		List<Waybilldetail> waybilldetailList = client.waybilldetailList(6l);
		for (Waybilldetail waybilldetail : waybilldetailList) {
			System.out.println(waybilldetail);
		}*/
	}
}
