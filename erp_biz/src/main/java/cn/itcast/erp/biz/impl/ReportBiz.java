package cn.itcast.erp.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.itcast.erp.biz.IReportBiz;
import cn.itcast.erp.dao.IGoodstypeDao;
import cn.itcast.erp.dao.IReportDao;
import cn.itcast.erp.entity.InOutMoney;

public class ReportBiz implements IReportBiz {
	
	private IReportDao reportDao;

	@Override
	public List<?> orderReport(Date startDate, Date endDate) {
		return reportDao.orderReport(startDate, endDate);
	}

	public void setReportDao(IReportDao reportDao) {
		this.reportDao = reportDao;
	}
	
	private IGoodstypeDao goodstypeDao;
	
	public void setGoodstypeDao(IGoodstypeDao goodstypeDao) {
		this.goodstypeDao = goodstypeDao;
	}

	public List<Map<String, Object>> trendReport(int year) {
		List<Map<String, Object>> monthData = this.reportDao.getSumMoney(year);
		//key=月份, value= 数据
		//把数据库存在的月份的数据转成key value格式，存map里去,
		// mapMonthData的key就是月份
		Map<Integer, Map<String,Object>> mapMonthData = new HashMap<Integer, Map<String,Object>>();
		for(Map<String, Object> data : monthData){
			mapMonthData.put((Integer)data.get("name"), data);
		}
		
		//补充月份数据，
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> data = null;
		for(int i = 1; i<=12; i++){
			//如果判断
			//如果不存在月份数据
			data = mapMonthData.get(i);
			if(null == data){
				data = new HashMap<String,Object>();
				data.put("name", i);
				data.put("y", 0);
				result.add(data);
			}else{
				//存在月份数据
				result.add(data);
			}
		}
		
		return result;
	}
	
	 @Override
    public List<?> returnOrderReport(Date startDate, Date endDate) {
        return reportDao.returnOrdersReport(startDate,endDate);
    }
    
    @Override
	public List<Map<String, Object>> returnTrendReport(int year) {
    	//获取原list
		List<Map<String, Object>> list = reportDao.getReturnSumMoney(year);
		//构建一个new map用来判断那些月份有值
		Map<Integer, Map<String, Object>> trendMap = new HashMap<Integer, Map<String, Object>>();
		Map<String, Map<String, Object>> trendResultMap = new HashMap<String, Map<String, Object>>();
		for (Map<String, Object> map : list) {
			//月份作为key,原始数据作为value
			trendMap.put((Integer)map.get("name"), map);
			//月份 + 类型 作为key,原始数据作为value
			trendResultMap.put(map.get("name")+ "" + map.get("type"), map);
		}
		
		//得到所有商品类型
		List<Long> typeList = goodstypeDao.getGoodstypeUuid();
		
		//重新构建一个有12个月份销售额的list
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i <= 12; i++) {
			//通过月份key 判断是否有数据,如果无,则令该月份的值为0
			Map<String, Object> map = trendMap.get(i);
			if (trendMap.get(i) != null) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				//初始化类型数据 
				newMap.put("name", i);
				for (Long type : typeList) {
					newMap.put(goodstypeDao.getName(type), 0);
				}
				
				for (Long type : typeList) {
					//得到当月数据
					map = trendResultMap.get(i+""+type);
					if (trendResultMap.get(i+""+type) != null) {
						//得到类型数据
						newMap.put(goodstypeDao.getName(type), map.get("y"));
					}
				}
				result.add(newMap);
//				for (int j = 1; j <= 3; j++) {
//					//得到当月数据
//					map = trendResultMap.get(i+""+j);
//					if (trendResultMap.get(i+""+j) != null) {
//						//得到类型数据
//						//type : 1:水果 , 2: 调味品 , 3:饼干
//						Long type = (Long) map.get("type");
//						if (type == 1) {
//							newMap.put("shui", map.get("y"));
//						}
//						if (type == 2) {
//							newMap.put("tiao", map.get("y"));
//						}
//						if (type == 3) {
//							newMap.put("bing", map.get("y"));
//						}
//					}
//				}
//				result.add(newMap);
			}else {
				map = new HashMap<String, Object>();
				for (Long type : typeList) {
					map.put(goodstypeDao.getName(type), 0);
				}
				result.add(map);
			}
		}
		
		return result;
	}

	
	public static void main(String[] args) {
		List<Map<String, Object>> monthData = new ArrayList<Map<String,Object>>();
		Map<String, Object> month = new HashMap<String, Object>();
		month.put("name", 7);
		month.put("y", 9527);
		monthData.add(month);
		
		String data = "[{\"name\":1,\"y\":B0B},{\"name\":2,\"y\":B0B},{\"name\":3,\"y\":B0B},{\"name\":4,\"y\":B0B},{\"name\":5,\"y\":B0B},{\"name\":6,\"y\":B0B},{\"name\":7,\"y\":B0B},{\"name\":8,\"y\":B0B},{\"name\":9,\"y\":B0B},{\"name\":10,\"y\":B0B},{\"name\":11,\"y\":B0B},{\"name\":12,\"y\":B0B}]";
		String repBefore = "\\{\"name\":%d,\"y\":B0B\\}";
		String after = "{\"name\":%d,\"y\":%d}";
		for(Map<String, Object> d : monthData){
			//{name:7,y:B0B}=>{name:7,y:9527}
			data = data.replaceAll(String.format(repBefore, d.get("name")), String.format(after, d.get("name"),d.get("y")));
		}
		System.out.println(data.replaceAll("B", ""));
	}

	@Override
	public List<InOutMoney> getInOutMoney(int year) {
		return reportDao.getInOutMoney(year);
	}

}
