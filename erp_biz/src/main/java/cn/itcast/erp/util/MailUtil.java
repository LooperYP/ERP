package cn.itcast.erp.util;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * 邮件util
 *
 */
public class MailUtil {
	
	private JavaMailSender sender;//邮件发送器
	
	private String from;//发件人

	public void sendMail(String to, String title, String text) throws Exception{
		//创建一封邮件
		MimeMessage mimeMessage = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
		//发件人
		helper.setFrom(from);
		helper.setTo(to);
		helper.setSubject(title);
		helper.setText(text,true);
		
		sender.send(mimeMessage);
	}

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}

	public void setFrom(String from) {
		this.from = from;
	}
}
