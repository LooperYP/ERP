package cn.itcast.erp.biz.impl;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

import cn.itcast.erp.biz.IReturnordersBiz;
import cn.itcast.erp.dao.IEmpDao;
import cn.itcast.erp.dao.IOrdersDao;
import cn.itcast.erp.dao.IReturnordersDao;
import cn.itcast.erp.dao.ISupplierDao;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.entity.Orders;
import cn.itcast.erp.entity.Returnorderdetail;
import cn.itcast.erp.entity.Returnorders;
import cn.itcast.erp.exception.ErpException;

/**
 * 退货订单业务逻辑类
 * @author Administrator
 *
 */
public class ReturnordersBiz extends BaseBiz<Returnorders> implements IReturnordersBiz {

private IReturnordersDao returnordersDao;
	
	private ISupplierDao supplierDao;
	private IEmpDao empDao;
	
	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}

	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}

	public void setReturnordersDao(IReturnordersDao returnordersDao) {
		this.returnordersDao = returnordersDao;
		super.setBaseDao(this.returnordersDao);
	}
	private IOrdersDao ordersDao;
	
	public void setOrdersDao(IOrdersDao ordersDao) {
		this.ordersDao = ordersDao;
	}

	public void add(Returnorders returnorders){
		//1. 生成日期
		returnorders.setCreatetime(new Date());
		//2. 订单状态
		returnorders.setState(Returnorders.STATE_CREATE);//未审核
		//3. 订单类型: 采购订单
		//orders.setType(Orders.TYPE_IN);
		//4. 计算合计金额
		double totalMoney = 0;
		//4.1 计算退货数量是否大于订单数量
		Orders orders = ordersDao.get(returnorders.getOrderuuid());
		for(Returnorderdetail od : returnorders.getReturnorderdetails()){
			//4.1 计算退货数量是否大于订单数量
			for (Orderdetail orderdetail : orders.getOrderdetails()) {
				//当两张子订单是同一goods时,计算他们的数量 
				if (orderdetail.getGoodsuuid() == od.getGoodsuuid()) {
					if (orderdetail.getNum() - od.getNum() < 0) {
						throw new ErpException("退货数量不能大于原订单数量");
					}
				}
			}
			//计算总金额
			totalMoney += od.getMoney();
			//设置明细与订单的关系
			od.setReturnorders(returnorders);
			//设置明细的状态
			od.setState(Returnorderdetail.STATE_NOT_IN);
		}
		//5. 合计金额
		returnorders.setTotalmoney(totalMoney);
		//6. 保存订单
		returnordersDao.add(returnorders);
	}
	
	public List<Returnorders> getListByPage(Returnorders t1,Returnorders t2,Object param,int firstResult, int maxResults){
		//获取order列表
		List<Returnorders> list = super.getListByPage(t1, t2, param, firstResult, maxResults);
		//缓存员工编号与员工名称
		Map<Long,String> empNameMap = new HashMap<Long, String>();
		//缓存供应商编号与供应商名称
		Map<Long,String> supplierNameMap = new HashMap<Long, String>();
		//设置名称
		for(Returnorders o : list){
			o.setCreaterName(getEmpName(o.getCreater(),empNameMap));
			o.setCheckerName(getEmpName(o.getChecker(),empNameMap));
			o.setEnderName(getEmpName(o.getEnder(),empNameMap));
			o.setSupplierName(getSupplierName(o.getSupplieruuid(),supplierNameMap));
		}
		return list;
	}

	@Override
	public void doCheck(long uuid, Long empuuid) {
		//获取订单进入持久化状态
		Returnorders returnorders = returnordersDao.get(uuid);
		//1. 判断订单状态是否为未审核
		if(!Returnorders.STATE_CREATE.equals(returnorders.getState())){
			throw new ErpException("该订单已经审核过了");
		}
		//2. 修改订单状态为 已审核
		returnorders.setState(Returnorders.STATE_CHECK);
		//3. 审核时间
		returnorders.setChecktime(new Date());
		//4. 审核人
		returnorders.setChecker(empuuid);
	}
	
	/**
	 * 获取员工名称
	 * @param uuid
	 * @param empNameMap 员工编号与名称的缓存
	 * @return
	 */
	private String getEmpName(Long uuid, Map<Long,String> empNameMap){
		if(null == uuid){
			return null;
		}
		String empName = empNameMap.get(uuid);
		if(null == empName){
			empName = empDao.get(uuid).getName();
			empNameMap.put(uuid, empName);
		}
		return empName;
	}


	
	/**
	 * 获取供应商名称
	 * @param uuid
	 * @param supplierNameMap 供应商编号与名称的缓存
	 * @return
	 */
	private String getSupplierName(Long uuid, Map<Long,String> supplierNameMap){
		if(null == uuid){
			return null;
		}
		String supplierName = supplierNameMap.get(uuid);
		if(null == supplierName){
			supplierName = supplierDao.get(uuid).getName();
			supplierNameMap.put(uuid, supplierName);
		}
		return supplierName;
	}
	
	@Override
	public void export(OutputStream os, long uuid) throws Exception {
		Returnorders returnorders = returnordersDao.get(uuid);
		//工作簿
		@SuppressWarnings("resource")
		HSSFWorkbook wk = new HSSFWorkbook();
		String title = "";
		if(Returnorders.TYPE_OUT.equals(returnorders.getType())){
			title = "销 售 退 货 单";
		}
		if(Returnorders.TYPE_IN.equals(returnorders.getType())){
			title = "采 购 退 货 单";
		}
		//创建工作表
		HSSFSheet sheet = wk.createSheet(title);
		//创建行 行的索引，从0开始
		HSSFRow row = null;
		//单元格的样式
		HSSFCellStyle style_content = wk.createCellStyle();
		style_content.setAlignment(CellStyle.ALIGN_CENTER);//水平居中
		style_content.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直居中
		HSSFCellStyle style_title = wk.createCellStyle();
		//复制样式
		style_title.cloneStyleFrom(style_content);
		
		//边框
		style_content.setBorderBottom(CellStyle.BORDER_THIN);
		style_content.setBorderLeft(CellStyle.BORDER_THIN);
		style_content.setBorderRight(CellStyle.BORDER_THIN);
		style_content.setBorderTop(CellStyle.BORDER_THIN);
		//内容的字体
		HSSFFont font_content = wk.createFont();
		font_content.setFontName("宋体");
		font_content.setFontHeightInPoints((short)12);
		style_content.setFont(font_content);
		//标题的字体 黑体
		HSSFFont font_title = wk.createFont();
		font_title.setFontName("黑体");
		font_title.setFontHeightInPoints((short)18);
		style_title.setFont(font_title);
		
		HSSFCell cell = null;
		sheet.createRow(0).createCell(0).setCellStyle(style_title);;//标题行
		//明细数量
		int size = returnorders.getReturnorderdetails().size();
		int rowCnt = 9 + size;
		//行高
		sheet.getRow(0).setHeight((short)1000);
		for(int i = 2; i <= rowCnt; i++){
			row = sheet.createRow(i);
			row.setHeight((short)500);//内容区域的行高
			for(int col = 0; col < 4; col++){
				cell = row.createCell(col);
				//设置单元格的样式
				cell.setCellStyle(style_content);
			}
		}
		//日期格式
		HSSFDataFormat df = wk.createDataFormat();
		HSSFCellStyle style_date = wk.createCellStyle();
		style_date.cloneStyleFrom(style_content);
		style_date.setDataFormat(df.getFormat("yyyy-MM-dd"));
		//设置日期格式
		for(int i = 3; i <= 6; i++){
			sheet.getRow(i).getCell(1).setCellStyle(style_date);
		}
		
		//合并单元格
		//1.开始的行索引
		//2.结束的行索引
		//3.列的开始索引
		//4.线束的列的索引
		//标题
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));
		//供应商
		sheet.addMergedRegion(new CellRangeAddress(2,2,1,3));
		//订单明细
		sheet.addMergedRegion(new CellRangeAddress(7,7,0,3));
		
		//设置内容
		sheet.getRow(0).getCell(0).setCellValue(title);
		sheet.getRow(2).getCell(0).setCellValue("供应商");
		sheet.getRow(2).getCell(1).setCellValue(supplierDao.getName(returnorders.getSupplieruuid()));
		
		sheet.getRow(3).getCell(0).setCellValue("下单日期");
		setDate(sheet.getRow(3).getCell(1),returnorders.getCreatetime());
		sheet.getRow(4).getCell(0).setCellValue("审核日期");
		setDate(sheet.getRow(4).getCell(1),returnorders.getChecktime());
		sheet.getRow(5).getCell(0).setCellValue("采购日期");
		//setDate(sheet.getRow(5).getCell(1),returnorders.getStarttime());
		sheet.getRow(6).getCell(0).setCellValue("入库日期");
		setDate(sheet.getRow(6).getCell(1),returnorders.getEndtime());
		sheet.getRow(3).getCell(2).setCellValue("经办人");
		sheet.getRow(3).getCell(3).setCellValue(empDao.getName(returnorders.getCreater()));
		sheet.getRow(4).getCell(2).setCellValue("经办人");
		sheet.getRow(4).getCell(3).setCellValue(empDao.getName(returnorders.getChecker()));
		sheet.getRow(5).getCell(2).setCellValue("经办人");
		//sheet.getRow(5).getCell(3).setCellValue(empDao.getName(returnorders.getStarter()));
		sheet.getRow(6).getCell(2).setCellValue("经办人");
		sheet.getRow(6).getCell(3).setCellValue(empDao.getName(returnorders.getEnder()));
		
		sheet.getRow(7).getCell(0).setCellValue("订单明细");
		sheet.getRow(8).getCell(0).setCellValue("商品");
		sheet.getRow(8).getCell(1).setCellValue("数量");
		sheet.getRow(8).getCell(2).setCellValue("价格");
		sheet.getRow(8).getCell(3).setCellValue("金额");
		
		//列宽
		for(int i = 0; i < 4; i++){
			sheet.setColumnWidth(i, 5000);
		}
				
		//明细内容
		int i = 9;
		for(Returnorderdetail od : returnorders.getReturnorderdetails()){
			row = sheet.getRow(i);
			row.getCell(0).setCellValue(od.getGoodsname());
			row.getCell(1).setCellValue(od.getPrice());
			row.getCell(2).setCellValue(od.getNum());
			row.getCell(3).setCellValue(od.getMoney());
			i++;
		}
		//合计
		sheet.getRow(rowCnt).getCell(0).setCellValue("合计");
		sheet.getRow(rowCnt).getCell(3).setCellValue(returnorders.getTotalmoney());
		
		wk.write(os);
	}
	
	private void setDate(Cell cel, Date date){
		if(null != date){
			cel.setCellValue(date);
		}
	}

}
