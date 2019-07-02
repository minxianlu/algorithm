package com.kayak.cloud.algorithm.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.kayak.cloud.algorithm.base.AlgorithmAI;
import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.comm.ModelUtils;
import com.kayak.cloud.algorithm.model.Cashflow;

public class LogicAI {
	/**
	 * 获取ABSAi
	 *
	 * @param start_date
	 *            阶段付息起始日
	 * @param begin_date
	 *            债券起息日
	 * @param rate
	 *            债券票面利率
	 * @param settle_date
	 *            计算日期(结算日)
	 * @param bond_baseday
	 *            (付息方式)
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	public static BigDecimal getAIbyABS(String start_date, String begin_date, BigDecimal rate, String settle_date,
										int bond_baseday) throws ParseException {
		BigDecimal AI = DateUtils.ZERO;
		// k--起息日到结算日之间的年整数
		int k = DateUtils.getYears(settle_date, start_date);
		int k1 = DateUtils.getYears(settle_date, begin_date);
		// rate-票面利率，年利率
		// TY--当前计息年度的实际天数，算头不算尾
		BigDecimal TY = DateUtils.ZERO;
		// t--周期中的 实际天数
		BigDecimal t = BigDecimal.valueOf(DateUtils.DateApart(start_date, settle_date));

		int months = 12;
		// begin1--开始日期 end1--周期性付息日，也就是周期一年之后的付息日
		String begin1 = "", end1 = "";
		// 根据算法类型，债券的应计利息
		if (t.compareTo(DateUtils.ZERO) == 0) {
			AI = DateUtils.ZERO;
		} else if (k > 0) {
			// 通过传入的结算时间，判断得到对应的开始日期以及付息日
			begin1 = DateUtils.DateAddMonth(begin_date, months * k);
			end1 = DateUtils.DateAddMonth(begin1, months);

			// 结算时间在整年数范围内，大于开始日期，小于付息日的情况 当前计息年度的实际天数，算头不算尾
			TY = BigDecimal.valueOf(DateUtils.DateApart(begin1, end1));

			if (t.compareTo(DateUtils.ZERO) > 0 && t.compareTo(TY) < 0) {
				// 公式=i*c+c/TY*t
				// 若bond_baseday类型为1则为实际天数，类型为2则为360天
				if (bond_baseday == 2
						&& (TY.compareTo(DateUtils.day_365) == 0 || TY.compareTo(DateUtils.day_366) == 0)) {
					TY = DateUtils.day_360;
				}
				// a1-- 应计利息
				AI = AlgorithmAI.getAIBankInterest(rate, t, TY, new BigDecimal(Integer.toString(k)))
						.setScale(DateUtils.SCALE, BigDecimal.ROUND_HALF_UP);
			}
			if (t.compareTo(TY) == 0) {
				AI = DateUtils.ZERO;
			}

		} else if (k <= 0 && k1 <= 0) {// 该债券期限不满一年的情况
			// TY--当前计息年度的实际天数，算头不算尾
			TY = BigDecimal.valueOf(DateUtils.DateApart(begin_date, DateUtils.DateAddMonth(begin_date, months)));

			// 若bond_baseday类型为1则为实际天数，类型为2则为360天
			if (bond_baseday == 2 && (TY.compareTo(DateUtils.day_365) == 0 || TY.compareTo(DateUtils.day_366) == 0)) {
				TY = DateUtils.day_360;
			}
			// a1-- 应计利息
			AI = AlgorithmAI.getAIBankCouponByDay(rate, t, TY);

		} else {
			// 通过传入的结算时间，判断得到对应的开始日期以及付息日
			begin1 = DateUtils.DateAddMonth(begin_date, months * k);
			end1 = DateUtils.DateAddMonth(begin1, months);

			// 结算时间在整年数范围内，大于开始日期，小于付息日的情况 当前计息年度的实际天数，算头不算尾
			TY = BigDecimal.valueOf(DateUtils.DateApart(begin1, end1));

			// 若bond_baseday类型为1则为实际天数，类型为2则为360天
			if (bond_baseday == 2 && (TY.compareTo(DateUtils.day_365) == 0 || TY.compareTo(DateUtils.day_366) == 0)) {
				TY = DateUtils.day_360;
			}

			// a1-- 应计利息
			AI = AlgorithmAI.getAIBankCouponByDay(rate, t, TY);

		}

		return AI;
	}

	/**
	 * * 计算应计利息
	 *
	 * @param interest_type
	 *            息票品种
	 * @param pay_freq
	 *            付息频率
	 * @param pay_date
	 *            (银行间)实际兑付日
	 * @param publish_price
	 *            发行价格
	 * @param cashflows
	 *            单位资金流
	 * @param interest_mode
	 *            计息模式
	 * @param begin_date
	 *            起息日
	 * @param settle_date
	 *            结算日，即输入的结算日期
	 * @param end_date
	 *            到期日
	 * @param coupon_rate
	 *            收益率
	 * @param interest_sort
	 *            附息利率品种
	 * @param bond_baseday
	 *            计息基础
	 * @param market_code
	 *            市场代码
	 * @param is_abs
	 *            是否为ABS债
	 * @param first_date
	 * @return
	 * @throws ParseException
	 *             传入的日期类型错误时抛出的异常
	 */
	public static BigDecimal GetAI(Integer interest_type, Integer pay_freq, String pay_date, BigDecimal publish_price,
								   List<Cashflow> cashflows, Integer interest_mode, String begin_date, String settle_date, String end_date,
								   String coupon_rate, Integer interest_sort, int bond_baseday, String market_code, String is_abs, String first_date)
			throws ParseException {
		// 参数有效性检查
		initcheck(interest_type, publish_price, begin_date, settle_date, end_date, coupon_rate, market_code, is_abs);
		// AI--应计利息
		BigDecimal AI = DateUtils.ZERO;
		BigDecimal c = new BigDecimal(coupon_rate);
		//付息周期起始日
		String begin1 = DateUtils.getstartdate(settle_date, begin_date, pay_freq, first_date, end_date);
		// 是否为abs债券，1：是 0：否
		if ("1".equals(is_abs)) {
			return getAIbyABS(begin1, begin_date, c, settle_date, bond_baseday);
		}
		if (interest_sort == 1) {
			// 对于浮动利率债，根据当前付息期的票面利率决定
			Map<String, Object> map = ModelUtils.getCash(cashflows, settle_date);
			c = new BigDecimal(map.get("rate").toString());
		}

		// t 起息日到结算日的实际天数
		int t = DateUtils.DateApart(begin1, settle_date);
		// 结算日期在债券有效期限范围内
		if (first_date.equals(begin_date)) {
			t++;
		}
		if (settle_date.compareTo(begin_date) < 0 || end_date.equals(settle_date)) {
			return AI;
		} else if (settle_date.compareTo(begin_date) < 0 || end_date.compareTo(settle_date) < 0) {
			throw new ParseException("参数异常：结算时间不在债券期限范围内", 1);
		} else {
			if ("3".equals(market_code)) {
				// 银行间交易 1——附加息 2——零息 3——贴现
				if (interest_type == 1 && !(settle_date.equals(begin_date))) {
					Map<String, Object> map = ModelUtils.getCash(cashflows, settle_date);
					String change_date = (String) map.get("pay_date");
					String start_date = (String) map.get("start_date");

					// T 起息日到兑付日的实际天数
					int T = DateUtils.DateApart(start_date, change_date);
					// T1 转换类型之后的起息日到兑付日的实际天数
					BigDecimal T1 = BigDecimal.valueOf(T);
					// t1 转换类型之后的起息日到结算日的实际天数 算头算尾
					BigDecimal t1 = BigDecimal.valueOf(t);
					// 若bond_baseday类型为1则为实际天数，类型为2且整一年则为360天
					if (bond_baseday == 2
							&& (T1.compareTo(DateUtils.day_365) == 0 || T1.compareTo(DateUtils.day_366) == 0)) {
						T1 = DateUtils.day_360;
					}
					// 按年付息
					if (pay_freq==4) {
						AI = AlgorithmAI.getAIBankCouponOnYear(c, t1, T1);
					} else if (interest_mode == null || interest_mode == 1) {// 分段计息 按平均值计息
						AI = AlgorithmAI.getAIBankCouponByAvg(DateUtils.getTimes(pay_freq), c, t1, T1);
					} else {// 逐日计息 按实际天数计息
						// 债券本身的完整计息年度
						T = DateUtils.getDaysByMonth(begin_date, 12);
						// T1 转换类型之后的起息日到兑付日的实际天数
						T1 = BigDecimal.valueOf(T);
						AI = AlgorithmAI.getAIBankCouponByDay(c, t1, T1);
					}
					//每百元应计利息*百元剩余本金
					AI = AI.multiply(new BigDecimal(map.get("end_money").toString())).divide(DateUtils.HUNDRED,DateUtils.SCALE,
							BigDecimal.ROUND_HALF_UP);
				} else if (interest_type == 2) {
					// start_date是起息日，end_date是兑付日，传进来的参数date是结算日
					BigDecimal k = BigDecimal.valueOf(DateUtils.getYears(settle_date, begin_date));
					// T 起息日到兑付日的实际天数
					int T = DateUtils.getdays_use(bond_baseday, begin_date, end_date);
					// 转换类型，pD 是转换之后的符合精度的发行价格
					// T1 转换类型之后的起息日到兑付日的实际天数
					BigDecimal T1 = BigDecimal.valueOf(T);
					// t1 转换类型之后的起息日到结算日的实际天数 算头算尾
					BigDecimal t1 = BigDecimal.valueOf(t);
					// 若bond_baseday类型为1则为实际天数，类型为2且整一年则为360天
					if (bond_baseday == 2
							&& (T1.compareTo(DateUtils.day_365) == 0 || T1.compareTo(DateUtils.day_366) == 0)) {
						T1 = DateUtils.day_360;
					}

					AI = AlgorithmAI.getAIBankInterest(k,c, t1, T1).setScale(DateUtils.SCALE,
							BigDecimal.ROUND_HALF_UP);
				} else {// pay_date实际兑付日 贴现债
					// start_date是起息日，end_date是兑付日，传进来的参数date是结算日
					// T 起息日到兑付日的实际天数
					int T = DateUtils.DateApart(begin_date, end_date);
					// 公式=（100-pd）/T*t
					// 转换类型，pD 是转换之后的符合精度的发行价格
					// T1 转换类型之后的起息日到兑付日的实际天数
					BigDecimal T1 = BigDecimal.valueOf(T);
					// t1 转换类型之后的起息日到结算日的实际天数 算头算尾
					BigDecimal t1 = BigDecimal.valueOf(t);
					// 应计利息
					AI = AlgorithmAI.getAIDiscount(T1, t1, publish_price).setScale(DateUtils.SCALE,
							BigDecimal.ROUND_HALF_UP);
				}
			} else {
				// 交易所
				// 贴现债券
				if (interest_type == 3) {
					// start_date是起息日，end_date是兑付日，传进来的参数date是结算日
					// T 起息日到兑付日的实际天数
					int T = DateUtils.DateApart(begin_date, end_date);
					// 公式=（100-pd）/T*t
					// 转换类型，pD 是转换之后的符合精度的发行价格
					// T1 转换类型之后的起息日到兑付日的实际天数
					BigDecimal T1 = BigDecimal.valueOf(T);
					// t1 转换类型之后的起息日到结算日的实际天数 算头算尾 不计算2月29日
					BigDecimal t1 = BigDecimal.valueOf(t).subtract(DateUtils.DateFeb29(begin_date, settle_date));
					// 应计利息
					AI = AlgorithmAI.getAIDiscount(T1, t1, publish_price).setScale(DateUtils.SCALE,
							BigDecimal.ROUND_HALF_UP);
				} else {
					// 附息和利随本清
					// t1 转换类型之后的起息日到结算日的实际天数 算头算尾
					BigDecimal t1 = BigDecimal.valueOf(t);
					// 应计利息
					AI = AlgorithmAI.getAIExchangeCoupon(c, t1).setScale(DateUtils.SCALE, BigDecimal.ROUND_HALF_UP);
					Map<String, Object> map = ModelUtils.getCash(cashflows, settle_date);
					//每百元应计利息*百元剩余本金
					AI = AI.multiply(new BigDecimal(map.get("end_money").toString())).divide(DateUtils.HUNDRED,DateUtils.SCALE,
							BigDecimal.ROUND_HALF_UP);
				}
			}

		}

		return AI.setScale(DateUtils.SCALE, RoundingMode.HALF_UP);
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
	 * @return 质押式回购应计利息
	 * @throws ParseException
	 *             传入的日期类型错误时抛出的异常
	 */
	public static BigDecimal GetCRAI(String first_date, String next_date, BigDecimal rate, BigDecimal price)
			throws ParseException {
		// 参数校验
		if (first_date.compareTo(next_date) > 0) {
			throw new CALCException("输入日期有误，首期日不能大于结算日");
		}
		if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(DateUtils.HUNDRED) > 0) {
			throw new CALCException("输入的利率有误");
		}
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new CALCException("输入的金额有误");
		}
		// CRAI-质押式回购应计利息
		// CRAI=(next_date-first_date)*(rate/100)*price/365
		BigDecimal days = BigDecimal.valueOf(DateUtils.DateApart(first_date, next_date));
		BigDecimal CRAI = AlgorithmAI.getAIofCR(days, rate, price, DateUtils.day_365);

