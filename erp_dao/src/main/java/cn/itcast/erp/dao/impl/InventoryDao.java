package cn.itcast.erp.dao.impl;

import java.util.Calendar;
import java.util.Date;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import cn.itcast.erp.dao.IInventoryDao;
import cn.itcast.erp.entity.Inventory;

/**
 * 盘盈盘亏数据访问类
 * @author Administrator
 *
 */
public class InventoryDao extends BaseDao<Inventory> implements IInventoryDao {

	/**
	 * 构建查询条件
	 * @param dep1
	 * @param dep2
	 * @param param
	 * @return
	 */
	public DetachedCriteria getDetachedCriteria(Inventory inventory1, Inventory inventory2, Object param) {
		DetachedCriteria dc = DetachedCriteria.forClass(Inventory.class);
		dc.addOrder(Order.desc("createtime"));
		dc.addOrder(Order.desc("checktime"));
		if (inventory1 != null) {
			if (null != inventory1.getType() && inventory1.getType().trim().length() > 0) {
				dc.add(Restrictions.eq("type", inventory1.getType()));
			}
			if (null != inventory1.getState() && inventory1.getState().trim().length() > 0) {
				dc.add(Restrictions.eq("state", inventory1.getState()));
			}
			if (null != inventory1.getRemark() && inventory1.getRemark().trim().length() > 0) {
				dc.add(Restrictions.like("remark", inventory1.getRemark(), MatchMode.ANYWHERE));
			}
			// 登记日期
			if (null != inventory1.getCreatetime()) {
				dc.add(Restrictions.ge("createtime", inventory1.getCreatetime()));
			}
			// 审核日期
			if (null != inventory1.getChecktime()) {
				dc.add(Restrictions.ge("checktime", inventory1.getChecktime()));
			}

			if (null != inventory2) {
				// 登记日期
				if (null != inventory2.getCreatetime()) {
					Calendar car = getDate(inventory2.getCreatetime());
					dc.add(Restrictions.le("createtime", car.getTime()));
				}
				// 审核日期
				if (null != inventory2.getChecktime()) {
					Calendar car = getDate(inventory2.getChecktime());
					dc.add(Restrictions.le("checktime", car.getTime()));
				}
			}

		}
		return dc;
	}

	/**
	 * @param date
	 * @return
	 * 例：将2017-01-01 00:00:00转换成2017-01-01 23:59:59
	 */
	private Calendar getDate(Date date) {
		Calendar car = Calendar.getInstance();
		car.setTime(date);
		car.set(Calendar.HOUR, 23);// 23点
		car.set(Calendar.MINUTE, 59);// 59分
		car.set(Calendar.SECOND, 59);// 秒
		car.set(Calendar.MILLISECOND, 999);// 毫秒
		return car;
	}

}
