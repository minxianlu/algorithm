package com.kayak.algorithmtest.entity;
/**
 * 行权信息
 * @author pc_xjf
 *
 */
public class Exercise {
	private String id 		;//   ID 
	/**债券识别号*/
	private String bond_id          ;//   债券识别号
	/**行权日期*/
	private String exercise_date    ;//   行权日期  
	/**票面利率*/
	private String coupon_rate      ;//   票面利率  
	/**录入柜员*/
	private String inputuser        ;//   录入柜员
	/**票面利率是否确定 -- 0表示利率未确定 1 确定*/
	private Integer is_rate      ;//   票面利率  
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/**债券识别号*/
	public String getBond_id() {
		return bond_id;
	}
	/**债券识别号*/
	public void setBond_id(String bond_id) {
		this.bond_id = bond_id;
	}
	/**行权日期*/
	public String getExercise_date() {
		return exercise_date;
	}
	/**行权日期*/
	public void setExercise_date(String exercise_date) {
		this.exercise_date = exercise_date;
	}
	/**票面利率*/
	public String getCoupon_rate() {
		return coupon_rate;
	}
	/**票面利率*/
	public void setCoupon_rate(String coupon_rate) {
		this.coupon_rate = coupon_rate;
	}
	/**录入柜员*/
	public String getInputuser() {
		return inputuser;
	}
	/**录入柜员*/
	public void setInputuser(String inputuser) {
		this.inputuser = inputuser;
	}
	/**票面利率是否确定 -- 0表示利率未确定 1 确定*/
	public Integer getIs_rate() {
		return is_rate;
	}
	/**票面利率是否确定 -- 0表示利率未确定 1 确定*/
	public void setIs_rate(Integer is_rate) {
		this.is_rate = is_rate;
	}
	public Exercise(String id, String bond_id, String exercise_date,
			String coupon_rate, String inputuser,Integer is_rate) {
		super();
		this.id = id;
		this.bond_id = bond_id;
		this.exercise_date = exercise_date;
		this.coupon_rate = coupon_rate;
		this.inputuser = inputuser;
		this.is_rate = is_rate;
	}
	public Exercise() {
		super();
	}

}
