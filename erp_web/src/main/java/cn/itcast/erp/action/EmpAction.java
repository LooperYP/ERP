package cn.itcast.erp.action;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.itcast.erp.biz.IEmpBiz;
import cn.itcast.erp.entity.Emp;
import cn.itcast.erp.entity.Tree;
import cn.itcast.erp.exception.ErpException;

/**
 * 员工Action 
 * @author Administrator
 *
 */
public class EmpAction extends BaseAction<Emp> {
	
	private static final Logger log = LoggerFactory.getLogger(EmpAction.class);

	private IEmpBiz empBiz;
	private String checkedIds;// 角色ID的字符串，多个角色ID，以逗号分割
	
	private String oldPwd;//原密码
	private String newPwd;//新密码
	private File file;
	private String fileFileName;
	private String fileContentType;
	public void setEmpBiz(IEmpBiz empBiz) {
		this.empBiz = empBiz;
		super.setBaseBiz(this.empBiz);
	}
	
	
	/**
	 * 导入文档数据
	 */
	public void doImport(){
		
		try {
			if(!"application/vnd.ms-excel".equals(fileContentType)){
				if(!(fileFileName.endsWith(".xls"))){
					ajaxReturn(false,"不是excel文件-xls类型");
					return;
				}
			}
			empBiz.doImport(new FileInputStream(file));
			ajaxReturn(true,"导入成功");
		} catch (Exception e) {
			log.error("导入失败",e);
		}
	}
	

	/**
	 * 导出文档
	 */
	public void export(){
		//导出的文件名
		String filename = "员工表.xls";

		try {
			//进行ISO-8859-1，传输中对中文的转码
			filename = new String(filename.getBytes(), "ISO-8859-1");
			HttpServletResponse res = ServletActionContext.getResponse();
			//告诉客户端，传输是一个文件
			res.setHeader("Content-Disposition", "attachment;filename=" + filename);
			empBiz.export(res.getOutputStream(), getT1());
		} catch (Exception e) {
			log.error("导出数据失败",e);
		}
	}
	
	/**
	 * 修改密码
	 */
	public void updatePwd(){
		Emp loginUser = getLoginUser();
		try {
			empBiz.updatePwd(newPwd, oldPwd, loginUser.getUuid());
			ajaxReturn(true,"修改成功");
		} catch (ErpException e) {
			log.error("修改密码失败",e);
			ajaxReturn(false,e.getMessage());
		} catch (Exception e) {
			log.error("修改密码失败",e);
			ajaxReturn(false,"修改失败");
		}
	}
	
	/**
	 * 重置密码
	 */
	public void updatePwd_reset(){
		try {
			empBiz.updatePwd_reset(newPwd,getId());
			ajaxReturn(true,"重置密码成功");
		} catch (Exception e) {
			log.error("重置密码失败",e);
			ajaxReturn(false,"重置密码失败");
		}
	}
	
	/**
	 * 读取用户角色
	 */
	public void readEmpRole(){
		List<Tree> list = empBiz.readEmpRole(getId());
		write(JSON.toJSONString(list));
	}
	
	/**
	 * 更新用户角色
	 */
	public void updateEmpRole(){
		try {
			empBiz.updateEmpRole(getId(), checkedIds);
			ajaxReturn(true,"更新成功");
		} catch (Exception e) {
			log.error("更新用户角色失败",e);
			ajaxReturn(false,"更新失败");
		}
	}

	public void setCheckedIds(String checkedIds) {
		this.checkedIds = checkedIds;
	}
	
	public void setOldPwd(String oldPwd) {
		this.oldPwd = oldPwd;
	}

	public void setNewPwd(String newPwd) {
		this.newPwd = newPwd;
	}


	public void setFile(File file) {
		this.file = file;
	}


	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}


	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}
	
	

}
