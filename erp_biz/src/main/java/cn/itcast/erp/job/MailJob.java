package cn.itcast.erp.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import cn.itcast.erp.biz.IStoredetailBiz;
import cn.itcast.erp.entity.Storealert;
import cn.itcast.erp.util.MailUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class MailJob {
	
	private static final Logger log = LoggerFactory.getLogger(MailJob.class);
	
	private IStoredetailBiz storedetailBiz;
	
	private Configuration freeMarker;
	
	private MailUtil mailUtil;
	
	private String to;
	
	private String title;
	
	private String text;

	public void doJob(){
		try {
			//预警列表
			log.info("进入定时邮件预警任务");
			List<Storealert> storealertList = storedetailBiz.getStorealertList();
			log.info("预警商品各类：" + storealertList.size());
			if(storealertList.size() > 0){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				//获取模板
				Template template = freeMarker.getTemplate("email.html");
				Map<String,Object> model = new HashMap<String,Object>();
				model.put("storealertList", storealertList);
				//把对象转化到模板里出,输出成字符串
				String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
				mailUtil.sendMail(to, title.replace("[time]", sdf.format(new Date())), 
						content);
				log.info("发送预警邮件成功! email:" + to);
			}
		} catch (Exception e) {
			log.error("定义预警邮件失败",e);
		}
	}

	public void setStoredetailBiz(IStoredetailBiz storedetailBiz) {
		this.storedetailBiz = storedetailBiz;
	}

	public void setMailUtil(MailUtil mailUtil) {
		this.mailUtil = mailUtil;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setFreeMarker(Configuration freeMarker) {
		this.freeMarker = freeMarker;
	}
}
