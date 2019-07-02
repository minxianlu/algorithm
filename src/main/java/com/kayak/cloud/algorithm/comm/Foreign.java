package com.kayak.cloud.algorithm.comm;

import java.lang.Exception;
import java.math.BigDecimal;
import java.util.List;

import com.kayak.cloud.algorithm.logic.*;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.FixedIncome;

public class Foreign {
	/**
	 * 资产久期
	 *
	 * @param settleDate
	 *            结算日期
	 * @param isDoExercise
	 *            是否行权
	 * @param netPrice
	 *            全价
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetDuration(String settleDate, int isDoExercise, BigDecimal netPrice,
										   BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal duration;
		try {
			duration = Duration.getDuration(settleDate, isDoExercise, netPrice, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetDuration");
		}
		return calcResponse.calcResponseSuccess(duration, "GetDuration");
	}

	/**
	 * 修正久期
	 *
	 * @param settleDate
	 *            结算日期
	 * @param isDoExercise
	 *            是否行权
	 * @param netPrice
	 *            全价
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetModDuration(BigDecimal netPrice, String settleDate, int isDoExercise,
											  BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal duration;
		try {
			duration = Duration.getMODDuration(netPrice, settleDate, isDoExercise, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetModDuration");
		}
		return calcResponse.calcResponseSuccess(duration, "GetModDuration");
	}

	/**
	 * 资产凸性
	 *
	 * @param settleDate
	 *            结算日期
	 * @param isDoExercise
	 *            是否行权
	 * @param netPrice
	 *            全价
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetConvexity(BigDecimal netPrice, String settleDate, int isDoExercise,
											BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal duration;
		try {
			duration = Duration.getConvexity(netPrice, settleDate, isDoExercise, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetConvexity");
		}
		return calcResponse.calcResponseSuccess(duration, "GetConvexity");
	}

	/**
	 * pvbp
	 *
	 * @param settleDate
	 *            结算日期
	 * @param isDoExercise
	 *            是否行权
	 * @param netPrice
	 *            全价
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetPvbp(BigDecimal netPrice, String settleDate, int isDoExercise,
									   BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal duration;
		try {
			duration = Duration.getPvbp(netPrice, settleDate, isDoExercise, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetPvbp");
		}
		return calcResponse.calcResponseSuccess(duration, "GetPvbp");
	}

	/**
	 * TODO 净价推实际利率（%）
	 */
	public static CALCResponse GetRealRateByNetPrice(FixedIncome FI, Integer i) {
		CALCResponse calcResponse = new CALCResponse();
		return calcResponse.calcResponseSuccess(new BigDecimal(3.3), "GetAI");
	}

	/**
	 * TODO 实际利率（%）推净价
	 */
	public static CALCResponse GetNetPriceByRealRate(FixedIncome FI, Integer i) {
		CALCResponse calcResponse = new CALCResponse();
		return calcResponse.calcResponseSuccess(new BigDecimal(3.3), "GetAI");
	}

	/**
	 * 净价推全价
	 *
	 * @param netPrice
	 *            净价
	 * @param settle_date
	 *            结算日期
	 * @param bondInfo
	 *            债券信息
	 *          interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 		publish_price 发行价格  cashflows 单位资金流  interest_mode 计息模式
	 * 	 		begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 		coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 		market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 */
	public static CALCResponse GetFullPriceByRealRate(BigDecimal netPrice, String settle_date,
													  BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal fullPrice;
		try {
			fullPrice = LogicPrice.NetPriceToFullPrice(netPrice, settle_date, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetRealRateByFullPrice");
		}
		return calcResponse.calcResponseSuccess(fullPrice, "GetRealRateByFullPrice");
	}

	/**
	 * 全价推净价
	 *
	 * @param settle_date
	 *            结算日期
	 * @param fullPrice
	 *            全价
	 * @param bondInfo
	 *            债券信息
	 *          interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 		publish_price 发行价格  List<Cashflow> 单位资金流  interest_mode 计息模式
	 * 	 		begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 		coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 		market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 * @return 净价
	 */
	public static CALCResponse GetRealRateByFullPrice(BigDecimal fullPrice, String settle_date,
													  BondInfo bondInfo) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal netprice;
		try {
			netprice = LogicPrice.FullPriceToNetPrice(fullPrice, settle_date, bondInfo);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetRealRateByFullPrice");
		}
		return calcResponse.calcResponseSuccess(netprice, "GetRealRateByFullPrice");
	}

