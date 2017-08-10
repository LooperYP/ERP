package cn.itcast.erp.dao;

import java.util.List;

import cn.itcast.erp.entity.Menu;
/**
 * 菜单数据访问接口
 * @author Administrator
 *
 */
public interface IMenuDao extends IBaseDao<Menu>{

	/**
	 * 获取员工下的菜单
	 * @param empuuid
	 * @return
	 */
	List<Menu> getMenusByEmpuuid(Long empuuid);
}
