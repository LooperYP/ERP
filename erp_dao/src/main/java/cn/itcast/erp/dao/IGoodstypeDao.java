package cn.itcast.erp.dao;

import java.util.List;

import cn.itcast.erp.entity.Goodstype;
/**
 * 商品分类数据访问接口
 * @author Administrator
 *
 */
public interface IGoodstypeDao extends IBaseDao<Goodstype>{
	public List<Long> getGoodstypeUuid();
}