		return CRAI;
	}

	/**
	 * 参数有效性判断
	 *
	 * @param interest_type
	 *            息票品种
	 * @param publish_price
	 *            发行价格
	 * @param begin_date
	 *            起息日
	 * @param settle_date
	 *            结算日期
	 * @param end_date
	 *            到期日
	 * @param coupon_rate
	 *            利率
	 * @param market_code
	 *            市场代码
	 * @param is_abs
	 *            是否为abs债
	 */
	public static void initcheck(Integer interest_type, BigDecimal publish_price, String begin_date, String settle_date,
								 String end_date, String coupon_rate, String market_code, String is_abs) {

		if (StringUtils.isBlank(begin_date)) {
			throw new CALCException("【起息日】不能为空");
		}
		if (StringUtils.isBlank(settle_date)) {
			throw new CALCException("【结算日期】不能为空");
		}
		if (StringUtils.isBlank(coupon_rate)) {
			throw new CALCException("【利率】不能为空");
		}
		if (StringUtils.isBlank(is_abs)) {
			throw new CALCException("【是否为abs债】不能为空");
		}
		if (!"1".equals(is_abs)) {
			if (StringUtils.isBlank(end_date)) {
				throw new CALCException("【到期日】不能为空");
			}
			if (StringUtils.isBlank(market_code)) {
				throw new CALCException("【市场代码】不能为空");
			}
			if (interest_type == 3) {
				if (StringUtils.isBlank(publish_price.toString())) {
					throw new CALCException("贴现债【发行价格】不能为空");
				}
			}
		}
	}

}
