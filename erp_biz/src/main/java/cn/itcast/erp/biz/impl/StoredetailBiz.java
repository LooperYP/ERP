package cn.itcast.erp.biz.impl;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.itcast.erp.biz.IStoredetailBiz;
import cn.itcast.erp.dao.IGoodsDao;
import cn.itcast.erp.dao.IStoreDao;
import cn.itcast.erp.dao.IStoredetailDao;
import cn.itcast.erp.entity.Storealert;
import cn.itcast.erp.entity.Storedetail;
import cn.itcast.erp.exception.ErpException;
import cn.itcast.erp.util.MailUtil;
/**
 * 仓库库存业务逻辑类
 * @author Administrator
 *
 */
public class StoredetailBiz extends BaseBiz<Storedetail> implements IStoredetailBiz {

	private IStoredetailDao storedetailDao;
	private IStoreDao storeDao;
	private IGoodsDao goodsDao;
	private MailUtil mailUtil;
	private String to;//邮件接收者
	private String title;//邮件标题
	private String text;//邮件内容
	
	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
		super.setBaseDao(this.storedetailDao);
	}
	
	public List<Storedetail> getListByPage(Storedetail t1,Storedetail t2,Object param,int firstResult, int maxResults){
		List<Storedetail> list = super.getListByPage(t1,t2,param,firstResult, maxResults);
		Map<Long,String> goodsNameMap = new HashMap<Long, String>();
		Map<Long,String> storeNameMap = new HashMap<Long, String>();
		for(Storedetail sd : list){
			sd.setGoodsName(getName(sd.getGoodsuuid(),goodsNameMap,goodsDao));
			sd.setStoreName(getName(sd.getStoreuuid(),storeNameMap,storeDao));
		}
		return list;
	}
	
	/**
	 * 商品库存预警列表
	 * @return
	 */
	public List<Storealert> getStorealertList(){
		return storedetailDao.getStorealertList();
	}
	

	@Override
	public void sendStorealertMail() throws Exception {
		//预警列表
		List<Storealert> storealertList = storedetailDao.getStorealertList();
		if(storealertList.size() > 0){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			mailUtil.sendMail(to, title.replace("[time]", sdf.format(new Date())), 
					text.replace("[cout]", storealertList.size() + ""));
		}else{
			throw new ErpException("当前不存在需要告警的商品");
		}
	}
	

	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
	}

	public void setGoodsDao(IGoodsDao goodsDao) {
		this.goodsDao = goodsDao;
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

}
