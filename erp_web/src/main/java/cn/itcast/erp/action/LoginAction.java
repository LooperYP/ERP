package cn.itcast.erp.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IEmpBiz;
import cn.itcast.erp.entity.Emp;

/**
 * 用户的登陆/出Action
 *
 */
public class LoginAction {
	
	private static final Logger log = LoggerFactory.getLogger(LoginAction.class);
	
	private IEmpBiz empBiz;
	
	private String username;//登陆名
	private String pwd;//密码

	public void checkUser2(){
		Emp loginUser = null;
		try {
			loginUser = empBiz.findByUsernameAndPwd(username, pwd);
			if(null == loginUser){
				ajaxReturn(false,"用户名或密码不正确");
			}else{
				//把登陆的用户放入值栈中
				ServletActionContext.getContext().getSession().put("loginUser", loginUser);
				ajaxReturn(true,"登陆成功");				
			}
		} catch (Exception e) {
			ajaxReturn(false,"登陆失败");
			log.error("登陆失败",e);
		}
	}
	
	/**
	 * 登陆
	 */
	public void checkUser(){
		//创建令牌
		UsernamePasswordToken upt = new UsernamePasswordToken(username,pwd);
		//获取主题
		Subject subject = SecurityUtils.getSubject();
		//执行主题的登陆方法
		try {
			subject.login(upt);
			ajaxReturn(true,"登陆成功");
		} catch (AuthenticationException e) {
			ajaxReturn(false,"登陆失败");
			log.error("登陆失败",e);
		}
	}
	
	//上次登录时间
	public void lastDate(){
		Emp loginUser = (Emp)SecurityUtils.getSubject().getPrincipal();
		//格式化日期
		Date date=new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd kk:mm:ss"); 
		String now=sdf.format(date);
		//获得所有cookie
		Cookie[] cookies = ServletActionContext.getRequest().getCookies();
		//第一次有带jesionid的cookie
		if(cookies.length==1){
			ajaxReturn(false, "您的第一次登录系统");
		}
		for (Cookie cookie:cookies) {
			//如果存在获得上次时间并销毁cookie
			if (cookie.getName().equals(loginUser.getUsername())) {
				
					ajaxReturn(true, "您的上次登录时间:"+cookie.getValue());
					cookie.setPath("/");
					cookie.setMaxAge(0);
			}
		}
		//销毁后重新设置cookie
		Cookie c2=new Cookie(loginUser.getUsername(), now);
		c2.setMaxAge(7*24*3600);
		c2.setPath("/");
		HttpServletResponse response = ServletActionContext.getResponse();
		response.addCookie(c2);
	}
	
	/**
	 * 显示登陆的用户名
	 */
	public void showName(){
		//获取登陆的用户
		//Emp loginUser = (Emp)ServletActionContext.getContext().getSession().get("loginUser");
		Emp loginUser = (Emp)SecurityUtils.getSubject().getPrincipal();
		if(null != loginUser){
			//回显用户名
			ajaxReturn(true,loginUser.getName());
		}else{
			ajaxReturn(false,"你还没有登陆");
		}
	}
	
	/**
	 * 退出登陆
	 */
	public void loginOut(){
		//ServletActionContext.getContext().getSession().remove("loginUser");
		SecurityUtils.getSubject().logout();
	}
	
	/**
	 * 返回前端操作结果
	 * @param success
	 * @param message
	 */
	public void ajaxReturn(boolean success, String message){
		//返回前端的JSON数据
		Map<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("success",success);
		rtn.put("message",message);
		write(JSON.toJSONString(rtn));
	}
	
	/**
	 * 输出字符串到前端
	 * @param jsonString
	 */
	public void write(String jsonString){
		try {
			//响应对象
			HttpServletResponse response = ServletActionContext.getResponse();
			//设置编码
			response.setContentType("text/html;charset=utf-8"); 
			//输出给页面
			response.getWriter().write(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setEmpBiz(IEmpBiz empBiz) {
		this.empBiz = empBiz;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
