package com.kayak.cloud.algorithm.base;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

import com.kayak.cloud.algorithm.comm.DateUtils;

/**
 * 银行间，交易所债券应计利息计算方式
 * 
 * @author pc_xjf
 *
 */
public class AlgorithmOrb {
	/**
	 * 到期全价：（首期全价*回购利率/100/365）*（回购到期日-回购首期日）+首期全价-回购期间付息金额-回购期间还本金额
	 * 
	 * @param beginFullPrice
	 *            首期全价
	 * @param backCoup
	 *            回购利率
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 * @param amounts
	 *            [回购期间付息金额,回购期间还本金额]
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal getEndFullPrice(BigDecimal beginFullPrice, BigDecimal backCoup, String beginORDate,
			String endORDate, BigDecimal[] amounts) throws ParseException {
		BigDecimal endFullPrice = (beginFullPrice
				.multiply(backCoup.divide(BigDecimal.valueOf(36500), DateUtils.SCALE, RoundingMode.HALF_UP))
				.multiply(new BigDecimal(DateUtils.DateApart(beginORDate, endORDate)))).add(beginFullPrice)
						.subtract(amounts[0]).subtract(amounts[1]);
		return endFullPrice;
	}

	/**
	 * 首期全价：（到期全价+回购期间付息金额+回购期间还本金额）/（1+回购利率/100/365*（回购到期日-回购首期日））
	 * 
	 * @param backCoup
	 *            回购利率
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 * @param endFullPrice
	 *            到期全价
	 * @param amounts
	 *            [回购期间付息金额,回购期间还本金额]
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal getBeginFullPrice(BigDecimal backCoup, String beginORDate, String endORDate,
			BigDecimal endFullPrice, BigDecimal[] amounts) throws ParseException {
		BigDecimal divisor = backCoup.multiply(BigDecimal.valueOf(DateUtils.DateApart(beginORDate, endORDate)))
				.divide(new BigDecimal("36500"), DateUtils.SCALE, RoundingMode.HALF_UP).add(BigDecimal.valueOf(1));
		BigDecimal beginFullPrice = endFullPrice.add(amounts[0]).add(amounts[1]).divide(divisor, DateUtils.SCALE,
				BigDecimal.ROUND_HALF_UP);
		return beginFullPrice;
	}

	/**
	 * 回购利率：（到期全价+回购期间付息金额+回购期间还本金额-首期全价）/（回购到期日-回购首期日）/首期全价*365*100
	 * 
	 * @param beginORDate
	 *            回购首期日
	 * @param endORDate
	 *            回购到期日
	 * @param endFullPrice
	 *            到期全价
	 * @param amounts
	 *            [回购期间付息金额,回购期间还本金额]
	 * @param beginFullPrice
	 *            首期全价
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal getBackCoup(String beginORDate, String endORDate,
			BigDecimal[] amounts, BigDecimal endFullPrice, BigDecimal beginFullPrice) throws ParseException {
		BigDecimal count1 = endFullPrice.add(amounts[0].add(amounts[1])).subtract(beginFullPrice);
		BigDecimal count2 = BigDecimal.valueOf(DateUtils.DateApart(beginORDate, endORDate));
		return count1.divide(count2, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP)
				.divide(beginFullPrice, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("36500"));
	}
}
