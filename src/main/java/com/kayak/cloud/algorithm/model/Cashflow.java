package com.kayak.cloud.algorithm.model;

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
	 * 每百元利息/利率
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
	
	/**
	 * 剩余本金
	 */
	public String getEnd_money() {
		return end_money;
	}
	/**
	 * 剩余本金
	 */
	public void setEnd_money(String endMoney) {
		end_money = endMoney;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 发生类型1——起息、10——付息、12——提前还本、11——到期还本
	 */
	public Integer getChange_id() {
		return change_id;
	}
	/**
	 * 发生类型1——起息、10——付息、12——提前还本、11——到期还本
	 */
	public void setChange_id(Integer changeId) {
		this.change_id = changeId;
	}
	/**
	 * 付息日
	 */
	public String getPay_date() {
		return pay_date;
	}
	/**
	 * 付息日
	 */
	public void setPay_date(String payDate) {
		this.pay_date = payDate;
	}
	/**
	 * 开始日期
	 */
	public String getStart_date() {
		return start_date;
	}
	/**
	 * 开始日期
	 */
	public void setStart_date(String startDate) {
		this.start_date = startDate;
	}
	/**
	 * 每百元利息
	 */
	public String getCoupon_rate() {
		return coupon_rate;
	}
	/**
	 * 每百元利息
	 */
	public void setCoupon_rate(String couponRate) {
		this.coupon_rate = couponRate;
	}
	/**
	 * 单位资金流
	 */
	public String getAmount() {
		return amount;
	}
	/**
	 * 单位资金流
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}
	/**
	 * 是否发生变化
	 */
	public Integer getIs_change() {
		return is_change;
	}
	/**
	 * 是否发生变化
	 */
	public void setIs_change(Integer isChange) {
		this.is_change = isChange;
	}
	/**
	 * 变动类型优先级
	 */
	public Integer getTranslevel() {
		return translevel;
	}
	/**
	 * 变动类型优先级
	 */
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
