package cn.itcast.erp.biz;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cn.itcast.erp.entity.Goods;
/**
 * 商品业务逻辑层接口
 * @author Administrator
 *
 */
public interface IGoodsBiz extends IBaseBiz<Goods>{
	/**
	 * 商品导出操作
	 * 
	 */
	void export(OutputStream os ,Goods goods ) throws Exception;
	
	
	
	/**
	 * 商品导入操作
	 */
	void doImport(InputStream is) throws Exception;



	List<Goods> listByOrder(Goods goods1, Goods goods2, Long ordersuuid);
}

