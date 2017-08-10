package cn.itcast.erp.realm;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import cn.itcast.erp.biz.IEmpBiz;
import cn.itcast.erp.biz.IMenuBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Menu;

public class ErpRealm extends AuthorizingRealm {
	
	private IEmpBiz empBiz;
	private IMenuBiz menuBiz;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		System.out.println("执行了授权方法");
		//创建授权信息
		SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
		//设置授权关键字
		/*sai.addStringPermission("商品");
		sai.addStringPermission("采购查询");
		sai.addStringPermission("采购审核");
		sai.addStringPermission("采购申请");*/
		//获取登陆用户
		Emp emp = (Emp)principals.getPrimaryPrincipal();
		//获取用户的菜单权限
		List<Menu> menus = menuBiz.getMenusByEmpuuid(emp.getUuid());
		for(Menu m : menus){
			sai.addStringPermission(m.getMenuname());
		}
		return sai;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		System.out.println("执行了认证方法");
		//登陆的令牌
		UsernamePasswordToken upt = (UsernamePasswordToken)token;
		//取用户名和密码并进行查询
		Emp emp = empBiz.findByUsernameAndPwd(upt.getUsername(), new String(upt.getPassword()));
		if(null != emp){
			//返回认证信息
			return new SimpleAuthenticationInfo(emp,upt.getPassword(),getName());
		}
		return null;
	}

	@Override
	protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		// TODO Auto-generated method stub
		return ((Emp)principals.getPrimaryPrincipal()).getUuid();
	}
	
	public void setEmpBiz(IEmpBiz empBiz) {
		this.empBiz = empBiz;
	}

	public void setMenuBiz(IMenuBiz menuBiz) {
		this.menuBiz = menuBiz;
	}

}
