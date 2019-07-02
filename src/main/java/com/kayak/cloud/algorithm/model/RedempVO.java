package com.kayak.cloud.algorithm.model;

import java.math.BigDecimal;

/**
 * 提前还本
 * @author pc_xjf
 *
 */
public class RedempVO {
	public String id;
	/**债券识别号*/
	private String bond_id ;
	/**提前还本日期*/
	public String exerciseDate;
	/**单位提前还本金额*/
	public BigDecimal unitPrincipal;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	/**提前还本日期*/
	public String getExerciseDate() {
		return exerciseDate;
	}
	/**提前还本日期*/
	public void setExerciseDate(String exerciseDate) {
		this.exerciseDate = exerciseDate;
	}
	/**单位提前还本金额*/
	public BigDecimal getUnitPrincipal() {
		return unitPrincipal;
	}
	/**单位提前还本金额*/
	public void setUnitPrincipal(BigDecimal unitPrincipal) {
		this.unitPrincipal = unitPrincipal;
	}
	public String getBond_id() {
		return bond_id;
	}
	public void setBond_id(String bond_id) {
		this.bond_id = bond_id;
	}

	@Override
	public String toString() {
		return "RedempVO [id=" + id + ", bond_id=" + bond_id + ", exerciseDate=" + exerciseDate + ", unitPrincipal="
				+ unitPrincipal + "]";
	}

	public RedempVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RedempVO(String id, String bond_id, String exerciseDate, BigDecimal unitPrincipal) {
		super();
		this.id = id;
		this.bond_id = bond_id;
		this.exerciseDate = exerciseDate;
		this.unitPrincipal = unitPrincipal;
	}

}
