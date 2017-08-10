package cn.itcast.erp.entity;
/**
 * 部门实体类
 * @author Administrator *
 */
public class InOutMoney {	
	private Long month;//月份
	private double inMoney;//销售收入
	private double inMoneyReturn;//采购退货收入
	private double outMoney;//采购支出
	private double outMoneyReturn;//销售退货支出
	
	public Long getMonth() {
		return month;
	}
	public void setMonth(Long month) {
		this.month = month;
	}
	public double getInMoney() {
		return inMoney;
	}
	public void setInMoney(double inMoney) {
		this.inMoney = inMoney;
	}
	public double getInMoneyReturn() {
		return inMoneyReturn;
	}
	public void setInMoneyReturn(double inMoneyReturn) {
		this.inMoneyReturn = inMoneyReturn;
	}
	public double getOutMoney() {
		return outMoney;
	}
	public void setOutMoney(double outMoney) {
		this.outMoney = outMoney;
	}
	public double getOutMoneyReturn() {
		return outMoneyReturn;
	}
	public void setOutMoneyReturn(double outMoneyReturn) {
		this.outMoneyReturn = outMoneyReturn;
	}

}
