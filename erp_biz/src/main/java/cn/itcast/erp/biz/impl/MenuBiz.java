package cn.itcast.erp.biz.impl;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IMenuBiz;
import cn.itcast.erp.dao.IMenuDao;
import cn.itcast.erp.entity.Menu;
import redis.clients.jedis.Jedis;
/**
 * 菜单业务逻辑类
 * @author Administrator
 *
 */
public class MenuBiz extends BaseBiz<Menu> implements IMenuBiz {

	private IMenuDao menuDao;
	
	private Jedis jedis;
	
	public void setMenuDao(IMenuDao menuDao) {
		this.menuDao = menuDao;
		super.setBaseDao(this.menuDao);
	}

	@Override
	public List<Menu> getMenusByEmpuuid(Long empuuid) {
		//从缓存中取
		String menuListJsonString = jedis.get("team04_menuList_" + empuuid);
		List<Menu> menuList = null;
		if(null != menuListJsonString){
			//如果有，表示缓存过，从缓存中出菜单
			menuList = JSON.parseArray(menuListJsonString, Menu.class);
		}else{
			//没有缓存过，进行查询
			menuList = menuDao.getMenusByEmpuuid(empuuid);
			//转成json字符串
			menuListJsonString = JSON.toJSONString(menuList);
			//存入 缓存中
			jedis.set("team04_menuList_" + empuuid, menuListJsonString);
		}
		return menuList;
	}
	
	@Override
	public Menu readMenusByEmpuuid(Long empuuid) {
		//所有的菜单
		Menu root = menuDao.get("0");
		
		Menu result = cloneMenu(root);
		//员工底下的菜单
		List<Menu> empMenus = getMenusByEmpuuid(empuuid);
		//复制
		Menu _m1 = null;
		Menu _m2 = null;
		for(Menu m1 : root.getMenus()){
			//一级菜单
			_m1 = cloneMenu(m1);
			for(Menu m2 : m1.getMenus()){
				//二级菜单
				if(empMenus.contains(m2)){
					//如果员工底下有这个二级菜单，把它复制过来，并且加到复制的1级菜单底
					_m2 = cloneMenu(m2);
					_m1.getMenus().add(_m2);
				}
			}
			//如果复制后的1级菜单底下有二级菜单，应该把加进来
			if(_m1.getMenus().size() > 0){
				result.getMenus().add(_m1);
			}
		}
		return result;
	}
	
	/**
	 * 复制菜单
	 * @param src
	 * @return
	 */
	private Menu cloneMenu(Menu src){
		Menu menu = new Menu();
		menu.setIcon(src.getIcon());
		menu.setMenuid(src.getMenuid());
		menu.setMenuname(src.getMenuname());
		menu.setUrl(src.getUrl());
		//需要判断后，才能复制它底下的菜单
		menu.setMenus(new ArrayList<Menu>());
		return menu;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}
	
}
