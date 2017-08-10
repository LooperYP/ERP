package cn.itcast.erp.dao.impl;
import java.util.Calendar;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import cn.itcast.erp.dao.IStoreoperDao;
import cn.itcast.erp.entity.Storeoper;
/**
 * 仓库操作记录数据访问类
 * @author Administrator
 *
 */
public class StoreoperDao extends BaseDao<Storeoper> implements IStoreoperDao {

	/**
	 * 构建查询条件
	 * @param dep1
	 * @param dep2
	 * @param param
	 * @return
	 */
	public DetachedCriteria getDetachedCriteria(Storeoper storeoper1,Storeoper storeoper2,Object param){
		DetachedCriteria dc=DetachedCriteria.forClass(Storeoper.class);
		dc.addOrder(Order.desc("opertime"));
		if(storeoper1!=null){
			if(null != storeoper1.getType() && storeoper1.getType().trim().length()>0){
				dc.add(Restrictions.eq("type", storeoper1.getType()));
			}
			//仓库查询
			if(null != storeoper1.getStoreuuid()){
				dc.add(Restrictions.eq("storeuuid", storeoper1.getStoreuuid()));
			}
			//操作员编号
			if(null != storeoper1.getEmpuuid()){
				dc.add(Restrictions.eq("empuuid", storeoper1.getEmpuuid()));
			}
			//商品编号
			if(null != storeoper1.getGoodsuuid()){
				dc.add(Restrictions.eq("goodsuuid", storeoper1.getGoodsuuid()));
			}
			//操作开始日期
			if(null != storeoper1.getOpertime()){
				dc.add(Restrictions.ge("opertime",storeoper1.getOpertime()));
			}
		}
		if(null != storeoper2){
			//操作结束日期
			if(null != storeoper2.getOpertime()){
				Calendar car = Calendar.getInstance();
				car.setTime(storeoper2.getOpertime());
				car.set(Calendar.HOUR, 23);//23点
				car.set(Calendar.MINUTE, 59);//59分
				car.set(Calendar.SECOND, 59);//秒
				car.set(Calendar.MILLISECOND, 999);//毫秒
				dc.add(Restrictions.le("opertime", car.getTime()));
			}
		}
		return dc;
	}

}
