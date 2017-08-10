package cn.itcast.erp.biz;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.itcast.erp.entity.InOutMoney;

public interface IReportBiz {

	List<?> orderReport(Date startDate, Date endDate);
	
	List<Map<String,Object>> trendReport(int year);
	
	 /**
     *退货统计数据查询
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @return
     */
    List<?> returnOrderReport(Date startDate, Date endDate);
    
    /**
	 * 年度退货趋势
	 * @param year
	 * @return
	 */
	List<Map<String,Object>> returnTrendReport(int year);
	
	 /**
	 * 年度收入支出
	 * @param year
	 * @return
	 */
	public List<InOutMoney> getInOutMoney(int year);
}
