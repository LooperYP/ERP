package cn.itcast.erp.action;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.itcast.erp.biz.ISupplierBiz;
import cn.itcast.erp.entity.Supplier;

/**
 * 供应商Action 
 * @author Administrator
 *
 */
public class SupplierAction extends BaseAction<Supplier> {
	
	private static final Logger log = LoggerFactory.getLogger(SupplierAction.class);
	
	private File file;
	private String fileFileName;
	private String fileContentType;

	private ISupplierBiz supplierBiz;

	public void setSupplierBiz(ISupplierBiz supplierBiz) {
		this.supplierBiz = supplierBiz;
		super.setBaseBiz(this.supplierBiz);
	}
	
	/**
	 * 自动填充
	 */
	public void list(){
		String supplierName = getQ();
		if(null == getT1()){
			//构建查询条件
			setT1(new Supplier());
		}
		getT1().setName(supplierName);
		super.list();
	}
	
	/**
	 * 导出数据
	 */
	public void export(){
		//导出的文件名
		String filename = "供应商";
		//根据类型来决定文件名
		if("2".equals(getT1().getType())){
			filename = "客户";
		}
		//加上文件的扩展名
		filename += ".xls";
		try {
			//进行ISO-8859-1，传输中对中文的转码
			filename = new String(filename.getBytes(), "ISO-8859-1");
			HttpServletResponse res = ServletActionContext.getResponse();
			//告诉客户端，传输是一个文件
			res.setHeader("Content-Disposition", "attachment;filename=" + filename);
			supplierBiz.export(res.getOutputStream(), getT1());
		} catch (Exception e) {
			log.error("导出数据失败",e);
		}
	}
	
	/**
	 * 导入
	 */
	public void doImport(){
		try {
			if(!"application/vnd.ms-excel".equals(fileContentType)){
				if(!fileFileName.endsWith(".xls")){
					ajaxReturn(false,"不是excel文件xls类型");
					return;
				}
			}
			supplierBiz.doImport(new FileInputStream(file));
			ajaxReturn(true,"导入成功");
		} catch (Exception e) {
			log.error("导入失败",e);
		}
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
