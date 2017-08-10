package cn.itcast.erp.biz;
import java.io.OutputStream;

import cn.itcast.erp.entity.Orders;
/**
 * 订单业务逻辑层接口
 * @author Administrator
 *
 */
public interface IOrdersBiz extends IBaseBiz<Orders>{

	/**
	 * 审核订单
	 * @param uuid 订单编号
	 * @param empuuid 操作员编号
	 */
	void doCheck(Long uuid,Long empuuid);
	
	/**
	 * 审核订单
	 * @param uuid 订单编号
	 * @param empuuid 操作员编号
	 */
	void doStart(Long uuid,Long empuuid);
	
	/**
	 * 导出数据
	 * @param os
	 * @param uuid
	 * @throws Exception
	 */
	void export(OutputStream os, Long uuid) throws Exception;
}

