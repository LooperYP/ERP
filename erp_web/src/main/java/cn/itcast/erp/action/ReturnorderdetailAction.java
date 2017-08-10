package cn.itcast.erp.action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.itcast.erp.biz.IReturnorderdetailBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Returnorderdetail;
import cn.itcast.erp.exception.ErpException;

/**
 * 退货订单明细Action 
 * @author Administrator
 *
 */
public class ReturnorderdetailAction extends BaseAction<Returnorderdetail> {

	private static final Logger log = LoggerFactory.getLogger(OrderdetailAction.class);

	private IReturnorderdetailBiz returnorderdetailBiz;
	private Long storeuuid;//页面传过来的仓库编号

	public Long getStoreuuid() {
		return storeuuid;
	}

	public void setStoreuuid(Long storeuuid) {
		this.storeuuid = storeuuid;
	}

	public void setReturnorderdetailBiz(IReturnorderdetailBiz returnorderdetailBiz) {
		this.returnorderdetailBiz = returnorderdetailBiz;
		super.setBaseBiz(this.returnorderdetailBiz);
	}
	
	public void doInStore(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		try {
			returnorderdetailBiz.doInStore(getId(), storeuuid, loginUser.getUuid());
			ajaxReturn(true,"入库成功");
		} catch(ErpException e){
			ajaxReturn(false,e.getMessage());
			log.error("入库失败",e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("入库失败",e);
			ajaxReturn(false,"入库失败");
		}
	}

	public void doOutStore(){
		//当前登陆用户
		Emp loginUser = getLoginUser();
		log.info("loginUser:" + (null == loginUser? "":loginUser.getUuid()));
		if(null == loginUser){
			ajaxReturn(false,"您 还没有登陆");
			return;
		}
		try {
			returnorderdetailBiz.doOutStore(getId(), storeuuid, loginUser.getUuid());
			ajaxReturn(true,"出库成功");
		} catch(ErpException e){
			ajaxReturn(false,e.getMessage());
			log.error("出库失败",e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("出库失败",e);
			ajaxReturn(false,"出库失败");
		}
	}

}
