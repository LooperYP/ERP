package cn.itcast.erp.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

public class ErpAuthorizationFilter extends AuthorizationFilter {

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		Subject subject = getSubject(request, response);
        String[] perms = (String[]) mappedValue;
        HttpServletRequest req = (HttpServletRequest)request;
        boolean isPermitted = false;
        System.out.println(req.getRequestURI() + "?" + req.getQueryString());
        if (perms != null && perms.length > 0) {
        	//只要有一个，就让它通过
            for(String perm : perms){
            	if(subject.isPermitted(perm)){
            		isPermitted = true;
            		break;
            	}
            }
        }else{
        	isPermitted = true;
        }

        return isPermitted;
	}

}
