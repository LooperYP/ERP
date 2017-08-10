package cn.itcast.erp.biz.impl;
import cn.itcast.erp.biz.IDepBiz;
import cn.itcast.erp.dao.IDepDao;
import cn.itcast.erp.dao.IEmpDao;
import cn.itcast.erp.entity.Dep;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.exception.ErpException;
/**
 * 部门业务逻辑类
 * @author Administrator
 *
 */
public class DepBiz extends BaseBiz<Dep> implements IDepBiz {

	private IDepDao depDao;
	
	private IEmpDao empDao;
	
	public void setDepDao(IDepDao depDao) {
		this.depDao = depDao;
		super.setBaseDao(this.depDao);
	}
	
	/**
	 * 删除部门
	 */
	public void delete(Long uuid){
		//查询是否有属于该部门的员工
		//构造查询条件
		Emp emp = new Emp();
		Dep dep = new Dep();
		dep.setUuid(uuid);
		emp.setDep(dep);
		long count = empDao.getCount(emp, null, null);
		if(count > 0){
			throw new ErpException("该部门下存在员工，不可删除");
		}
		super.delete(uuid);
	}

	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}
}
