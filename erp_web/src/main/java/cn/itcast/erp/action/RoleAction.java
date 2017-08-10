package cn.itcast.erp.action;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IRoleBiz;
import cn.itcast.erp.entity.Role;
import cn.itcast.erp.entity.Tree;

/**
 * 角色Action 
 * @author Administrator
 *
 */
public class RoleAction extends BaseAction<Role> {
	
	private static final Logger log = LoggerFactory.getLogger(RoleAction.class);

	private IRoleBiz roleBiz;
	private String checkedIds;// 菜单ID的字符串，多个菜单ID，以逗号分割

	public void setRoleBiz(IRoleBiz roleBiz) {
		this.roleBiz = roleBiz;
		super.setBaseBiz(this.roleBiz);
	}
	
	/**
	 * 读取角色菜单
	 */
	public void readRoleMenu(){
		List<Tree> list = roleBiz.readRoleMenu(getId());
		write(JSON.toJSONString(list));
	}
	
	/**
	 * 更新角色菜单
	 */
	public void updateRoleMenu(){
		try {
			roleBiz.updateRoleMenu(getId(), checkedIds);
			ajaxReturn(true,"更新成功");
		} catch (Exception e) {
			log.error("更新角色菜单失败",e);
			ajaxReturn(false,"更新失败");
		}
	}

	public void setCheckedIds(String checkedIds) {
		this.checkedIds = checkedIds;
	}

}
