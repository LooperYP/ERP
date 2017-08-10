package cn.itcast.erp.biz.impl;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.redsun.bos.ws.impl.IWaybillWs;

import cn.itcast.erp.biz.IOrderdetailBiz;
import cn.itcast.erp.dao.IOrderdetailDao;
import cn.itcast.erp.dao.IStoredetailDao;
import cn.itcast.erp.dao.IStoreoperDao;
import cn.itcast.erp.dao.ISupplierDao;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.entity.Orders;
import cn.itcast.erp.entity.Storedetail;
import cn.itcast.erp.entity.Storeoper;
import cn.itcast.erp.entity.Supplier;
import cn.itcast.erp.exception.ErpException;
/**
 * 订单明细业务逻辑类
 * @author Administrator
 *
 */
public class OrderdetailBiz extends BaseBiz<Orderdetail> implements IOrderdetailBiz {

	private IOrderdetailDao orderdetailDao;
	private IStoredetailDao storedetailDao;
	private IStoreoperDao storeoperDao;
	private IWaybillWs waybillWs;
	private ISupplierDao supplierDao;
	
	public void setOrderdetailDao(IOrderdetailDao orderdetailDao) {
		this.orderdetailDao = orderdetailDao;
		super.setBaseDao(this.orderdetailDao);
	}

	@Override
	@RequiresPermissions("采购订单入库")
	public void doInStore(Long uuid, Long storeuuid, Long empuuid) {
		//1. 明细的操作 (orderdetail)
		Orderdetail orderdetail = orderdetailDao.get(uuid);
		//不能重复入库
		if(!Orderdetail.STATE_NOT_IN.equals(orderdetail.getState())){
			throw new ErpException("亲，该明细已经入库了");
		}
		//1.1 更新状态为已入库
		orderdetail.setState(Orderdetail.STATE_IN);
		//1.2 更新库管员
		orderdetail.setEnder(empuuid);
		//1.3 更新仓库编号
		orderdetail.setStoreuuid(storeuuid);
		//1.4 更新入库日期
		orderdetail.setEndtime(new Date());

		//2. 库存操作 (storedetail)
		//2.1 判断仓库中是否存在该商品的库存
		// 根据仓库编号与商品编号查询
		//   构建查询条件
		Storedetail storedetail = new Storedetail();
		storedetail.setStoreuuid(storeuuid);
		storedetail.setGoodsuuid(orderdetail.getGoodsuuid());
		List<Storedetail> storedetailList = storedetailDao.getList(storedetail, null, null);
		//2.2 如果 有库存，库存数量累加
		if(storedetailList.size() > 0){
			storedetail = storedetailList.get(0);
			//数量累计
			storedetail.setNum(storedetail.getNum() + orderdetail.getNum());
		}else{
			//设置数量
			storedetail.setNum(orderdetail.getNum());
		//2.3 如果 没有库存信息，加入一条新的记录
			storedetailDao.add(storedetail);
		}
		
		
		//3. 记录操作的日志(storeoper)
		Storeoper log = new Storeoper();
		log.setEmpuuid(empuuid);
		log.setStoreuuid(storeuuid);
		log.setNum(orderdetail.getNum());
		log.setOpertime(orderdetail.getEndtime());
		log.setGoodsuuid(orderdetail.getGoodsuuid());
		log.setType(Storeoper.TYPE_IN);
		//3.1 插入记录
		storeoperDao.add(log);

		//4. 订单操作(orders)
		Orders orders = orderdetail.getOrders();
		//4.1 判断是否所有的明细都已经入库
		//  构建查询条件
		Orderdetail queryParam = new Orderdetail();
		queryParam.setOrders(orders);
		queryParam.setState(Orderdetail.STATE_NOT_IN);
		//通过计算订单存在未入库的明细的明细
		long count = orderdetailDao.getCount(queryParam, null, null);
		//4.2 如果都入库，更新订单的状态，否则不做其它操作
		if(count == 0){
			//意味着：所有的明细都已经入库了
			orders.setState(Orders.STATE_END);
			//库管员
			orders.setEnder(empuuid);
			//入库时间
			orders.setEndtime(orderdetail.getEndtime());
		}
	}
	
	@Override
	@RequiresPermissions("销售订单出库")
	public void doOutStore(Long uuid, Long storeuuid, Long empuuid) {
		//1. 明细的操作 (orderdetail)
		Orderdetail orderdetail = orderdetailDao.get(uuid);
		//不能重复入库
		if(!Orderdetail.STATE_NOT_OUT.equals(orderdetail.getState())){
			throw new ErpException("亲，该明细已经出库了");
		}
		//1.1 更新状态为已出库
		orderdetail.setState(Orderdetail.STATE_OUT);
		//1.2 更新库管员
		orderdetail.setEnder(empuuid);
		//1.3 更新仓库编号
		orderdetail.setStoreuuid(storeuuid);
		//1.4 更新出库日期
		orderdetail.setEndtime(new Date());

		//2. 库存操作 (storedetail)
		//2.1 判断仓库中是否存在该商品的库存
		// 根据仓库编号与商品编号查询
		//   构建查询条件
		Storedetail storedetail = new Storedetail();
		storedetail.setStoreuuid(storeuuid);
		storedetail.setGoodsuuid(orderdetail.getGoodsuuid());
		List<Storedetail> storedetailList = storedetailDao.getList(storedetail, null, null);
		//2.2 如果 有库存，库存数量累加
		if(storedetailList.size() > 0){
			storedetail = storedetailList.get(0);
			//数量减少
			//取出库存数
			long storenum = storedetail.getNum();
			//要出库的数量
			long outnum = orderdetail.getNum();
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
		log.setNum(orderdetail.getNum());
		log.setOpertime(orderdetail.getEndtime());
		log.setGoodsuuid(orderdetail.getGoodsuuid());
		log.setType(Storeoper.TYPE_OUT);
		//3.1 插入记录
		storeoperDao.add(log);

		//4. 订单操作(orders)
		Orders orders = orderdetail.getOrders();
		//4.1 判断是否所有的明细都已经出库
		//  构建查询条件
		Orderdetail queryParam = new Orderdetail();
		queryParam.setOrders(orders);
		queryParam.setState(Orderdetail.STATE_NOT_OUT);
		//通过计算订单存在未出库的明细
		long count = orderdetailDao.getCount(queryParam, null, null);
		//4.2 如果都出库，更新订单的状态，否则不做其它操作
		if(count == 0){
			//意味着：所有的明细都已经出库了
			orders.setState(Orders.STATE_END);
			//库管员
			orders.setEnder(empuuid);
			//入库时间
			orders.setEndtime(orderdetail.getEndtime());
			
			//获取客户信息
			Supplier customer = supplierDao.get(orders.getSupplieruuid()); 
			//物流预约下单
			Long waybillsn = waybillWs.addWaybill(1l, customer.getAddress(), customer.getName(), customer.getTele(), "零食");
			//设置运单编号
			orders.setWaybillsn(waybillsn);
		}
	}

	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}

	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}

	public void setWaybillWs(IWaybillWs waybillWs) {
		this.waybillWs = waybillWs;
	}

	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}
	
}
