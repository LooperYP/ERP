package cn.itcast.erp.biz.impl;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;

import cn.itcast.erp.biz.IOrdersBiz;
import cn.itcast.erp.dao.IEmpDao;
import cn.itcast.erp.dao.IOrdersDao;
import cn.itcast.erp.dao.ISupplierDao;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.entity.Orders;
import cn.itcast.erp.exception.ErpException;
import net.sf.jxls.transformer.XLSTransformer;
/**
 * 订单业务逻辑类
 * @author Administrator
 *
 */
public class OrdersBiz extends BaseBiz<Orders> implements IOrdersBiz {

	private IOrdersDao ordersDao;
	private IEmpDao empDao;
	private ISupplierDao supplierDao;
	private CacheManager cacheManager;
	
	public void setOrdersDao(IOrdersDao ordersDao) {
		this.ordersDao = ordersDao;
		super.setBaseDao(this.ordersDao);
	}
	
	public void add(Orders orders){
		//订单类型判断，排除非法参数，用户不正规访问
		if(!Orders.TYPE_IN.equals(orders.getType()) && !Orders.TYPE_OUT.equals(orders.getType())){
			throw new ErpException("非法参数");
		}
		//当前登陆用记的主题
		Subject subject = SecurityUtils.getSubject();
		if(Orders.TYPE_IN.equals(orders.getType())){
			//采购订单
			if(!subject.isPermitted("采购订单申请")){
				throw new ErpException("没有权限");
			}
		}
		if(Orders.TYPE_OUT.equals(orders.getType())){
			//销售订单
			if(!subject.isPermitted("销售订单录入")){
				throw new ErpException("没有权限");
			}
		}
		//1. 生成日期
		orders.setCreatetime(new Date());
		//2. 订单状态
		orders.setState(Orders.STATE_CREATE);//未审核
		//3. 订单类型: 采购订单
		//orders.setType(Orders.TYPE_IN);
		//4. 计算合计金额
		double totalMoney = 0;
		for(Orderdetail od : orders.getOrderdetails()){
			totalMoney += od.getMoney();
			//设置明细与订单的关系
			od.setOrders(orders);
			//设置明细的状态
			od.setState(Orderdetail.STATE_NOT_IN);
		}
		//5. 合计金额
		orders.setTotalmoney(totalMoney);
		//6. 保存订单
		ordersDao.add(orders);
	}
	
	public List<Orders> getListByPage(Orders t1,Orders t2,Object param,int firstResult, int maxResults){
		//获取order列表
		List<Orders> list = super.getListByPage(t1, t2, param, firstResult, maxResults);
		//设置名称
		for(Orders o : list){
			o.setCreaterName(getEmpName(o.getCreater()));
			o.setCheckerName(getEmpName(o.getChecker()));
			o.setStarterName(getEmpName(o.getStarter()));
			o.setEnderName(getEmpName(o.getEnder()));
			o.setSupplierName(getSupplierName(o.getSupplieruuid()));
		}
		return list;
	}

	public List<Orders> getList(Orders t1,Orders t2,Object param){
		param = "下单日期";
		//获取order列表
		List<Orders> list = super.getList(t1, t2, param);
		//设置名称
		for(Orders o : list){
			o.setCreaterName(getEmpName(o.getCreater()));
			o.setCheckerName(getEmpName(o.getChecker()));
			o.setStarterName(getEmpName(o.getStarter()));
			o.setEnderName(getEmpName(o.getEnder()));
			o.setSupplierName(getSupplierName(o.getSupplieruuid()));
		}
		return list;
	}
	
	@Override
	@RequiresPermissions("采购订单审核")
	public void doCheck(Long uuid, Long empuuid) {
		//获取订单进入持久化状态
		Orders orders = ordersDao.get(uuid);
		//1. 判断订单状态是否为未审核
		if(!Orders.STATE_CREATE.equals(orders.getState())){
			throw new ErpException("该订单已经审核过了");
		}
		//2. 修改订单状态为 已审核
		orders.setState(Orders.STATE_CHECK);
		//3. 审核时间
		orders.setChecktime(new Date());
		//4. 审核人
		orders.setChecker(empuuid);
	}
	
	@Override
	@RequiresPermissions("采购订单确认")
	public void doStart(Long uuid, Long empuuid) {
		//获取订单进入持久化状态
		Orders orders = ordersDao.get(uuid);
		//1. 判断订单状态是否为已审核
		if(!Orders.STATE_CHECK.equals(orders.getState())){
			throw new ErpException("该订单已经确认过了");
		}
		//2. 修改订单状态为 已确认
		orders.setState(Orders.STATE_START);
		//3. 确认时间
		orders.setStarttime(new Date());
		//4. 确认人
		orders.setStarter(empuuid);
	}

