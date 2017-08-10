package cn.itcast.erp.biz.impl;
import java.util.Date;
import java.util.List;

import cn.itcast.erp.biz.IReturnorderdetailBiz;
import cn.itcast.erp.dao.IReturnorderdetailDao;
import cn.itcast.erp.dao.IStoredetailDao;
import cn.itcast.erp.dao.IStoreoperDao;
import cn.itcast.erp.entity.Returnorderdetail;
import cn.itcast.erp.entity.Returnorders;
import cn.itcast.erp.entity.Storedetail;
import cn.itcast.erp.entity.Storeoper;
import cn.itcast.erp.exception.ErpException;
/**
 * 退货订单明细业务逻辑类
 * @author Administrator
 *
 */
public class ReturnorderdetailBiz extends BaseBiz<Returnorderdetail> implements IReturnorderdetailBiz {

	private IReturnorderdetailDao returnorderdetailDao;
	private IStoredetailDao storedetailDao;
	private IStoreoperDao storeoperDao;
	
	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}

	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}

	public void setReturnorderdetailDao(IReturnorderdetailDao returnorderdetailDao) {
		this.returnorderdetailDao = returnorderdetailDao;
		super.setBaseDao(this.returnorderdetailDao);
	}
	
	@Override
	public void doInStore(long uuid, Long storeuuid, Long empuuid) {
		//1. 明细的操作 (orderdetail)
		Returnorderdetail returnorderdetail = returnorderdetailDao.get(uuid);
		//不能重复入库
		if(!Returnorderdetail.STATE_NOT_IN.equals(returnorderdetail.getState())){
			throw new ErpException("亲，该明细已经入库了");
		}
		//1.1 更新状态为已入库
		returnorderdetail.setState(Returnorderdetail.STATE_IN);
		//1.2 更新库管员
		returnorderdetail.setEnder(empuuid);
		//1.3 更新仓库编号
		returnorderdetail.setStoreuuid(storeuuid);
		//1.4 更新入库日期
		returnorderdetail.setEndtime(new Date());

		//2. 库存操作 (storedetail)
		//2.1 判断仓库中是否存在该商品的库存
		// 根据仓库编号与商品编号查询
		//   构建查询条件
		Storedetail storedetail = new Storedetail();
		storedetail.setStoreuuid(storeuuid);
		storedetail.setGoodsuuid(returnorderdetail.getGoodsuuid());
		List<Storedetail> storedetailList = storedetailDao.getList(storedetail, null, null);
		//2.2 如果 有库存，库存数量累加
		if(storedetailList.size() > 0){
			storedetail = storedetailList.get(0);
			//数量累计
			storedetail.setNum(storedetail.getNum() + returnorderdetail.getNum());
		}else{
			//设置数量
			storedetail.setNum(returnorderdetail.getNum());
		//2.3 如果 没有库存信息，加入一条新的记录
			storedetailDao.add(storedetail);
		}
		
		
		//3. 记录操作的日志(storeoper)
		Storeoper log = new Storeoper();
		log.setEmpuuid(empuuid);
		log.setStoreuuid(storeuuid);
		log.setNum(returnorderdetail.getNum());
		log.setOpertime(returnorderdetail.getEndtime());
		log.setGoodsuuid(returnorderdetail.getGoodsuuid());
		log.setType(Storeoper.TYPE_IN);
		System.out.println(log);
		//3.1 插入记录
		storeoperDao.add(log);

		//4. 订单操作(orders)
		Returnorders returnorders = returnorderdetail.getReturnorders();
		//4.1 判断是否所有的明细都已经入库
		//  构建查询条件
		List<Returnorderdetail> returnorderdetails = returnorders.getReturnorderdetails();
		int size=0;
		for (Returnorderdetail returnorderdetail2 : returnorderdetails) {
			if (returnorderdetail2.getState().equals(Returnorderdetail.STATE_NOT_IN)) {
				size++;
			}
		}
		/*Returnorderdetail queryParam = new Returnorderdetail();
		queryParam.setReturnorders(returnorders);
		queryParam.setState(Returnorderdetail.STATE_NOT_IN);
		//通过计算订单存在未入库的明细的明细
		long count = returnorderdetailDao.getCount(queryParam, null, null);*/
		//4.2 如果都入库，更新订单的状态，否则不做其它操作
		if(size == 0){
			//意味着：所有的明细都已经入库了
			returnorders.setState(Returnorders.STATE_IN);
			//库管员
			returnorders.setEnder(empuuid);
			//入库时间
			returnorders.setEndtime(returnorderdetail.getEndtime());
		}
	}
	
	
	
	@Override
	public void doOutStore(long uuid, Long storeuuid, Long empuuid) {
		//1. 明细的操作 (orderdetail)
		Returnorderdetail returnorderdetail = returnorderdetailDao.get(uuid);
		System.out.println(returnorderdetail);
		//不能重复入库
		if(!Returnorderdetail.STATE_NOT_OUT.equals(returnorderdetail.getState())){
			throw new ErpException("亲，该明细已经出库了");
		}
		//1.1 更新状态为已出库
		returnorderdetail.setState(Returnorderdetail.STATE_OUT);
		//1.2 更新库管员
		returnorderdetail.setEnder(empuuid);
		//1.3 更新仓库编号
		returnorderdetail.setStoreuuid(storeuuid);
		//1.4 更新出库日期
		returnorderdetail.setEndtime(new Date());

		//2. 库存操作 (storedetail)
		//2.1 判断仓库中是否存在该商品的库存
		// 根据仓库编号与商品编号查询
		//   构建查询条件
		Storedetail storedetail = new Storedetail();
		storedetail.setStoreuuid(storeuuid);
		storedetail.setGoodsuuid(returnorderdetail.getGoodsuuid());
		List<Storedetail> storedetailList = storedetailDao.getList(storedetail, null, null);
		//2.2 如果 有库存，库存数量累加
		if(storedetailList.size() > 0){
			storedetail = storedetailList.get(0);
			//数量减少
			//取出库存数
			long storenum = storedetail.getNum();
			//要出库的数量
			long outnum = returnorderdetail.getNum();
			//出完库剩下的数量
			long newStorenum = storenum - outnum;
			if(newStorenum < 0){
				throw new ErpException("库存不足");
			}
			storedetail.setNum(newStorenum);
		}else{
			//报库存不足
			throw new ErpException("库存不足");
		}
		
		//3. 记录操作的日志(storeoper)
		Storeoper log = new Storeoper();
		log.setEmpuuid(empuuid);
		log.setStoreuuid(storeuuid);
		log.setNum(returnorderdetail.getNum());
		log.setOpertime(returnorderdetail.getEndtime());
		log.setGoodsuuid(returnorderdetail.getGoodsuuid());
		log.setType(Storeoper.TYPE_OUT);
		//3.1 插入记录
		storeoperDao.add(log);

		//4. 订单操作(orders)
		Returnorders returnorders = returnorderdetail.getReturnorders();
		//4.1 判断是否所有的明细都已经出库
		
		List<Returnorderdetail> returnorderdetails = returnorders.getReturnorderdetails();
		long count =0;
		for (Returnorderdetail returnorderdetails2 : returnorderdetails) {
			if (returnorderdetails2.getState().equals("0")) {
				count++;
			}
		}
		if(count == 0){
			//意味着：所有的明细都已经出库了
			returnorders.setState(Returnorders.STATE_END);
			//库管员
			returnorders.setEnder(empuuid);
			//入库时间
			returnorders.setEndtime(returnorderdetail.getEndtime());
		}
	}
	
}
