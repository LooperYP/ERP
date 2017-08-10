package cn.itcast.erp.biz.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import cn.itcast.erp.biz.IGoodsBiz;
import cn.itcast.erp.dao.IGoodsDao;
import cn.itcast.erp.dao.IGoodstypeDao;
import cn.itcast.erp.dao.IOrdersDao;
import cn.itcast.erp.entity.Goods;
import cn.itcast.erp.entity.Goodstype;
import cn.itcast.erp.entity.Orderdetail;
import cn.itcast.erp.entity.Orders;
/**
 * 商品业务逻辑类
 * @author Administrator
 *
 */
public class GoodsBiz extends BaseBiz<Goods> implements IGoodsBiz {

	private IGoodsDao goodsDao;
	private IGoodstypeDao goodstypeDao;
	
	public void setGoodsTypeDao(IGoodstypeDao goodsTypeDao) {
		this.goodstypeDao = goodsTypeDao;
	}
	public void setGoodsDao(IGoodsDao goodsDao) {
		this.goodsDao = goodsDao;
		super.setBaseDao(this.goodsDao);
	}
	private IOrdersDao ordersDao;
	public void setOrdersDao(IOrdersDao ordersDao) {
		this.ordersDao = ordersDao;
	}
	@Override
	public void export(OutputStream os , Goods goods){
		//创建工作簿
		HSSFWorkbook book = new HSSFWorkbook();
		//创建表
		HSSFSheet sheet = book.createSheet("商品表");
		// 内容样式
		HSSFCellStyle style_content = book.createCellStyle();
		style_content.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
		style_content.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
		style_content.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
		style_content.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
		//创建行
		HSSFRow row_0 = sheet.createRow(2);
		for (int i = 0; i < 8; i++) {
			//创建列
			HSSFCell cell = row_0.createCell(i);
			cell.setCellStyle(style_content);
		}
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,7));

		//****** 设置固定文本内容 ******//
		sheet.createRow(0).createCell(0).setCellValue("商品单");//设置标题内容
		sheet.getRow(2).getCell(0).setCellValue("编号");//
		sheet.getRow(2).getCell(1).setCellValue("名称");//
		sheet.getRow(2).getCell(2).setCellValue("产地");//
		sheet.getRow(2).getCell(3).setCellValue("厂家");//
		sheet.getRow(2).getCell(4).setCellValue("计量单位");//
		sheet.getRow(2).getCell(5).setCellValue("进货价格");//
		sheet.getRow(2).getCell(6).setCellValue("销售价格");//
		sheet.getRow(2).getCell(7).setCellValue("商品类型");//

		
		//****** 设置行高和列宽 ******//
		sheet.getRow(0).setHeight((short)1000);//设置标题行高
		// 设置内容部分的行高
		
		sheet.getRow(2).setHeight((short)500);
		
		//设置列宽
		for(int i = 0; i < 8; i++){
		    sheet.setColumnWidth(i, 4000);
		}

		// 内容部分的对齐设置
		style_content.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
		style_content.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
		
		// 标题样式
		HSSFCellStyle style_title = book.createCellStyle();
		style_title.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
		style_title.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
		HSSFFont font_title = book.createFont();//创建字体
		font_title.setFontName("黑体");//设置字体名称
		font_title.setBold(true);//加粗
		font_title.setFontHeightInPoints((short)23);//设置字体大小
		style_title.setFont(font_title);//设置样式的字体
		sheet.getRow(0).getCell(0).setCellStyle(style_title);//设置标题样式
		
		
		List<Goods> list = goodsDao.getList(goods, null, null);
		
		int rowIndex = 3 ;
		
		for (Goods goods2 : list) {
				sheet.createRow(rowIndex).createCell(0).setCellValue(goods2.getUuid());
				sheet.getRow(rowIndex).createCell(1).setCellValue(goods2.getName());
				sheet.getRow(rowIndex).createCell(2).setCellValue(goods2.getOrigin());
				sheet.getRow(rowIndex).createCell(3).setCellValue(goods2.getProducer());
				sheet.getRow(rowIndex).createCell(4).setCellValue(goods2.getUnit());
				sheet.getRow(rowIndex).createCell(5).setCellValue(goods2.getInprice());
				sheet.getRow(rowIndex).createCell(6).setCellValue(goods2.getOutprice());
				sheet.getRow(rowIndex).createCell(7).setCellValue(goods2.getGoodstype().getName());
				
				for (int i = 0; i < 8; i++) {
					sheet.getRow(rowIndex).getCell(i).setCellStyle(style_content);
					
				}
				rowIndex++;
		}
		
		try {
			book.write(os);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				book.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	
	
	/**
	 * 导入操作
	 */
	@Override
	public void doImport(InputStream is) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook(is);
		
		HSSFSheet sheet = wb.getSheetAt(0);

		
		int lastRowNum = sheet.getLastRowNum();

		Goods goods = null;
		for (int i = 3; i <= lastRowNum; i++) {
			
			String name = sheet.getRow(i).getCell(1).getStringCellValue();
			goods = new Goods();
			goods.setName(name);
			
			//根据商品的名称来判断是否已经存在
			List<Goods> list2 = goodsDao.getList(null, goods, null);
			
			if (list2.size()>0) {
				goods = list2.get(0);
			}
			
			goods.setOrigin(sheet.getRow(i).getCell(2).getStringCellValue());
			
			goods.setProducer(sheet.getRow(i).getCell(3).getStringCellValue());
			
			goods.setUnit(sheet.getRow(i).getCell(4).getStringCellValue());
			
			goods.setInprice(sheet.getRow(i).getCell(5).getNumericCellValue());
			
			goods.setOutprice(sheet.getRow(i).getCell(6).getNumericCellValue());
			
			
			/**
			 * 用goodstypeDao根据name来获取goodstype对象
			 */
			Goodstype goodstype2 = new Goodstype();
			List<Goodstype> list = goodstypeDao.getList(goodstype2, null, null);
			
			Goodstype gg = new Goodstype();
			
			for (Goodstype goodstype : list) {
				String name2 = goodstype.getName();
				
				if (name2.equals(sheet.getRow(i).getCell(7).getStringCellValue())) {
					gg.setName(name2);
					gg.setUuid(goodstype.getUuid());
				}
			}
			
			
			goods.setGoodstype(gg);
			
			
			if (list2.size()==0) {
				goodsDao.add(goods);
			}
			
			wb.close();
		}
	}
	
	public void setGoodstypeDao(IGoodstypeDao goodstypeDao) {
		this.goodstypeDao = goodstypeDao;
	}
	
	@Override
	public List<Goods> listByOrder(Goods goods1, Goods goods2, Long ordersuuid) {
		//构建一个空的list
		List<Goods> list = new ArrayList<Goods>();
		//根据ordersuuid 查询该订单中存在的goods
		Orders orders = ordersDao.get(ordersuuid);
		for (Orderdetail orderdetail : orders.getOrderdetails()) {
			Long goodsuuid = orderdetail.getGoodsuuid();
			Goods goods = goodsDao.get(goodsuuid);
			//向list中add goods
			list.add(goods);
		}
		return list;
	}

}
