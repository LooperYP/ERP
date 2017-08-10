package cn.itcast.erp.action;
import cn.itcast.erp.biz.IDepBiz;
import cn.itcast.erp.entity.Dep;
import cn.itcast.erp.exception.ErpException;

/**
 * 部门Action 
 * @author Administrator
 *
 */
public class DepAction extends BaseAction<Dep> {

	private IDepBiz depBiz;

	public void setDepBiz(IDepBiz depBiz) {
		this.depBiz = depBiz;
		super.setBaseBiz(this.depBiz);
	}
	
	public void delete(){
		try {
			depBiz.delete(getId());
			ajaxReturn(true, "删除成功");
		} catch (ErpException e) {
			e.printStackTrace();
			ajaxReturn(false, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			ajaxReturn(false, "删除失败");
		}
	}
}
