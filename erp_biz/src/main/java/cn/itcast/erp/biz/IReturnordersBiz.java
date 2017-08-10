package cn.itcast.erp.biz;
import java.io.OutputStream;

import cn.itcast.erp.entity.Returnorders;
/**
 * 退货订单业务逻辑层接口
 * @author Administrator
 *
 */
public interface IReturnordersBiz extends IBaseBiz<Returnorders>{

	void doCheck(long uuid, Long uuid2);
	
	void export(OutputStream outputStream, long id) throws Exception;
}

