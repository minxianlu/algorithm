package com.kayak.cloud.algorithm.base;

import java.math.BigDecimal;

import com.kayak.cloud.algorithm.comm.DateUtils;

/**
 * 银行间，交易所债券应计利息计算方式
 * 
 * @author pc_xjf
 *
 */
public class AlgorithmAI {

	/**
	 * 银行间 附息债 付息周期等于一年 计算公式：c/ts*t
	 * 
	 * @param c
	 *            票面利率
	 * @param t
	 *            上一次付息日至结算日之间的实际天数,含2月29日，算头算尾
	 * @param ts
	 *            上一次付息日至付息日之间的实际天数
	 * @return 应计利息
	 */
	public static BigDecimal getAIBankCouponOnYear(BigDecimal c, BigDecimal t, BigDecimal ts) {
		BigDecimal ai = c.divide(ts, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP).multiply(t);
		return ai;
	}

	/**
	 * 银行间 附息债 付息周期小于一年 按实际天数付息 计算公式：c/ty*t
	 * 
	 * @param c
	 *            票面利率
	 * @param t
	 *            上一次付息日至结算日之间的实际天数,含2月29日，算头算尾
	 * @param ty
	 *            本付息周期所在计息年度的实际天数,含2月29日，算头算尾
	 * @return 应计利息
	 */
	public static BigDecimal getAIBankCouponByDay(BigDecimal c, BigDecimal t, BigDecimal ty) {
		BigDecimal ai = c.divide(ty, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP).multiply(t);
		return ai;
	}

	/**
	 * 银行间 附息债 付息周期小于一年 按平均值付息 当债券类型为附息债时的算法公式 a=c/f*(t/ts)
	 * 
	 * @param f
	 *            转换之后的付息频率
	 * @param c
	 *            票面利率
	 * @param t
	 *            上一次付息日至结算日之间的实际天数,含2月29日，算头算尾
	 * @param ts
	 *            上一次付息日至付息日之间的实际天数
	 * @return 应计利息
	 */
	public static BigDecimal getAIBankCouponByAvg(BigDecimal f, BigDecimal c, BigDecimal t, BigDecimal ts) {
		BigDecimal ai = c.divide(f, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP)
				.multiply(t.divide(ts, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP));
		return ai;
	}

	/**
	 * 银行间 当债券类型为利随本清债券时的算法公式 a=k*c+c/ts*t
	 * 
	 * @param c
	 *            票面利率
	 * @param t
	 *            上一次理论付息日至结算日之间的实际天数
	 * @param ts
	 *            上一次理论付息日至理论付息日之间的实际天数
	 * @param k
	 *            起息日至结算日之间的整年数
	 * @return 应计利息
	 */
	public static BigDecimal getAIBankInterest(BigDecimal k, BigDecimal c, BigDecimal t, BigDecimal ts) {
		BigDecimal ai = k.multiply(c).add(c.multiply(t.divide(ts, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP)));
		return ai;
	}

	/**
	 * 银行间-交易所 当债券类型为贴现债券时的算法公式 a=（100-pd）*t/T
	 * 
	 * @param T
	 *            起息日至到期日之间的天数
	 * @param t
	 *            起息日至结算日之间的天数
	 * @param pd
	 *            发行价格
	 * @return 应计利息
	 */
	public static BigDecimal getAIDiscount(BigDecimal T, BigDecimal t, BigDecimal pd) {
		BigDecimal a = (new BigDecimal("100").subtract(pd))
				.multiply(t.divide(T, DateUtils.SCALE, BigDecimal.ROUND_HALF_UP));
		a = a.setScale(DateUtils.SCALE, BigDecimal.ROUND_HALF_UP);
		return a;
	}

	/**
	 * 交易所 附息债、利随本清债 付息周期小于一年 按实际天数付息 计算公式：c/ty*t
	 * 
	 * @param c
	 *            票面利率
	 * @param t
	 *            上一次付息日至结算日之间的实际天数,含2月29日，算头算尾
	 * @return 应计利息
	 */
	public static BigDecimal getAIExchangeCoupon(BigDecimal c, BigDecimal t) {
		BigDecimal ai = getAIBankCouponByDay(c, t, BigDecimal.valueOf(365));
		return ai;
	}

	/**
	 * 质押式回购应计利息计算
	 * @param days 回购周期时间
	 * @param rate 回购利率
	 * @param price 回购金额
	 * @param act 年实际天数
	 * @return
	 */
	public static BigDecimal getAIofCR(BigDecimal days, BigDecimal rate, BigDecimal price, BigDecimal act) {
		BigDecimal CRAI = DateUtils.ZERO;
		CRAI = days.multiply(rate).divide(DateUtils.HUNDRED).multiply(price).divide(act, DateUtils.SCALE,
				BigDecimal.ROUND_HALF_UP);
		return CRAI;
	}
}