	/**
	 * 计算应计利息
	 *
	 * @param FI
	 * 	 interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 publish_price 发行价格  List<Cashflow> 单位资金流  interest_mode 计息模式
	 * 	 begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 * @param settle_date
	 *            结算日期
	 */
	public static CALCResponse GetAI(BondInfo FI, String settle_date) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal AI;
		try {
			AI = LogicAI.GetAI(FI.getInterest_type(), FI.getPay_freq(), FI.getPay_date(),
					new BigDecimal(FI.getIssue_price()), FI.getCashflowList(), FI.getInterest_mode(),
					FI.getBegin_date(), settle_date, FI.getEnd_date(), FI.getCoupon_rate(), FI.getInterest_sort(),
					FI.getBond_baseday(), FI.getMarket_code(), FI.getIs_abs(),FI.getFirst_pay_date());
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetAI");
		}
		return calcResponse.calcResponseSuccess(AI, "GetAI");
	}

//	public static CALCResponse GetAI2(BondInfo FI, String settle_date) {
//		CALCResponse calcResponse = new CALCResponse();
//		BigDecimal AI;
//		try {
//			AI = Price.GetAI(FI,settle_date);
//		} catch (Exception e) {
//			return calcResponse.calcResponseError(e.getMessage(), "GetAI");
//		}
//		return calcResponse.calcResponseSuccess(AI, "GetAI");
//	}

	/**
	 * 买断式回购首期净价推到期净价
	 *
	 * @param bondInfo
	 *            债券信息
	 *          interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 		publish_price 发行价格  List<Cashflow> 单位资金流  interest_mode 计息模式
	 * 	 		begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 		coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 		market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 * @param settle_date
	 *            结算日期
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 * @param backCoup
	 *            回购利率
	 * @param beginNetPrice
	 *            首期净价
	 * @return
	 */
	public static CALCResponse GetORNetPriceEByB(BondInfo bondInfo, String settle_date, String beginORDate,
												 String endORDate, BigDecimal backCoup, BigDecimal beginNetPrice) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal NetPrice;
		try {
			NetPrice = PriceAndOrb.ORBTENetPrice(bondInfo, settle_date, beginORDate, endORDate, backCoup, beginNetPrice);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetORNetPriceEByB");
		}
		return calcResponse.calcResponseSuccess(NetPrice, "GetORNetPriceEByB");
	}

	/**
	 * 买断式回购到期净价推首期净价
	 *
	 * @param bondInfo
	 *            债券信息
	 *          interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 		publish_price 发行价格  List<Cashflow> 单位资金流  interest_mode 计息模式
	 * 	 		begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 		coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 		market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 * @param settle_date
	 *            结算日期
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 * @param backCoup
	 *            回购利率
	 * @param endNetPrice
	 *            到期净价
	 * @return
	 */
	public static CALCResponse GetORNetPriceBByE(BondInfo bondInfo, String settle_date, String beginORDate,
												 String endORDate, BigDecimal backCoup, BigDecimal endNetPrice) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal NetPrice;
		try {
			NetPrice = PriceAndOrb.ORETBNetPrice(bondInfo, settle_date, beginORDate, endORDate, backCoup, endNetPrice);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetORNetPriceBByE");
		}
		return calcResponse.calcResponseSuccess(NetPrice, "GetORNetPriceBByE");
	}

	/**
	 * 买断式回购净价推回购收益率
	 *
	 * @param bondInfo
	 *            债券信息
	 *          interest_type 息票品种  pay_freq 付息频率  pay_date (银行间)实际兑付日
	 * 	 		publish_price 发行价格  List<Cashflow> 单位资金流  interest_mode 计息模式
	 * 	 		begin_date 起息日  settle_date 结算日，即输入的结算日期  end_date 到期日
	 * 	 		coupon_rate 收益率  interest_sort 附息利率品种  bond_baseday 计息基础
	 * 	 		market_code 市场代码  is_abs 是否为ABS债  first_date 首次付息日
	 * @param settle_date
	 *            结算日期
	 * @param beginORDate
	 *            回购起息日
	 * @param endORDate
	 *            回购到期日
	 * @param endNetPrice
	 *            回购到期净价
	 * @param beginNetPrice
	 *            回购首期净价
	 * @return
	 */
	public static CALCResponse GetYieldByORNetPrice(BondInfo bondInfo, String settle_date, String beginORDate,
													String endORDate, BigDecimal endNetPrice, BigDecimal beginNetPrice) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal NetPrice;
		try {
			NetPrice = PriceAndOrb.ORNetPriceToYield(bondInfo, settle_date, beginORDate, endORDate, endNetPrice,
					beginNetPrice);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetYieldByORNetPrice");
		}
		return calcResponse.calcResponseSuccess(NetPrice, "GetYieldByORNetPrice");
	}

	/**
	 * 质押式回购应计利息计算
	 *
	 * @param first_date
	 *            首期日期
	 * @param next_date
	 *            到期日期
	 * @param rate
	 *            回购利率
	 * @param price
	 *            回购金额，以元为单位
	 */
	public static CALCResponse GetCRAI(String first_date, String next_date, BigDecimal rate, BigDecimal price) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal AI;
		try {
			AI = LogicAI.GetCRAI(first_date, next_date, rate, price);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetCRAI");
		}
		return calcResponse.calcResponseSuccess(AI, "GetCRAI");
	}

	/**
	 * 全价推收益率算法
	 * 1.对处于最后付息周期的固定利率债券、待偿期在一年及以内的到期一次还本付息零息债和贴现债券，到期收益率按单利计算
	 * 2.对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算
	 * 3.对不处于最后付息周期的固定利率债券，到期收益率按复利计算
	 * 4.对不处于最后周期的浮息债，到期收益率的计算
	 * 5.对不处于最后周期的提前还本固定利率债券，到期收益率的计算
	 *
	 * @param fullPrice
	 *            全价
	 * @param settleDate
	 *            结算日期
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetYTMByFullPrice(BondInfo bondInfo,BigDecimal fullPrice,String settleDate) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal ytm;
		try {
			ytm = PriceAndYtm.getYTMByFullPrice(bondInfo,fullPrice,settleDate);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetYTMByFullPrice");
		}
		return calcResponse.calcResponseSuccess(ytm, "GetYTMByFullPrice");
	}

	/**
	 * 收益率（%）推全价
	 * 1.对处于最后付息周期的固定利率债券、待偿期在一年及以内的零息债和贴现债券、处于最后付息周期的浮息债，到期收益率按单利计算
	 * 2.对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算。
	 * 3.对不处于最后付息周期的固定利率债券，到期收益率按复利计算
	 * 4.对不处于最后付息周期的浮息债，到期收益率按复利计算
	 *
	 * @param settleDate
	 *            结算日期
	 * @param ytm
	 *            收益率
	 * @param bondInfo
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetFullPriceByYTM(BondInfo bondInfo,BigDecimal ytm,String settleDate) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal fullPrice;
		try {
			fullPrice = PriceAndYtm.getFullPriceByYTM(bondInfo,ytm,settleDate);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "getFullPriceByYTM");
		}
		return calcResponse.calcResponseSuccess(fullPrice, "getFullPriceByYTM");
	}

	/**
	 * 净价推收益率（%）
	 *
	 * @param settleDate
	 *            结算日期
	 * @param netPrice
	 *            净价
	 * @param bondInfo
	 *            债券信息
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetYTMByRealPrice(BondInfo bondInfo,BigDecimal netPrice,String settleDate) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal ytm;
		try {
			ytm = PriceAndYtm.getYTMByRealPrice(bondInfo,netPrice,settleDate);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "getYTMByRealPrice");
		}
		return calcResponse.calcResponseSuccess(ytm, "getYTMByRealPrice");
	}

	/**
	 * 收益率（%）推净价
	 *
	 * @param settleDate
	 *            结算日期
	 * @param ytm
	 *            收益率
	 * @param bondInfo
	 *            债券信息
	 *            List<CashFlows> 现金流 List<Exercise>  行权信息
	 *            begin_date	  起息日 end_date        到期兑付日 pay_freq    付息频率
	 *            is_exercise     是否含权：1--含权，0--不含权
	 *            interest_type   债券的品种：1--附加利息，2--零息，3--贴现
	 *            interest_sort   付息利率品种：1--浮动利率，2--固定利率
	 *            interestMode    计息模式：1--分段计息，2--逐日计息
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetRealPriceByYTM(BondInfo bondInfo,BigDecimal ytm,String settleDate) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal netPrice;
		try {
			netPrice = PriceAndYtm.getRealPriceByYTM(bondInfo,ytm,settleDate);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "getRealPriceByYTM");
		}
		return calcResponse.calcResponseSuccess(netPrice, "getRealPriceByYTM");
	}

	/**
	 * 针对类似于债券的产品，生成资金流list
	 * @param bond 类似于债券的产品信息
	 * begin_date 起息日  first_pay_date 首次付息日
	 * end_date 到期日 pay_freq 付息频率 coupon_rate 利率
	 * List<RedempVO> 提前还本信息 List<Exercise> 行权信息
	 * List<FloatRate> 浮动利率信息 interest_sort 附息利率品种:1——浮动利率 2——固定利率 3——累进利率
	 * bond_spread 利差 is_exercise 是否行权 data_list 自定义付息日
	 * List<Cashflow> 现金流 interest_type 付息品种，2--零息，1--附加利息，3--贴现 repayment_type 还本方式
	 * return
	 */
	public static CALCResponse GetBondLikeCashflow(BondInfo bond) {
		CALCResponse calcResponse = new CALCResponse();
		List<Cashflow> cflist;
		try {
			cflist = LogicCashFlow.getCashFlowList(bond, 0);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetBondLikeCashflow");
		}
		return calcResponse.calcResponseSuccess(cflist, "GetBondLikeCashflow");
	}

	/**
	 * 债券生成资金流水
	 * @param bond 债券信息
	 * begin_date 起息日  first_pay_date 首次付息日
	 * end_date 到期日 pay_freq 付息频率 coupon_rate 利率
	 * List<RedempVO> 提前还本信息 List<Exercise> 行权信息
	 * List<FloatRate> 浮动利率信息 interest_sort 附息利率品种:1——浮动利率 2——固定利率 3——累进利率
	 * bond_spread 利差 is_exercise 是否行权 data_list 自定义付息日
	 * List<Cashflow> 现金流 interest_type 付息品种，2--零息，1--附加利息，3--贴现 repayment_type
	 * @return
	 */
	public static CALCResponse GetBondCashflow(BondInfo bond) {
		CALCResponse calcResponse = new CALCResponse();
		List<Cashflow> cflist;
		try {
			cflist = LogicCashFlow.getCashFlowList(bond, 1);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "GetBondCashflow");
		}
		return calcResponse.calcResponseSuccess(cflist, "GetBondCashflow");
	}

	/**
	 * 获取单位本金
	 *
	 * @param bondInfo
	 *            债券信息 List<Cashflow> 现金流 beginDate 起息日 endDate 到期日
	 * @param settleDate
	 *            起息日
	 * @return CALCResponse 计算响应类 status 响应状态 algorithmName 算法名称 types 计算类型
	 * 						CALCResult 计算结果类(calcLog,bdlResult)
	 */
	public static CALCResponse GetUnitPrinciple(BondInfo bondInfo,String settleDate) {
		CALCResponse calcResponse = new CALCResponse();
		BigDecimal endMoney;
		try {
			endMoney = UnitPrincipal.getUnitPrinciple(bondInfo,settleDate);
		} catch (Exception e) {
			return calcResponse.calcResponseError(e.getMessage(), "getUnitPrinciple");
		}
		return calcResponse.calcResponseSuccess(endMoney, "getUnitPrinciple");
	}

}
