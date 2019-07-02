package com.kayak.cloud.algorithm.logic;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kayak.cloud.algorithm.base.AlgorithmOrb;
import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;

/**
 * 该类用于买断式回购的算法：<br>
 * 1.买断式回购首期净价推到期净价,ORBTENetPrice<br>
 * 2.买断式回购到期净价推首期净价,ORETBNetPrice<br>
 * 3.买断式回购净价推回购收益率,ORNetPriceToYield
 *
 * @author cz
 *
 */
public class PriceAndOrb {

	/**
	 * 买断式回购首期净价推到期净价
	 *
	 * @param bondInfo
	 *            债券信息
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
	 * @throws ParseException
	 */
	public static BigDecimal ORBTENetPrice(BondInfo bondInfo, String settle_date, String beginORDate, String endORDate,
										   BigDecimal backCoup, BigDecimal beginNetPrice) throws ParseException {
		// 参数有效性检查
		initcheck(bondInfo, settle_date, beginORDate, endORDate);
		// 0
		BigDecimal zero = BigDecimal.ZERO;
		if (backCoup.compareTo(zero) <= 0 || beginNetPrice.compareTo(zero) <= 0) {
			throw new CALCException("回购利率或净价必须大于0！");
		}
		// 1、根据首期净价计算出首期全价；
		BigDecimal beginFullPrice = LogicPrice.NetPriceToFullPrice(beginNetPrice, settle_date, bondInfo);

		// 2、计算回购期间付息金额和还本金额；amounts[0]:付息总额，amounts[1]:还本总额
		BigDecimal[] amounts = countAmount(bondInfo.getCashflowList(), beginORDate, endORDate);

		// 3、计算到期全价：（首期全价*回购利率/100/365）*（回购到期日-回购首期日）+首期全价-回购期间付息金额-回购期间还本金额；
		BigDecimal endFullPrice = AlgorithmOrb.getEndFullPrice(beginFullPrice, backCoup, beginORDate, endORDate,
				amounts);

		// 4、根据到期全价推算到期净价
		return LogicPrice.FullPriceToNetPrice(endFullPrice, settle_date, bondInfo);
	}

	/**
	 * 买断式回购到期净价推首期净价
	 *
	 * @param bondInfo
	 *            债券信息
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
	 * @throws ParseException
	 */
	public static BigDecimal ORETBNetPrice(BondInfo bondInfo, String settle_date, String beginORDate, String endORDate,
										   BigDecimal backCoup, BigDecimal endNetPrice) throws ParseException {

		// 参数有效性检查
		initcheck(bondInfo, settle_date, beginORDate, endORDate);
		BigDecimal zero = BigDecimal.ZERO;
		if (backCoup.compareTo(zero) <= 0 || endNetPrice.compareTo(zero) <= 0) {
			throw new CALCException("回购利率或净价必须大于0！");
		}
		// 1、根据到期净价计算出到期全价；
		BigDecimal endFullPrice = LogicPrice.NetPriceToFullPrice(endNetPrice, settle_date, bondInfo);

		// 2、计算回购期间付息金额和还本金额；amounts[0]:付息总额，amounts[1]:还本总额
		BigDecimal[] amounts = countAmount(bondInfo.getCashflowList(), beginORDate, endORDate);

		// 3、计算首期全价：（到期全价+回购期间付息金额+回购期间还本金额）/（1+回购利率/100/365*（回购到期日-回购首期日））；
		BigDecimal beginFullPrice = AlgorithmOrb.getBeginFullPrice(backCoup, beginORDate, endORDate, endFullPrice,
				amounts);
		// 4、根据首期全价推算首期净价，并且要精确到小数点后4位；
		return LogicPrice.FullPriceToNetPrice(beginFullPrice, settle_date, bondInfo);
	}

