package cn.itcast.erp.biz.impl;

import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.cache.CacheManager;

import cn.itcast.erp.biz.IInventoryBiz;
import cn.itcast.erp.dao.IInventoryDao;
import cn.itcast.erp.dao.impl.EmpDao;
import cn.itcast.erp.dao.impl.GoodsDao;
import cn.itcast.erp.dao.impl.StoreDao;
import cn.itcast.erp.dao.impl.StoredetailDao;
import cn.itcast.erp.dao.impl.StoreoperDao;
import cn.itcast.erp.entity.Inventory;
import cn.itcast.erp.entity.Storedetail;
import cn.itcast.erp.entity.Storeoper;
import cn.itcast.erp.exception.ErpException;

/**
 * 盘盈盘亏业务逻辑类
 * @author Administrator
 *
 */
public class InventoryBiz extends BaseBiz<Inventory> implements IInventoryBiz {

	private IInventoryDao inventoryDao;
	private CacheManager cacheManager;
	private EmpDao empDao;
	private GoodsDao goodsDao;
	private StoreDao storeDao;
	private StoreoperDao storeoperDao;// 操作操作记录表
	private StoredetailDao storedetailDao;// 仓库库存表

	public void setStoredetailDao(StoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}

	public void setStoreoperDao(StoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}

	public void setGoodsDao(GoodsDao goodsDao) {
		this.goodsDao = goodsDao;
	}

	public void setStoreDao(StoreDao storeDao) {
		this.storeDao = storeDao;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setEmpDao(EmpDao empDao) {
		this.empDao = empDao;
	}

	public void setInventoryDao(IInventoryDao inventoryDao) {
		this.inventoryDao = inventoryDao;
		super.setBaseDao(this.inventoryDao);
	}

	/*
	 * (non-Javadoc) 添加盘点记录
	 */
	@RequiresPermissions("盘盈盘亏登记")
	public void add(Inventory inventory) {

		Storedetail storedetail = new Storedetail();
		storedetail.setGoodsuuid(inventory.getGoodsuuid());
		storedetail.setStoreuuid(inventory.getStoreuuid());
		List<Storedetail> storeList = storedetailDao.getList(storedetail, null, null);
		if (storeList.size() > 0) {
			storedetail = storeList.get(0);
			if (storedetail.getNum() >= inventory.getNum()) {
				inventory.setType(Inventory.TYPE_LOSS);
			}
			if (storedetail.getNum() <= inventory.getNum()) {
				inventory.setType(Inventory.TYPE_PROFIT);
			}
			// 设置创建时间
			inventory.setCreatetime(new Date());
			// 设置盘点记录状态为未审核
			inventory.setState(Inventory.STATE_CREATE);

			inventoryDao.add(inventory);
		} else {
			throw new ErpException("盘点失败，该仓库中不存在该产品，请检查!");
		}

	}

	/*
	 * 盘点查询
	 */
	public List<Inventory> getListByPage(Inventory t1, Inventory t2, Object param, int firstResult, int maxResults) {
		// 获取order列表
		List<Inventory> list = super.getListByPage(t1, t2, param, firstResult, maxResults);
		// 设置名称
		for (Inventory o : list) {
			o.setCreaterName(getEmpName(o.getCreater()));
			o.setCheckerName(getEmpName(o.getChecker()));
			o.setGoodsName(getGoodsName(o.getGoodsuuid()));
			o.setStoreName(getStoreName(o.getStoreuuid()));

		}
		return list;
	}

	/**
	 * 获取员工名称
	 * @param uuid
	 * @param empNameMap 员工编号与名称的缓存
	 * @return
	 */
	private String getEmpName(Long uuid) {
		if (null == uuid) {
			return null;
		}
		String empName = cacheManager.getCache("myCache").get("emp_" + uuid, String.class);
		if (null == empName) {
			empName = empDao.get(uuid).getName();
			cacheManager.getCache("myCache").put("emp_" + uuid, empName);
		}
		return empName;
	}

	/**
	 * @param uuid
	 * @return
	 * 
	 * 缓存商品名称
	 */
	private String getGoodsName(Long uuid) {
		if (null == uuid) {
			return null;
		}
		String goodsName = cacheManager.getCache("myCache").get("goods_" + uuid, String.class);
		if (null == goodsName) {
			goodsName = goodsDao.get(uuid).getName();
			cacheManager.getCache("myCache").put("goods_" + uuid, goodsName);
		}
		return goodsName;
	}

	/**
	 * @param uuid
	 * @return
	 * 
	 * 缓存仓库名称
	 */
	private String getStoreName(Long uuid) {
		if (null == uuid) {
			return null;
		}
		String storeName = cacheManager.getCache("myCache").get("store_" + uuid, String.class);
		if (null == storeName) {
			storeName = storeDao.get(uuid).getName();
			cacheManager.getCache("myCache").put("store_" + uuid, storeName);
		}
		return storeName;
	}

	@Override
	@RequiresPermissions("盘盈盘亏审核")
	public void doCheck(Long uuid, Long empuuid) {

		// 第一步：对Inventory表进行操作，更新审核时间，审核状态，审核人。
		// 获取记录进入持久化状态
		Inventory inventory = inventoryDao.get(uuid);
		if (!Inventory.STATE_CREATE.equals(inventory.getState())) {
			throw new ErpException("该货物已经盘点审核过了。");
		}
		// 2.修改盘点记录状态未已审核
		inventory.setState(Inventory.STATE_CHECK);
		// 3.更新审核时间
		inventory.setChecktime(new Date());
		// 4.更新审核人
		inventory.setChecker(empuuid);

		// 第二步，对库存表进行操作
		Storedetail storedetail = new Storedetail();
		storedetail.setGoodsuuid(inventory.getGoodsuuid());
		storedetail.setStoreuuid(inventory.getStoreuuid());
		List<Storedetail> storedetailsList = storedetailDao.getList(storedetail, null, null);
		Long storeNum = 0l;// 记录历史库存量
		if (storedetailsList.size() > 0) {
			storedetail = storedetailsList.get(0);
			storeNum = storedetail.getNum();
			
			//更新库存数量
			storedetail.setNum(inventory.getNum());
		} else {
			throw new ErpException("盘点信息异常，审核失败");
		}

		// 第三步，对库存变动表进行操作
		// 3.1. 记录操作的日志(storeoper)
		Storeoper log = new Storeoper();
		log.setEmpuuid(empuuid);
		log.setStoreuuid(inventory.getStoreuuid());
		log.setNum(Math.abs(storeNum - inventory.getNum()));
		log.setOpertime(inventory.getChecktime());
		log.setGoodsuuid(inventory.getGoodsuuid());
		if ("0".equals(inventory.getType())) {
			log.setType(Storeoper.TYPE_PROFIT);// 设置类型为盘盈
		} else if ("1".equals(inventory.getType())) {
			log.setType(Storeoper.TYPE_LOSS);// 设置库存变更类型为盘亏
		}
		// 3.2 插入记录
		storeoperDao.add(log);
	}

}
