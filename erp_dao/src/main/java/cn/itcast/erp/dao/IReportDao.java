package cn.itcast.erp.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.itcast.erp.entity.InOutMoney;

/**
 * 报表Dao
 *
 */
public interface IReportDao {

	/**
	 * 销售统计
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	List<?> orderReport(Date startDate, Date endDate);
	
	/**
	 * 获取指定年份的销售额
	 * @param year
	 * @return
	 */
	List<Map<String,Object>> getSumMoney(int year);
	
	
	 /**
     * 退货统计
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    List<?> returnOrdersReport(Date startDate, Date endDate);
    
    /**
	 * 获取指定年份的退货额
	 * @param year
	 * @return
	 */
	List<Map<String,Object>> getReturnSumMoney(int year);

	
	/**
	 * 获取指定年份的收入支出额
	 * @param year
	 * @return
	 */
	public List<InOutMoney> getInOutMoney(int year);
}
