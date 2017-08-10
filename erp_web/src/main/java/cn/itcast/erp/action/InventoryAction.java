package cn.itcast.erp.action;

import org.apache.shiro.authz.UnauthorizedException;

import cn.itcast.erp.biz.IInventoryBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Inventory;
import cn.itcast.erp.exception.ErpException;

/**
 * 盘盈盘亏Action 
 * @author Administrator
 *
 */
public class InventoryAction extends BaseAction<Inventory> {

	private IInventoryBiz inventoryBiz;

	public void setInventoryBiz(IInventoryBiz inventoryBiz) {
		this.inventoryBiz = inventoryBiz;
		super.setBaseBiz(this.inventoryBiz);
	}

	/*
	 * 添加盘点记录
	 */
	public void add() {
		Emp loginUser = super.getLoginUser();
		if (null == loginUser) {
			ajaxReturn(false, "亲，请先登录系统");
			return;
		}
		try {
			Inventory inventory = getT();
			inventory.setCreater(loginUser.getUuid());
			inventoryBiz.add(inventory);
			ajaxReturn(true, "亲，添加成功");
		} catch (ErpException e) {
			e.printStackTrace();
			ajaxReturn(false, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			ajaxReturn(false, "亲，添加失败");
		}

	}

	/**
	 * 盘点审核
	 */
	public void doCheck() {
		// 当前登陆用户
		Emp loginUser = super.getLoginUser();
		if (null == loginUser) {
			ajaxReturn(false, "亲,请先登录系统。");
			return;
		}
		// 获取页面传过来的订单编号
		Long uuid = getId();
		try {
			inventoryBiz.doCheck(uuid, loginUser.getUuid());
			ajaxReturn(true, "订单审核成功");
		} catch (ErpException e) {
			ajaxReturn(false, e.getMessage());
			e.printStackTrace();
		} catch (UnauthorizedException e) {
			ajaxReturn(false, "没有权限");
			e.printStackTrace();
		} catch (Exception e) {
			ajaxReturn(false, "订单审核失败");
			e.printStackTrace();
		}
	}

	/**
	 * 查询我的订单
	 */
	public void query() {
		// 获取页面传过来的查询条件，如果有查询条件，t1不为空
		if (null == getT1()) {
			// 构建查询条件
			setT1(new Inventory());
		}
		// 当前登陆用户
		Emp loginUser = getLoginUser();
		if (null != loginUser) {
			// 查询条件
			Inventory t1 = getT1();
			// 设置订单创建者
			t1.setCreater(loginUser.getUuid());
			super.listByPage();
		}
	}

}
