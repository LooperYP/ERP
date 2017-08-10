package cn.itcast.erp.biz;
import cn.itcast.erp.entity.Returnorderdetail;
/**
 * 退货订单明细业务逻辑层接口
 * @author Administrator
 *
 */
public interface IReturnorderdetailBiz extends IBaseBiz<Returnorderdetail>{

	void doInStore(long id, Long storeuuid, Long uuid);

	void doOutStore(long id, Long storeuuid, Long uuid);
}

