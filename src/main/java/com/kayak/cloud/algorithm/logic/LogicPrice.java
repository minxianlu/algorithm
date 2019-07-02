package com.kayak.cloud.algorithm.logic;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;

public class LogicPrice {
	/**
	 * 净价推全价
	 *
	 * @param netPrice
	 *            净价
	 * @param settle_date
	 *            结算日期
	 * @param bondInfo
	 *            债券信息
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal NetPriceToFullPrice(BigDecimal netPrice, String settle_date, BondInfo bondInfo)
			throws ParseException {
		// 参数有效性检查
		LogicPrice.initcheck(bondInfo, settle_date);
		// 净价推全价
		BigDecimal fullprice = LogicPrice.NetPriceToFullPrice(netPrice, bondInfo.getInterest_type(),
				bondInfo.getPay_freq(), new BigDecimal(bondInfo.getIssue_price()), bondInfo.getCashflowList(),
				bondInfo.getBegin_date(), settle_date, bondInfo.getEnd_date(), bondInfo.getBond_baseday(),
				bondInfo.getPay_date(), bondInfo.getInterest_mode(), bondInfo.getCoupon_rate(),
				bondInfo.getInterest_sort(), bondInfo.getMarket_code(), bondInfo.getIs_abs(),bondInfo.getFirst_pay_date());
		return fullprice;

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
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal FullPriceToNetPrice(BigDecimal fullPrice, String settle_date, BondInfo bondInfo)
			throws ParseException {
		// 参数有效性检查
		LogicPrice.initcheck(bondInfo, settle_date);
		// 全价推净价
		BigDecimal netprice = LogicPrice.FullPriceToNetPrice(fullPrice, bondInfo.getInterest_type(),
				bondInfo.getPay_freq(), new BigDecimal(bondInfo.getIssue_price()), bondInfo.getCashflowList(),
				bondInfo.getBegin_date(), settle_date, bondInfo.getEnd_date(), bondInfo.getBond_baseday(),
				bondInfo.getPay_date(), bondInfo.getInterest_mode(), bondInfo.getCoupon_rate(),
				bondInfo.getInterest_sort(), bondInfo.getMarket_code(), bondInfo.getIs_abs(),bondInfo.getFirst_pay_date());
		return netprice;

	}

	/**
	 * 净价推全价
	 *
	 * @param netPrice
	 * @param interest_type
	 *            息票品种
	 * @param pay_freq
	 *            付息频率
	 * @param publish_price
	 *            发行价格
	 * @param cashflows
	 *            单位资金流
	 * @param begin_date
	 *            起息日
	 * @param settle_date
	 *            结算日，即输入的结算日期
	 * @param end_date
	 *            到期日
	 * @param bond_baseday
	 *            计息基础
	 * @param pay_date
	 *            (银行间)实际兑付日
	 * @param interest_mode
	 *            计息模式
	 * @param coupon_rate
	 *            利率
	 * @param interest_sort
	 *            附息利率品种
	 * @param market_code
	 *            市场代码
	 * @param is_abs
	 *            是否為ABS 1是
	 * @param first_date
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal NetPriceToFullPrice(BigDecimal netPrice, Integer interest_type, Integer pay_freq,
												 BigDecimal publish_price, List<Cashflow> cashflows, String begin_date, String settle_date, String end_date,
												 int bond_baseday, String pay_date, Integer interest_mode, String coupon_rate, Integer interest_sort,
												 String market_code, String is_abs, String first_date) throws ParseException {
		// 检查参数有效性
		if (netPrice.compareTo(new BigDecimal(200)) > 0 || netPrice.compareTo(DateUtils.ZERO) <= 0) {
			throw new CALCException("净价不能小于0或大于200！");
		}
		settle_date = settle_date.replaceAll("\\-", "");
		// 调用应计利息算法，得到应计利息
		BigDecimal AI = LogicAI.GetAI(interest_type, pay_freq, pay_date, publish_price, cashflows, interest_mode,
				begin_date, settle_date, end_date, coupon_rate, interest_sort, bond_baseday, market_code, is_abs,first_date);
		// 全价=净价+应计利息
		BigDecimal b = AI.add(netPrice);
		if (b.compareTo(DateUtils.ZERO) < 0) {
			throw new CALCException("净价输入有误，全价不得小于0");
		}
		// 201606
		return b.setScale(DateUtils.SCALE, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 全价推净价
	 *
	 * @param fullPrice
	 *            百元全价
	 * @param interest_type
	 *            息票品种
	 * @param pay_freq
	 *            付息频率
	 * @param publish_price
	 *            发行价格
	 * @param cashflows
	 *            单位资金流
	 * @param begin_date
	 *            起息日
	 * @param settle_date
	 *            结算日，即输入的结算日期
	 * @param end_date
	 *            到期日
	 * @param bond_baseday
	 *            计息基础
	 * @param pay_date
	 *            (银行间)实际兑付日
	 * @param interest_mode
	 *            计息模式
	 * @param coupon_rate
	 *            利率
	 * @param interest_sort
	 *            附息利率品种
	 * @param market_code
	 *            市场代码
	 * @param is_abs
	 *            是否為ABS 1是
	 * @param first_date
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal FullPriceToNetPrice(BigDecimal fullPrice, Integer interest_type, Integer pay_freq,
												 BigDecimal publish_price, List<Cashflow> cashflows, String begin_date, String settle_date, String end_date,
												 int bond_baseday, String pay_date, Integer interest_mode, String coupon_rate, Integer interest_sort,
												 String market_code, String is_abs, String first_date) throws ParseException {

		// 检查参数有效性
		if (fullPrice.compareTo(new BigDecimal("200")) > 0 || fullPrice.compareTo(new BigDecimal("0")) <= 0) {
			throw new CALCException("全价不能小于0或大于200");
		}
		settle_date = settle_date.replaceAll("\\-", "");

		// 调用应计利息算法得到应计利息
		BigDecimal AI = LogicAI.GetAI(interest_type, pay_freq, pay_date, publish_price, cashflows, interest_mode,
				begin_date, settle_date, end_date, coupon_rate, interest_sort, bond_baseday, market_code, is_abs,first_date);
		// 净价=全价-应计利息
		BigDecimal b = fullPrice.subtract(AI);

		if (b.compareTo(DateUtils.ZERO) < 0) {
			throw new CALCException("全价输入有误，净价不得小于0");
		}
		return b.setScale(DateUtils.SCALE, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 参数 约束 检查 数字属性 已设置默认值 无需非空检查
	 *
	 * @param settle_date
	 *            结算日期
	 * @param bond
	 *            债券信息
	 */
	public static void initcheck(BondInfo bond, String settle_date) {
		if (bond == null) {
			throw new CALCException("获取不到债券信息");
		}
		if (StringUtils.isBlank(bond.getBegin_date())) {
			throw new CALCException("【起息日】不能为空");
		}
		if (StringUtils.isBlank(settle_date)) {
			throw new CALCException("【结算日期】不能为空");
		}
		if (StringUtils.isBlank(bond.getCoupon_rate())) {
			throw new CALCException("【利率】不能为空");
		}
		if (StringUtils.isBlank(bond.getIs_abs())) {
			throw new CALCException("【是否为ABS债】不能为空");
		}
		if (!"1".equals(bond.getIs_abs())) {
			if (bond.getInterest_type() == 3) {
				if (StringUtils.isBlank(bond.getIssue_price())) {
					throw new CALCException("【发行价格】不能为空");
				}
			}
			if (StringUtils.isBlank(bond.getEnd_date())) {
				throw new CALCException("【到期日】不能为空");
			}
			if (StringUtils.isBlank(bond.getMarket_code())) {
				throw new CALCException("【市场代码】不能为空");
			}
		}
		if (bond.getBegin_date().compareTo(bond.getEnd_date()) >= 0) {
			throw new CALCException("日期有误，起息日不能大于等于到期日！");
		}
	}

}
