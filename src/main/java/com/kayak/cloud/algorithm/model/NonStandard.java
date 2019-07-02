package com.kayak.cloud.algorithm.model;
/**
 * 非标
 * @author pc_xjf
 */
public class NonStandard extends FixedIncome{
	//**ID*/
	private Integer id;
	/**非标识别号=市场代码+非标代码*/
	private String bond_id;
	/**市场代码*/
	private String market_code;
	/**非标代码*/
	private String bond_code;
	/**非标简称*/
	private String bond_name;
	/**非标全称*/
	private String full_name;
	/**非标属性*/
	private Integer bond_prop=1;
	/**货币代码*/
	private String cur="CN";

}
