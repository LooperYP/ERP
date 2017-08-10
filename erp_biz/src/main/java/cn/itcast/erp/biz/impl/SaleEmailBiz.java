package cn.itcast.erp.biz.impl;

import cn.itcast.erp.biz.ISaleEmailBiz;
import cn.itcast.erp.dao.IGoodsDao;
import cn.itcast.erp.dao.ISupplierDao;
import cn.itcast.erp.entity.Goods;
import cn.itcast.erp.entity.Supplier;
import cn.itcast.erp.util.MailUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daviddai on 2017/8/2.
 */
public class SaleEmailBiz implements ISaleEmailBiz {
    private ISupplierDao supplierDao;
    private MailUtil mailUtil;
    private String title;
    private Configuration freeMarker;
    private IGoodsDao goodsDao;


    public void setTitle(String title) {
        this.title = title;
    }

    public void setMailUtil(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    public void setSupplierDao(ISupplierDao supplierDao) {
        this.supplierDao = supplierDao;
    }

    //发送销售模板邮件给所有客户
    @Override
    public void sendEmailToAllCustomer() throws Exception {

            Supplier supplier1 = new Supplier();
            supplier1.setType("2");//2为customer
            List<Supplier> suppliers = supplierDao.getList(supplier1, null, null);
            //查询所有商品
            List<Goods> goodsList = goodsDao.getList(null, null, null);
            //获取模板
            Template template = freeMarker.getTemplate("saleEmail.html");
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("goodsList", goodsList);
            if (suppliers.size() > 0) {
                for (Supplier supplier : suppliers) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    //把对象转化到模板里出,输出成字符串
                    model.put("supplierName",supplier.getName());
                    String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                    mailUtil.sendMail(supplier.getEmail(), title.replace("[time]", sdf.format(new Date())), content);
                    //休眠时间   4000毫秒
                    Thread.sleep(4000);
                }
            }

    }

    public void setFreeMarker(Configuration freeMarker) {
        this.freeMarker = freeMarker;
    }

    ;

    public void setGoodsDao(IGoodsDao goodsDao) {
        this.goodsDao = goodsDao;
    }

    ;
}
