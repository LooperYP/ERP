package cn.itcast.erp.biz.impl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.cache.CacheManager;
import cn.itcast.erp.biz.IEmpBiz;
import cn.itcast.erp.dao.IDepDao;
import cn.itcast.erp.dao.IEmpDao;
import cn.itcast.erp.dao.IRoleDao;
import cn.itcast.erp.entity.Dep;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Role;
import cn.itcast.erp.entity.Tree;
import cn.itcast.erp.exception.ErpException;
import redis.clients.jedis.Jedis;
/**
 * 员工业务逻辑类
 * @author Administrator
 *
 */
public class EmpBiz extends BaseBiz<Emp> implements IEmpBiz {

	private IEmpDao empDao;
	private CacheManager cacheManager;
	private IRoleDao roleDao;
	private IDepDao depDao;
	public void setDepDao(IDepDao depDao) {
		this.depDao = depDao;
	}

	public void setShiroCacheManager(org.apache.shiro.cache.CacheManager shiroCacheManager) {
		this.shiroCacheManager = shiroCacheManager;
	}

	private org.apache.shiro.cache.CacheManager shiroCacheManager;
	private Jedis jedis;
	
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
		super.setBaseDao(this.empDao);
	}

	@Override
	public Emp findByUsernameAndPwd(String username, String pwd) {
		//1： source 要加密的字符串
		//2: salt 盐, 扰乱码
		//3: 散列次数
		//Md5Hash md5 = new Md5Hash(pwd, username, 2);
		String encryptedPwd = encrypt(pwd,username);//md5.toString();
		System.out.println(encryptedPwd);
		return empDao.findByUsernameAndPwd(username, encryptedPwd);
	}

	@Override
	public void updatePwd(String newPwd, String oldPwd, Long uuid) {
		//验证旧密码是否正确
		Emp emp = empDao.get(uuid);
		//加密旧密码
		String encryptedOldPwd = encrypt(oldPwd,emp.getUsername());
		if(!emp.getPwd().equals(encryptedOldPwd)){
			throw new ErpException("原密码不正确");
		}
		//emp.setPwd(pwd);
		//加密新密码
		String encryptedNewPwd = encrypt(newPwd,emp.getUsername());
		//修改密码		
		empDao.updatePwd(encryptedNewPwd, uuid);
	}
	
	private String encrypt(String pwd, String username){
		Md5Hash md5 = new Md5Hash(pwd, username, 2);
		return md5.toString();
	}

	@Override
	public void updatePwd_reset(String newPwd, Long uuid) {
		Emp emp = empDao.get(uuid);
		empDao.updatePwd(encrypt(newPwd,emp.getUsername()), uuid);
	}
	
	@Override
	public void add(Emp emp){
		//取出登陆名，用登陆名做为默认密码
		String username = emp.getUsername();
		//密码加密后才能存入数据库
		String encryptedPwd = encrypt(username, username);
		//设置初始化密码
		emp.setPwd(encryptedPwd);
		empDao.add(emp);
	}
	
	@Override
	public void update(Emp e){
		//把缓存中的员工名称删除
		cacheManager.getCache("myCache").evict("emp_" + e.getUuid());
		empDao.update(e);
	}

	@Override
	public List<Tree> readEmpRole(Long uuid) {
		Emp emp = empDao.get(uuid);
		//员式下的角色
		List<Role> empRoles = emp.getRoles();
		//取出所有的角色
		List<Role> roleList = roleDao.getList(null, null, null);
		Tree tree = null;
		List<Tree> treeList = new ArrayList<Tree>();
		for(Role r : roleList){
			tree = new Tree();
			tree.setId(r.getUuid() + "");//角色编号
			tree.setText(r.getName());//角色名称
			if(empRoles.contains(r)){
				//如果员工有这个角色，就让它选中
				tree.setChecked(true);
			}
			treeList.add(tree);
		}
		return treeList;
	}

	@Override
	public void updateEmpRole(Long uuid, String checkedIds) {
		//员工进入持久化状态
		Emp emp = empDao.get(uuid);
		//清除员工下的角色
		emp.setRoles(new ArrayList<Role>());
		//得到选中的角色编号数组
		String[] ids = checkedIds.split(",");
		for(String id : ids){
			//给员工设置角色
			emp.getRoles().add(roleDao.get(Long.valueOf(id)));
		}
		Cache<Object, Object> cache = shiroCacheManager.getCache("cn.itcast.erp.realm.ErpRealm.authorizationCache");
		cache.remove(uuid);
		//清除该员工的菜单缓存
		jedis.del("team04_menuList_" + uuid);
		
	}

	@Override
	public void export(OutputStream os, Emp emp) {
		//创建工作簿
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		
		//设置表头
		sheet.createRow(0).createCell(0).setCellValue("员工表");
		
		
		// 固定内容样式
		HSSFCellStyle style_content = wb.createCellStyle();
		style_content.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
		style_content.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
		style_content.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
		style_content.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
		style_content.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFFont font_content = wb.createFont();//创建字体
		font_content.setFontName("黑体");//设置字体名称
		font_content.setBold(true);//加粗
		font_content.setFontHeightInPoints((short)15);//设置字体大小
		style_content.setFont(font_content);//设置样式的字体
		
		
		// 不固定内容样式
		HSSFCellStyle style_content2 = wb.createCellStyle();
		style_content2.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
		style_content2.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
		style_content2.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
		style_content2.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
		style_content2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFFont font_content2 = wb.createFont();//创建字体
		font_content2.setFontName("宋体");//设置字体名称
		font_content2.setFontHeightInPoints((short)13);//设置字体大小
		style_content2.setFont(font_content2);//设置样式的字体
		
		//创建第三行的固定行
		HSSFRow row_2 = sheet.createRow(2);
		
		row_2.createCell(0).setCellValue("编号");
		row_2.createCell(1).setCellValue("登录名");
		row_2.createCell(2).setCellValue("真实姓名");
		row_2.createCell(3).setCellValue("性别");
		row_2.createCell(4).setCellValue("邮件地址");
		row_2.createCell(5).setCellValue("联系电话");
		row_2.createCell(6).setCellValue("联系地址");
		row_2.createCell(7).setCellValue("出生年月日");
		row_2.createCell(8).setCellValue("部门");
		
		for (int i = 0; i < 9; i++) {
			row_2.getCell(i).setCellStyle(style_content);
			
		}
			
		
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

		
		//****** 设置行高和列宽 ******//
		sheet.getRow(0).setHeight((short)1000);//设置标题行高
		// 设置内容部分的行高
		
		sheet.getRow(2).setHeight((short)500);
		
		//设置列宽
		for(int i = 0; i < 8; i++){
			if (i!=4||i!=6) {
				sheet.setColumnWidth(i, 4000);
				
			}else{
				sheet.setColumnWidth(i, 6000);
			}
		}

		// 内容部分的对齐设置
		style_content.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
		style_content.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
		
		// 标题样式
		HSSFCellStyle style_title = wb.createCellStyle();
		style_title.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
		style_title.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
		HSSFFont font_title = wb.createFont();//创建字体
		font_title.setFontName("黑体");//设置字体名称
		font_title.setBold(true);//加粗
		font_title.setFontHeightInPoints((short)24);//设置字体大小
		style_title.setFont(font_title);//设置样式的字体
		sheet.getRow(0).getCell(0).setCellStyle(style_title);//设置标题样式
		
		List<Emp> list = empDao.getList(emp, null, null);
		
		int rowIndex = 3;
		
		if (list.size()>0) {
			for (Emp getList_emp : list) {
				HSSFRow row_3 = sheet.createRow(rowIndex);
				row_3.createCell(0).setCellValue(getList_emp.getUuid());
				row_3.createCell(1).setCellValue(getList_emp.getUsername());
				row_3.createCell(2).setCellValue(getList_emp.getName());
				row_3.createCell(3).setCellValue(getList_emp.getGender());
				row_3.createCell(4).setCellValue(getList_emp.getEmail());
				row_3.createCell(5).setCellValue(getList_emp.getTele());
				row_3.createCell(6).setCellValue(getList_emp.getAddress());
				row_3.createCell(7).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(getList_emp.getBirthday()));//日期格式化
				row_3.createCell(8).setCellValue(getList_emp.getDep().getName());
				
				for (int i = 0; i < 9; i++) {
					sheet.getRow(rowIndex).getCell(i).setCellStyle(style_content2);
					
				}
				
				rowIndex++;
			}
			
		}

		try {
			wb.write(os);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				wb.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setRoleDao(IRoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	@Override
	public void doImport(InputStream is) {
		// TODO Auto-generated method stub
		HSSFWorkbook wb = null;
		try {
			wb = new HSSFWorkbook(is);
			HSSFSheet sheet = wb.getSheetAt(0);
			
			int lastRowNum = sheet.getLastRowNum();
			
			
			Emp emp = null;
			for (int i = 3; i <= lastRowNum; i++) {
				HSSFRow row = sheet.getRow(i);
				
				String username = row.getCell(1).getStringCellValue();
				
				emp = new Emp();
				emp.setUsername(username);
				List<Emp> list = empDao.getList(null, emp, null);
				
				if (list.size()>0) {
					emp = list.get(0);
				}
				emp.setName(row.getCell(2).getStringCellValue());
				
				double numericCellValue = row.getCell(3).getNumericCellValue();
				
				emp.setGender(new Double(numericCellValue).longValue());
				
				emp.setEmail(row.getCell(4).getStringCellValue());
				
/*				double tele = row.getCell(4).getNumericCellValue();
 * 				String tele = String.valueOf(tele)
 * */
				
				emp.setTele(row.getCell(5).getStringCellValue());
				
				emp.setAddress(row.getCell(6).getStringCellValue());
				
				try {
					//字符串转日期
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date birthday = sdf.parse(row.getCell(7).getStringCellValue());
					emp.setBirthday(birthday);
				} catch (Exception e) {
					e.printStackTrace();
				}
				/**
				 * 用depDao根据name来获取dep对象
				 */
				String depName = row.getCell(8).getStringCellValue();
				
				Dep dep = new Dep();
				dep.setName(depName);
				List<Dep> list2 = depDao.getList(null,dep,null);
				Dep dep2 = list2.get(0);
				
				emp.setDep(dep2);
				
				if (list.size()==0) {
					empDao.add(emp);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				wb.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
				
		
		
	}

	
}
