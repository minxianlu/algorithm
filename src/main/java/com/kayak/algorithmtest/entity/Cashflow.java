package com.kayak.algorithmtest.entity;

/**
 * @author pc_xjf
 *
 */
public class Cashflow {
	/**
	 * 识别号
	 */
	String id;
	/**
	 * 发生类型1——起息、10——付息、12——提前还本、11——到期还本
	 */
	Integer change_id;
	/**
	 * 付息日
	 */
	String pay_date;
	/**
	 * 开始日期
	 */
	String start_date;
	/**
	 * 利率
	 */
	String coupon_rate;
	/**
	 * 单位资金流
	 */
	String amount;
	/**
	 * 是否发生变化
	 */
	Integer is_change;
	/**
	 * 剩余本金
	 */
	String end_money;
	
	/**
	 * 变动类型优先级
	 */
	Integer translevel;
	

	public String getEnd_money() {
		return end_money;
	}
	public void setEnd_money(String endMoney) {
		end_money = endMoney;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getChange_id() {
		return change_id;
	}
	public void setChange_id(Integer changeId) {
		this.change_id = changeId;
	}
	public String getPay_date() {
		return pay_date;
	}
	public void setPay_date(String payDate) {
		this.pay_date = payDate;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String startDate) {
		this.start_date = startDate;
	}
	public String getCoupon_rate() {
		return coupon_rate;
	}
	public void setCoupon_rate(String couponRate) {
		this.coupon_rate = couponRate;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public Integer getIs_change() {
		return is_change;
	}
	public void setIs_change(Integer isChange) {
		this.is_change = isChange;
	}

	public Integer getTranslevel() {
		return translevel;
	}
	public void setTranslevel(Integer translevel) {
		this.translevel = translevel;
	}
	@Override
	public String toString() {
		return "Cashflow [id=" + id + ", change_id=" + change_id + ", pay_date=" + pay_date + ", start_date="
				+ start_date + ", coupon_rate=" + coupon_rate + ", amount=" + amount + ", is_change=" + is_change
				+ ", end_money=" + end_money + ", translevel=" + translevel + "]";
	}
	public Cashflow(String id, Integer change_id, String pay_date, String start_date, String coupon_rate, String amount,
			Integer is_change, String end_money, Integer translevel) {
		super();
		this.id = id;
		this.change_id = change_id;
		this.pay_date = pay_date;
		this.start_date = start_date;
		this.coupon_rate = coupon_rate;
		this.amount = amount;
		this.is_change = is_change;
		this.end_money = end_money;
		this.translevel = translevel;
	}
	public Cashflow() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
