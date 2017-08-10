package cn.itcast.erp.biz.impl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.cache.CacheManager;

import cn.itcast.erp.biz.ISupplierBiz;
import cn.itcast.erp.dao.ISupplierDao;
import cn.itcast.erp.entity.Supplier;
/**
 * 供应商业务逻辑类
 * @author Administrator
 *
 */
public class SupplierBiz extends BaseBiz<Supplier> implements ISupplierBiz {

	private ISupplierDao supplierDao;
	private CacheManager cacheManager;
	
	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
		super.setBaseDao(this.supplierDao);
	}

	@Override
	public void export(OutputStream os, Supplier t1) throws IOException {
		List<Supplier> supplierList = supplierDao.getList(t1, null, null);
		int size = supplierList.size();
		//工作簿
		HSSFWorkbook wk = new HSSFWorkbook();
		//创建工作表
		HSSFSheet sheet = null;
		if (t1.getType().equals("1")) {
			sheet = wk.createSheet("供应商表");
		}else if (t1.getType().equals("2")) {
			sheet = wk.createSheet("客户表");
		}else{
			try {
				throw new Exception("工作表明不对");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		//创建行 行的索引，从0开始
		HSSFRow row = sheet.createRow(0);//表头
		String[] headers = {"名称","地址","联系人","电话","Email"};
		for(int i = 0; i < headers.length; i++){
			row.createCell(i).setCellValue(headers[i]);
		}
		if(size > 0){
			Supplier s = null;
			for(int i = 0; i < size; i++){
				s = supplierList.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(s.getName());
				row.createCell(1).setCellValue(s.getAddress());
				row.createCell(2).setCellValue(s.getContact());
				row.createCell(3).setCellValue(s.getTele());
				row.createCell(4).setCellValue(s.getEmail());
			}
		}
		wk.write(os);
		wk.close();
	}

	@Override
	public void doImport(InputStream is) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook(is);
		//获取第一个工作表
		HSSFSheet sheet = wb.getSheetAt(0);
		//工作表名称
		String sheetName = sheet.getSheetName();
		String type = "0";
		//根据工作表名称来判断是供应商还是客户
		if("供应商表".equals(sheetName)){
			type = "1";
		}
		if("客户表".equals(sheetName)){
			type = "2";
		}
		
		//读取内容
		String name = null;//名称
		HSSFRow row = null;
		Supplier supplier = null;
		List<Supplier> list = null;
		for(int i = 1; i <= sheet.getLastRowNum(); i++){
			//判断 是否 存在？名称
			row = sheet.getRow(i);
			supplier = new Supplier();
			name = row.getCell(0).getStringCellValue();
			supplier.setName(name);
			//查询是否存在
			list = supplierDao.getList(null, supplier, null);
			if(list.size() > 0){
				supplier = list.get(0);//持久化状态
			}
			//设置属性内容
			supplier.setAddress(row.getCell(1).getStringCellValue());
			supplier.setContact(row.getCell(2).getStringCellValue());
			supplier.setTele(row.getCell(3).getStringCellValue());
			supplier.setEmail(row.getCell(4).getStringCellValue());
			
			//如果不存在，就插入新的数据
			if(list.size() == 0){
				supplier.setType(type);
				supplierDao.add(supplier);
			}
		}
		wb.close();
	}
	
	/**
	 * 更新
	 */
	public void update(Supplier t){
		//把缓存中的供应商名称删除
		cacheManager.getCache("myCache").evict("supplier_" + t.getUuid());
		supplierDao.update(t);
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
}
