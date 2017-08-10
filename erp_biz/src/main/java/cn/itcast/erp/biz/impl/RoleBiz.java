package cn.itcast.erp.biz.impl;
import java.util.ArrayList;
import java.util.List;

import cn.itcast.erp.biz.IRoleBiz;
import cn.itcast.erp.dao.IMenuDao;
import cn.itcast.erp.dao.IRoleDao;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Menu;
import cn.itcast.erp.entity.Role;
import cn.itcast.erp.entity.Tree;
import redis.clients.jedis.Jedis;
/**
 * 角色业务逻辑类
 * @author Administrator
 *
 */
public class RoleBiz extends BaseBiz<Role> implements IRoleBiz {

	private IRoleDao roleDao;
	
	private IMenuDao menuDao;
	
	private Jedis jedis;
	
	public void setRoleDao(IRoleDao roleDao) {
		this.roleDao = roleDao;
		super.setBaseDao(this.roleDao);
	}

	@Override
	public List<Tree> readRoleMenu(Long uuid) {
		Role role = roleDao.get(uuid);
		//角色下的菜单
		List<Menu> roleMenus = role.getMenus();
		//所有的菜单
		Menu root = menuDao.get("0");
		List<Menu> menus_1 = root.getMenus();//一级菜单
		List<Tree> treeList = new ArrayList<Tree>();//返回的所有一级菜单
		//把菜单转成Tree
		Tree tree = null;
		for(Menu m1 : menus_1){
			tree = createTree(m1);
			//二级菜单
			if(null != m1.getMenus() && m1.getMenus().size() > 0){
				for(Menu m2 : m1.getMenus()){
					//二级菜单
					Tree t2 = createTree(m2);
					//因hibernate的一缓存是用对象id来做标识，相同id,只保存一个对象，roleMenus中的id对应对象m2是同一个对象
					if(roleMenus.contains(m2)){
						//如果角色下有这个菜单，那么就让它选中
						t2.setChecked(true);
					}
					//把二级菜单加到一级中去
					tree.getChildren().add(t2);
				}
			}
			treeList.add(tree);
		}
		return treeList;
	}
	
	@Override
	public void updateRoleMenu(Long uuid, String checkedIds) {
		//role进入持久化状态
		Role role = roleDao.get(uuid);
		//清除角色下的菜单
		role.setMenus(new ArrayList<Menu>());//delete from role_menu where roleuuid=?
		//得到选中的菜单编号数组
		String[] ids = checkedIds.split(",");
		for(String id : ids){
			//给角色设置菜单
			role.getMenus().add(menuDao.get(id));
		}
		//清除拥有该角色的员工的菜单缓存
		List<Emp> empList = role.getEmps();
		for(Emp emp : empList){
			jedis.del("team04_menuList_" + emp.getUuid());
		}
	}
	
	
	private Tree createTree(Menu m){
		Tree tree = new Tree();
		tree.setId(m.getMenuid());
		tree.setText(m.getMenuname());
		tree.setChildren(new ArrayList<Tree>());
		return tree;
	}

	public void setMenuDao(IMenuDao menuDao) {
		this.menuDao = menuDao;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	
}
