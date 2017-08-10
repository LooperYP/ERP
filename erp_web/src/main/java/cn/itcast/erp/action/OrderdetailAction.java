package cn.itcast.erp.action;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.itcast.erp.biz.IOrderdetailBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.exception.ErpException;

/**
 * 订单明细Action 
 * @author Administrator
 *
 */
public class OrderdetailAction extends BaseAction<Orderdetail> {
	
	private static final Logger log = LoggerFactory.getLogger(OrderdetailAction.class);

	private IOrderdetailBiz orderdetailBiz;
	private Long storeuuid;//页面传过来的仓库编号

	public void setOrderdetailBiz(IOrderdetailBiz orderdetailBiz) {
		this.orderdetailBiz = orderdetailBiz;
		super.setBaseBiz(this.orderdetailBiz);
	}
	
	/**
	 * 入库
	 */
	public void doInStore(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		try {
			orderdetailBiz.doInStore(getId(), storeuuid, loginUser.getUuid());
			ajaxReturn(true,"入库成功");
		} catch(ErpException e){
			ajaxReturn(false,e.getMessage());
			log.error("入库失败",e);
		} catch(UnauthorizedException e){
			ajaxReturn(false,"没有权限");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("入库失败",e);
			ajaxReturn(false,"入库失败");
		}
	}
	
	/**
	 * 入库
	 */
	public void doOutStore(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		try {
			orderdetailBiz.doOutStore(getId(), storeuuid, loginUser.getUuid());
			ajaxReturn(true,"出库成功");
		} catch(ErpException e){
			ajaxReturn(false,e.getMessage());
			log.error("出库失败",e);
		} catch(UnauthorizedException e){
			ajaxReturn(false,"没有权限");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("出库失败",e);
			ajaxReturn(false,"出库失败");
		}
	}

	public void setStoreuuid(Long storeuuid) {
		this.storeuuid = storeuuid;
	}

}
