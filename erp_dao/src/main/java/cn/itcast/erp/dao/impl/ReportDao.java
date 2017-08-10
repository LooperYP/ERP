package cn.itcast.erp.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import cn.itcast.erp.dao.IReportDao;
import cn.itcast.erp.entity.InOutMoney;

public class ReportDao extends HibernateDaoSupport implements IReportDao {

	@Override
	public List<?> orderReport(Date startDate, Date endDate) {
		String hql = "select new Map(gt.name as name,sum(od.money) as y) "
				   + "from Goodstype gt, Orderdetail od, Orders o, Goods g "
				   + "where g.goodstype=gt and g.uuid=od.goodsuuid "
				   + "and od.orders = o and o.type='2' ";
		List<Date> queryParam = new ArrayList<Date>();
		if(null != startDate){
			queryParam.add(startDate);
			hql += "and o.createtime>=? ";
		}
		if(null != endDate){
			queryParam.add(endDate);
			hql += "and o.createtime<=? ";
		}
		hql+= "group by gt.name";
		return this.getHibernateTemplate().find(hql,queryParam.toArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String,Object>> getSumMoney(int year) {
		String hql = "select new Map(month(o.createtime) as name,sum(od.money) as y) "
				+ "from Orderdetail od,Orders o "
				+ "where od.orders =o "
				+ "and o.type='2' and year(o.createtime)=? "
				+ "group by month(o.createtime)";
		return (List<Map<String,Object>>)this.getHibernateTemplate().find(hql, year);
	}
	
	/**
	 * 查询退货订单
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return
	 */
	@Override
	public List<?> returnOrdersReport(Date startDate, Date endDate) {
		//type=2 为销售退货
		String hql = "select new Map(gt.name as name,sum(rod.money) as y) "
				+ "from Goodstype gt, Returnorderdetail rod, Returnorders ro, Goods g "
				+ "where g.goodstype=gt and g.uuid=rod.goodsuuid "
				+ "and rod.returnorders = ro and ro.type='2' ";
		List<Date> queryParam = new ArrayList<Date>();
		if(null != startDate){
			queryParam.add(startDate);
			hql += "and ro.createtime>=? ";
		}
		if(null != endDate){
			queryParam.add(endDate);
			hql += "and ro.createtime<=? ";
		}
		hql+= "group by gt.name";
		return this.getHibernateTemplate().find(hql,queryParam.toArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getReturnSumMoney(int year) {
		String hql ="select new Map(month(o.createtime) as name, "
				+ "gt.uuid as type,sum(od.money) as y) "
				+ "from Returnorders o, Returnorderdetail od, Goods g, Goodstype gt "
				+ "where od.returnorders = o and od.goodsuuid = g "
				+ "and g.goodstype = gt "
				+ "and o.type = '2' and year(o.createtime) = ? "
				+ "group by month(o.createtime),gt.uuid ";
		return (List<Map<String, Object>>) getHibernateTemplate().find(hql,year);
	}
	
	@SuppressWarnings("unchecked")
	public List<InOutMoney> getInOutMoney(int year) {
		String hql = "from InOutMoney where year = ? order by month";
		return (List<InOutMoney>)this.getHibernateTemplate().find(hql,year);
	}
	
}
