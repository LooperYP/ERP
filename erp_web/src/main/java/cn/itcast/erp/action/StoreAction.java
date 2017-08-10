package cn.itcast.erp.action;
import cn.itcast.erp.biz.IStoreBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Store;

/**
 * 仓库Action 
 * @author Administrator
 *
 */
public class StoreAction extends BaseAction<Store> {

	private IStoreBiz storeBiz;

	public void setStoreBiz(IStoreBiz storeBiz) {
		this.storeBiz = storeBiz;
		super.setBaseBiz(this.storeBiz);
	}
	
	/**
	 * 登陆者的仓库
	 */
	public void myList(){
		Store store = getT1();
		Emp loginUser = getLoginUser();
		if(null != loginUser){
			if(null == store){
				store = new Store();
				//构建查询条件
				setT1(store);
			}
			store.setEmpuuid(loginUser.getUuid());
			super.list();
		}
	}

}
