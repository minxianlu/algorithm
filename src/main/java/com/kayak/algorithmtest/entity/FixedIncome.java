package com.kayak.algorithmtest.entity;


import java.util.List;

/**
 * 固收类理财
 * @author pc_xjf
 *
 */
public abstract class FixedIncome {
	private String id;
	/**起息日*/
	private String begin_date;
	/**到期日*/
	private String end_date;
	/**首次付息日*/
	private String first_pay_date;
	/**付息频率*/
	private Integer pay_freq=4;
	/**收益率*/
	private String coupon_rate;
	/**计息基础 上一年的计息天数：1.为实际天数，2.为360天 ,3.365天,4.366天*/
	private Integer bond_baseday;
	/**币种*/
	private String trust_ccy;
	/**代码简称*/
	private String fi_name;
	/**代码*/
	private String fi_code;
	
	/**
	 * 现金流
	 */
	protected List<Cashflow> cashflowList;
	/**
	 * 提前还本信息
	 */
	protected List<RedempVO> bondredempvoList;
	/**
	 * 行权信息
	 */
	protected List<Exercise> bondexerciseList;
	/**
	 * 浮动利率表
	 */
	protected List<FloatRate> bondfloatrateList;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/**起息日*/
	public String getBegin_date() {
		return begin_date;
	}
	/**起息日*/
	public void setBegin_date(String begin_date) {
		this.begin_date = begin_date;
	}
	/**到期日*/
	public String getEnd_date() {
		return end_date;
	}
	/**到期日*/
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	/**首次付息日*/
	public String getFirst_pay_date() {
		return first_pay_date;
	}
	/**首次付息日*/
	public void setFirst_pay_date(String first_pay_date) {
		this.first_pay_date = first_pay_date;
	}
	/**付息频率*/
	public Integer getPay_freq() {
		return pay_freq;
	}
	/**付息频率*/
	public void setPay_freq(Integer pay_freq) {
		this.pay_freq = pay_freq;
	}
	/**收益率*/
	public String getCoupon_rate() {
		return coupon_rate;
	}
	/**收益率*/
	public void setCoupon_rate(String coupon_rate) {
		this.coupon_rate = coupon_rate;
	}
	/**计息基础 上一年的计息天数：1.为实际天数，2.为360天*/
	public Integer getBond_baseday() {
		return bond_baseday;
	}
	/**计息基础 上一年的计息天数：1.为实际天数，2.为360天*/
	public void setBond_baseday(Integer bond_baseday) {
		this.bond_baseday = bond_baseday;
	}
	/**币种*/
	public String getTrust_ccy() {
		return trust_ccy;
	}
	/**币种*/
	public void setTrust_ccy(String trust_ccy) {
		this.trust_ccy = trust_ccy;
	}
	/**代码简称*/
	public String getFi_name() {
		return fi_name;
	}
	/**代码简称*/
	public void setFi_name(String fi_name) {
		this.fi_name = fi_name;
	}
	/**代码*/
	public String getFi_code() {
		return fi_code;
	}
	/**代码*/
	public void setFi_code(String fi_code) {
		this.fi_code = fi_code;
	}
	/**
	 * 现金流
	 */	
	public List<Cashflow> getCashflowList() {
		return cashflowList;
	}
	/**
	 * 现金流
	 */
	public void setCashflowList(List<Cashflow> cashflowList) {
		this.cashflowList = cashflowList;
	}
	/**
	 * 提前还本信息
	 */
	public List<RedempVO> getBondredempvoList() {
		return bondredempvoList;
	}
	/**
	 * 提前还本信息
	 */
	public void setBondredempvoList(List<RedempVO> bondredempvoList) {
		this.bondredempvoList = bondredempvoList;
	}
	/**
	 * 行权信息
	 */
	public List<Exercise> getBondexerciseList() {
		return bondexerciseList;
	}
	/**
	 * 行权信息
	 */
	public void setBondexerciseList(List<Exercise> bondexerciseList) {
		this.bondexerciseList = bondexerciseList;
	}
	/**
	 * 浮动利率表
	 */
	public List<FloatRate> getBondfloatrateList() {
		return bondfloatrateList;
	}
	/**
	 * 浮动利率表
	 */
	public void setBondfloatrateList(List<FloatRate> bondfloatrateList) {
		this.bondfloatrateList = bondfloatrateList;
	}
	@Override
	public String toString() {
		return "FixedIncome [id=" + id + ", begin_date=" + begin_date + ", end_date=" + end_date + ", first_pay_date="
				+ first_pay_date + ", pay_freq=" + pay_freq + ", coupon_rate=" + coupon_rate + ", bond_baseday=" + bond_baseday
				+ ", trust_ccy=" + trust_ccy + ", fi_name=" + fi_name + ", fi_code=" + fi_code + ", cashflowList="
				+ cashflowList + ", bondredempvoList=" + bondredempvoList + ", bondexerciseList=" + bondexerciseList
				+ ", bondfloatrateList=" + bondfloatrateList + "]";
	}
	public FixedIncome(String id, String begin_date, String end_date, String first_pay_date, Integer pay_freq,
                       String coupon_rate, Integer bond_baseday, String trust_ccy, String fi_name, String fi_code,
                       List<Cashflow> cashflowList, List<RedempVO> bondredempvoList, List<Exercise> bondexerciseList,
                       List<FloatRate> bondfloatrateList) {
		super();
		this.id = id;
		this.begin_date = begin_date;
		this.end_date = end_date;
		this.first_pay_date = first_pay_date;
		this.pay_freq = pay_freq;
		this.coupon_rate = coupon_rate;
		this.bond_baseday = bond_baseday;
		this.trust_ccy = trust_ccy;
		this.fi_name = fi_name;
		this.fi_code = fi_code;
		this.cashflowList = cashflowList;
		this.bondredempvoList = bondredempvoList;
		this.bondexerciseList = bondexerciseList;
		this.bondfloatrateList = bondfloatrateList;
	}
	public FixedIncome() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	
}
