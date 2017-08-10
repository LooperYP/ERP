package cn.itcast.erp.action;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IReturnordersBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Returnorderdetail;
import cn.itcast.erp.entity.Returnorders;
import cn.itcast.erp.exception.ErpException;

/**
 * 退货订单Action 
 * @author Administrator
 *
 */
public class ReturnordersAction extends BaseAction<Returnorders> {

	private static final Logger log = LoggerFactory.getLogger(ReturnordersAction.class);

	private IReturnordersBiz returnordersBiz;
	
	private String json;// 订单明细的json字符串，数据形式

	public void setReturnordersBiz(IReturnordersBiz returnordersBiz) {
		this.returnordersBiz = returnordersBiz;
		super.setBaseBiz(this.returnordersBiz);
	}
	
	public void add() {

		// 当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser ? "" : loginUser.getUuid()));
		if (null == loginUser) {
			ajaxReturn(false, "您 还没有登陆");
			return;
		}

		// 获取提交过来的订单，里面有供应商的编号
		try {
			Returnorders returnorders = getT();
			log.info("supplieruuid:" + (returnorders.getSupplieruuid() == null ? "-1" : returnorders.getSupplieruuid()));
			log.debug("returnorderdetail:" + json);
			returnorders.setCreater(loginUser.getUuid());
			// 订单明细
			List<Returnorderdetail> returnorderdetail = JSON.parseArray(json, Returnorderdetail.class);
			returnorders.setReturnorderdetails(returnorderdetail);
			returnordersBiz.add(returnorders);
			ajaxReturn(true, "新增订单成功");
		} catch (ErpException e) {
			ajaxReturn(false, e.getMessage());
			log.error("新增订单失败", e);
		} catch (Exception e) {
			ajaxReturn(false, "新增订单失败");
			log.error("新增订单失败", e);
		}
	}

	public void doCheck(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		//获取页面传过来的订单编号
		Long uuid = getId();
		log.info("审核：" + (uuid==null?"":uuid));
		try {
			returnordersBiz.doCheck(uuid, loginUser.getUuid());
			ajaxReturn(true,"订单审核成功");
		} catch(ErpException e){
			log.error("订单审核失败",e);
			ajaxReturn(false,e.getMessage());
		} catch (Exception e) {
			log.error("订单审核失败",e);
			ajaxReturn(false,"订单审核失败");
		}
	}
	
	public void setJson(String json) {
		this.json = json;
	}
	
	public void myOrderList(){
		//获取页面传过来的查询条件，如果有查询条件，t1不为空
		if(null == getT1()){
			//构建查询条件
			setT1(new Returnorders());
		}
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null != loginUser){
			//查询条件
			Returnorders t1 = getT1();
			//设置订单创建者
			t1.setCreater(loginUser.getUuid());
			super.listByPage();
		}
		
	}
	
	public void export(){
		try {
			String filename = String.format("attachment;filename=returnorders_%d.xls", getId());
			HttpServletResponse res = ServletActionContext.getResponse();
			//告诉客户端，传输是一个文件
			res.setHeader("Content-Disposition", filename);
			returnordersBiz.export(res.getOutputStream(), getId());
		} catch (Exception e) {
			log.error("导出数据失败",e);
		}
	}
}