	/**
	 * 买断式回购净价推回购收益率
	 *
	 * @param bondInfo
	 *            债券信息
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
	 * @throws ParseException
	 */
	public static BigDecimal ORNetPriceToYield(BondInfo bondInfo, String settle_date, String beginORDate,
											   String endORDate, BigDecimal endNetPrice, BigDecimal beginNetPrice)
			throws ParseException {
		// 参数有效性检查
		initcheck(bondInfo, settle_date, beginORDate, endORDate);
		BigDecimal zero = BigDecimal.ZERO;
		if (beginNetPrice.compareTo(zero) <= 0 || endNetPrice.compareTo(zero) <= 0) {
			throw new CALCException("净价必须大于0！");
		}
		// 1、根据到期净价计算出到期全价；
		BigDecimal endFullPrice = LogicPrice.NetPriceToFullPrice(endNetPrice, settle_date, bondInfo);

		// 2、计算回购期间付息金额和还本金额；amounts[0]:付息总额，amounts[1]:还本总额
		BigDecimal[] amounts = countAmount(bondInfo.getCashflowList(), beginORDate, endORDate);
		// 3、根据首期净价计算出首期全价；
		BigDecimal beginFullPrice = LogicPrice.NetPriceToFullPrice(beginNetPrice, settle_date, bondInfo);

		// 4、计算回购利率：（到期全价+回购期间付息金额+回购期间还本金额-首期全价）/（回购到期日-回购首期日）/首期全价*365*100，并且要精确到小数点后4位；
		return AlgorithmOrb.getBackCoup(beginORDate, endORDate, amounts, endFullPrice, beginFullPrice);
	}

	/**
	 * 计算回购期间付息金额和还本金额
	 *
	 * @param cashflows
	 *            资金流
	 * @param beginORDate
	 *            回购开始时间
	 * @param endORDate
	 *            回购到期时间
	 * @return
	 */
	private static BigDecimal[] countAmount(List<Cashflow> cashflows, String beginORDate, String endORDate) {
		BigDecimal[] ret = new BigDecimal[2];

		// 付息总额
		BigDecimal interest = DateUtils.ZERO;
		// 还本总额
		BigDecimal repay = DateUtils.ZERO;

		for (Cashflow t : cashflows) {
			String pay_date = t.getPay_date();// 发生日期
			// 发生日期在回购周期内
			if (pay_date.compareTo(beginORDate) > 0 && pay_date.compareTo(endORDate) <= 0) {
				int changeId = t.getChange_id();
				BigDecimal amount = new BigDecimal(t.getAmount());
				if (changeId == 10) {// 付息
					interest = interest.add(amount);
				} else {// 提前还本、到期还本
					repay = repay.add(amount);
				}
			}
		}
		ret[0] = interest;
		ret[1] = repay;
		return ret;
	}

	/**
	 * 参数有效性检查
	 *
	 * @param bondInfo
	 *            债券信息
	 * @param settle_date
	 *            结算日期
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 */
	public static void initcheck(BondInfo bondInfo, String settle_date, String beginORDate, String endORDate) {
		if (bondInfo == null) {
			throw new CALCException("获取不到债券信息");
		}
		if (bondInfo.getCashflowList().size() == 0) {
			throw new CALCException("获取不到现金流信息");
		}
		if (StringUtils.isEmpty(settle_date)) {
			throw new CALCException("【结算日期】不能为空");
		}
		if (StringUtils.isEmpty(beginORDate)) {
			throw new CALCException("【回购首期日】不能为空");
		}
		if (StringUtils.isEmpty(endORDate)) {
			throw new CALCException("【回购到期日】不能为空");
		}
		if (beginORDate.compareTo(endORDate) >= 0 || bondInfo.getBegin_date().compareTo(bondInfo.getEnd_date()) >= 0) {
			throw new CALCException("日期有误，开始时间大于或等于结束时间！");
		}
		if (beginORDate.compareTo(bondInfo.getBegin_date()) < 0 || endORDate.compareTo(bondInfo.getEnd_date()) > 0) {
			throw new CALCException("回购日期有误！");
		}

	}
}
