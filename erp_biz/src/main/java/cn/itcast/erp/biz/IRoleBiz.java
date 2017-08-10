package cn.itcast.erp.biz;
import java.util.List;

import cn.itcast.erp.entity.Role;
import cn.itcast.erp.entity.Tree;
/**
 * 角色业务逻辑层接口
 * @author Administrator
 *
 */
public interface IRoleBiz extends IBaseBiz<Role>{

	/**
	 * 角色下的菜单
	 * @param uuid
	 * @return
	 */
	List<Tree> readRoleMenu(Long uuid); 
	
	/**
	 * 更新角色权限
	 * @param uuid 角色编号
	 * @param checkedIds 菜单ID的字符串，多个菜单ID，以逗号分割
	 */
	void updateRoleMenu(Long uuid, String checkedIds);
}

