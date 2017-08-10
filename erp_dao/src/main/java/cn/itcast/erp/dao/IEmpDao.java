package cn.itcast.erp.dao;

import cn.itcast.erp.entity.Emp;
/**
 * 员工数据访问接口
 * @author Administrator
 *
 */
public interface IEmpDao extends IBaseDao<Emp>{

	/**
	 * 登陆时验证用户是否存在
	 * @param username
	 * @param pwd
	 * @return
	 */
	Emp findByUsernameAndPwd(String username, String pwd);
	
	/**
	 * 修改密码
	 * @param newPwd
	 * @param uuid
	 */
	void updatePwd(String newPwd, Long uuid);
}
