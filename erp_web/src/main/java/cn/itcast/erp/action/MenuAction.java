package cn.itcast.erp.action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IMenuBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Menu;

/**
 * 菜单Action 
 * @author Administrator
 *
 */
public class MenuAction extends BaseAction<Menu> {
	
	private static final Logger log = LoggerFactory.getLogger(MenuAction.class);

	private IMenuBiz menuBiz;

	public void setMenuBiz(IMenuBiz menuBiz) {
		this.menuBiz = menuBiz;
		super.setBaseBiz(this.menuBiz);
	}
	
	/**
	 * 动态加载菜单
	 */
	public void getMenuTree(){
		//Menu root = menuBiz.get("0");
		Emp loginUser = getLoginUser();
		if(null != loginUser){
			Menu menu = menuBiz.readMenusByEmpuuid(loginUser.getUuid());
			String menuJsonString = JSON.toJSONString(menu);
			log.debug(menuJsonString);
			write(menuJsonString);
		}else{
			log.info("用户没有登陆");
		}
	}
	

}