	@Override
	public void export(OutputStream os, Long uuid) throws Exception {
		Orders o = ordersDao.get(uuid);
		//工作簿, 读取模板
		HSSFWorkbook wk = new HSSFWorkbook(new ClassPathResource("export_orders.xls").getInputStream());
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("o", o);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		model.put("sdf", sdf);
		o.setCreaterName(getEmpName(o.getCreater()));
		o.setCheckerName(getEmpName(o.getChecker()));
		o.setStarterName(getEmpName(o.getStarter()));
		o.setEnderName(getEmpName(o.getEnder()));
		o.setSupplierName(getSupplierName(o.getSupplieruuid()));
		
		model.put("createtime", formatDate(sdf,o.getCreatetime()));
		model.put("checktime", formatDate(sdf,o.getChecktime()));
		model.put("starttime", formatDate(sdf,o.getStarttime()));
		model.put("endtime", formatDate(sdf,o.getEndtime()));
		
		//转化器
		XLSTransformer transfer = new XLSTransformer();
		//把model中的对象写入工作簿中 
		transfer.transformWorkbook(wk,model);
		wk.write(os);
		wk.close();
	}
	
	private String formatDate(SimpleDateFormat sdf, Date date){
		if(null == date){
			return null;
		}
		return sdf.format(date);
	}
	
	public void export2(OutputStream os, Long uuid) throws Exception {
		Orders orders = ordersDao.get(uuid);
		//工作簿
		HSSFWorkbook wk = new HSSFWorkbook();
		String title = "";
		if(Orders.TYPE_OUT.equals(orders.getType())){
			title = "销 售 单";
		}
		if(Orders.TYPE_IN.equals(orders.getType())){
			title = "采 购 单";
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
		int size = orders.getOrderdetails().size();
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
		sheet.getRow(2).getCell(1).setCellValue(supplierDao.getName(orders.getSupplieruuid()));
		
		sheet.getRow(3).getCell(0).setCellValue("下单日期");
		setDate(sheet.getRow(3).getCell(1),orders.getCreatetime());
		sheet.getRow(4).getCell(0).setCellValue("审核日期");
		setDate(sheet.getRow(4).getCell(1),orders.getChecktime());
		sheet.getRow(5).getCell(0).setCellValue("采购日期");
		setDate(sheet.getRow(5).getCell(1),orders.getStarttime());
		sheet.getRow(6).getCell(0).setCellValue("入库日期");
		setDate(sheet.getRow(6).getCell(1),orders.getEndtime());
		sheet.getRow(3).getCell(2).setCellValue("经办人");
		sheet.getRow(3).getCell(3).setCellValue(empDao.getName(orders.getCreater()));
		sheet.getRow(4).getCell(2).setCellValue("经办人");
		sheet.getRow(4).getCell(3).setCellValue(empDao.getName(orders.getChecker()));
		sheet.getRow(5).getCell(2).setCellValue("经办人");
		sheet.getRow(5).getCell(3).setCellValue(empDao.getName(orders.getStarter()));
		sheet.getRow(6).getCell(2).setCellValue("经办人");
		sheet.getRow(6).getCell(3).setCellValue(empDao.getName(orders.getEnder()));
		
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
		for(Orderdetail od : orders.getOrderdetails()){
			row = sheet.getRow(i);
			row.getCell(0).setCellValue(od.getGoodsname());
			row.getCell(1).setCellValue(od.getPrice());
			row.getCell(2).setCellValue(od.getNum());
			row.getCell(3).setCellValue(od.getMoney());
			i++;
		}
		//合计
		sheet.getRow(rowCnt).getCell(0).setCellValue("合计");
		sheet.getRow(rowCnt).getCell(3).setCellValue(orders.getTotalmoney());
		
		wk.write(os);
		wk.close();
	}
	
	private void setDate(Cell cel, Date date){
		if(null != date){
			cel.setCellValue(date);
		}
	}
	
	/**
	 * 获取员工名称
	 * @param uuid
	 * @param empNameMap 员工编号与名称的缓存
	 * @return
	 */
	private String getEmpName(Long uuid){
		if(null == uuid){
			return null;
		}
		String empName = cacheManager.getCache("myCache").get("emp_" + uuid, String.class);
		if(null == empName){
			empName = empDao.get(uuid).getName();
			cacheManager.getCache("myCache").put("emp_" + uuid, empName);
		}
		return empName;
	}
	
	/**
	 * 获取供应商名称
	 * @param uuid
	 * @param supplierNameMap 供应商编号与名称的缓存
	 * @return
	 */
	private String getSupplierName(Long uuid){
		if(null == uuid){
			return null;
		}
		String supplierName = cacheManager.getCache("myCache").get("supplier_" + uuid, String.class);
		if(null == supplierName){
			supplierName = supplierDao.get(uuid).getName();
			cacheManager.getCache("myCache").put("supplier_" + uuid, supplierName);
		}
		return supplierName;
	}

	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}

	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
