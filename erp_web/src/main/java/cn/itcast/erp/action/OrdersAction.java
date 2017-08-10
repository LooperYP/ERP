package cn.itcast.erp.action;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.redsun.bos.ws.Waybilldetail;
import com.redsun.bos.ws.impl.IWaybillWs;

import cn.itcast.erp.biz.IOrdersBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.entity.Orders;
import cn.itcast.erp.exception.ErpException;

/**
 * 订单Action 
 * @author Administrator
 *
 */
public class OrdersAction extends BaseAction<Orders> {
	
	private static final Logger log = LoggerFactory.getLogger(OrdersAction.class);

	private IOrdersBiz ordersBiz;
	
	private IWaybillWs waybillWs;

	public void setOrdersBiz(IOrdersBiz ordersBiz) {
		this.ordersBiz = ordersBiz;
		super.setBaseBiz(this.ordersBiz);
	}
	
	private String json;//订单明细的json字符串，数据形式
	
	private Long waybillsn;//运单号
	
	public void add(){
		
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		//获取提交过来的订单，里面有供应商的编号
		try {
			Orders orders = getT();
			log.info("supplieruuid:" + (orders.getSupplieruuid() == null ? "-1":orders.getSupplieruuid()));
			log.debug("orderdetails:" + json);
			orders.setCreater(loginUser.getUuid());
			//订单明细
			List<Orderdetail> orderdetails = JSON.parseArray(json, Orderdetail.class);
			orders.setOrderdetails(orderdetails);
			ordersBiz.add(orders);
			ajaxReturn(true,"新增订单成功");
		} catch(ErpException e){
			log.error("新增订单失败",e);
			ajaxReturn(false,e.getMessage());
		}catch (Exception e) {
			ajaxReturn(false,"新增订单失败");
			log.error("新增订单失败",e);
		}
	}
	
	/**
	 * 订单审核
	 */
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
			ordersBiz.doCheck(uuid, loginUser.getUuid());
			ajaxReturn(true,"订单审核成功");
		} catch(ErpException e){
			log.error("订单审核失败",e);
			ajaxReturn(false,e.getMessage());
		} catch(UnauthorizedException e){
			ajaxReturn(false,"没有权限");
		} catch (Exception e) {
			log.error("订单审核失败",e);
			ajaxReturn(false,"订单审核失败");
		}
	}
	
	/**
	 * 订单确认
	 */
	public void doStart(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		//获取页面传过来的订单编号
		Long uuid = getId();
		log.info("确认：" + (uuid==null?"":uuid));
		try {
			ordersBiz.doStart(uuid, loginUser.getUuid());
			ajaxReturn(true,"订单确认成功");
		} catch(ErpException e){
			log.error("订单确认失败",e);
			ajaxReturn(false,e.getMessage());
		} catch(UnauthorizedException e){
			ajaxReturn(false,"没有权限");
		} catch (Exception e) {
			log.error("订单确认失败",e);
			ajaxReturn(false,"订单确认失败");
		}
	}
	
	public void myOrderList(){
		//获取页面传过来的查询条件，如果有查询条件，t1不为空
		if(null == getT1()){
			//构建查询条件
			setT1(new Orders());
		}
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null != loginUser){
			//查询条件
			Orders t1 = getT1();
			//设置订单创建者
			t1.setCreater(loginUser.getUuid());
			super.listByPage();
		}
	}
	
	/**
	 * 导出数据
	 */
	public void export(){
		try {
			String filename = String.format("attachment;filename=orders_%d.xls", getId());
			HttpServletResponse res = ServletActionContext.getResponse();
			//告诉客户端，传输是一个文件
			res.setHeader("Content-Disposition", filename);
			ordersBiz.export(res.getOutputStream(), getId());
		} catch (Exception e) {
			log.error("导出数据失败",e);
		}
	}
	
	/**
	 * 获取物流详情
	 */
	public void waybilldetailList(){
		List<Waybilldetail> waybilldetailList = waybillWs.waybilldetailList(waybillsn);
		write(JSON.toJSONString(waybilldetailList));
	}

	public void setJson(String json) {
		this.json = json;
	}

	public void setWaybillsn(Long waybillsn) {
		this.waybillsn = waybillsn;
	}

	public void setWaybillWs(IWaybillWs waybillWs) {
		this.waybillWs = waybillWs;
	}

}
