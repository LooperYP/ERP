package cn.itcast.erp.action;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IStoredetailBiz;
import cn.itcast.erp.entity.Storealert;
import cn.itcast.erp.entity.Storedetail;
import cn.itcast.erp.exception.ErpException;

/**
 * 仓库库存Action 
 * @author Administrator
 *
 */
public class StoredetailAction extends BaseAction<Storedetail> {
	
	private static final Logger log = LoggerFactory.getLogger(StoredetailAction.class);

	private IStoredetailBiz storedetailBiz;

	public void setStoredetailBiz(IStoredetailBiz storedetailBiz) {
		this.storedetailBiz = storedetailBiz;
		super.setBaseBiz(this.storedetailBiz);
	}
	
	/**
	 * 库存预警
	 */
	public void storealertList(){
		List<Storealert> list = storedetailBiz.getStorealertList();
		write(JSON.toJSONString(list));
	}
	
	/**
	 * 发送库存预警邮件
	 */
	public void sendStorealert(){
		try {
			storedetailBiz.sendStorealertMail();
			ajaxReturn(true,"发送预警邮件成功");
		} catch(ErpException e){
			ajaxReturn(false,e.getMessage());
		} catch (Exception e) {
			log.error("发送邮件失败",e);
			ajaxReturn(false,"发送预警邮件失败");
		}
	}

}
