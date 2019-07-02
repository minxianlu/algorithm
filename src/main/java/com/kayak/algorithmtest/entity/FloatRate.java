package com.kayak.algorithmtest.entity;

import java.math.BigDecimal;

/**
 * 浮动利率
 * @author pc_xjf
 *
 */
public class FloatRate {
	private String id 		;//   ID 
	/**起息日*/
	private String begin_date;
	/**到期日*/
	private String end_date; 
	/**基础利率*/
	private BigDecimal base_rate;
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
	/**基础利率*/
	public BigDecimal getBase_rate() {
		return base_rate;
	}
	/**基础利率*/
	public void setBase_rate(BigDecimal base_rate) {
		this.base_rate = base_rate;
	}
	public FloatRate(String id, String begin_date,
			String end_date, BigDecimal base_rate) {
		super();
		this.id = id;
		this.begin_date = begin_date;
		this.end_date = end_date;
		this.base_rate = base_rate;
	}
	public FloatRate() {
		super();
	}

}
