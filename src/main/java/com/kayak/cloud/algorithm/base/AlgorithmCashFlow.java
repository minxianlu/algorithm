package com.kayak.cloud.algorithm.base;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.Exercise;
import com.kayak.cloud.algorithm.model.FloatRate;

/**
 * 部分债券类型获取资产现金流
 * 
 * @author pc_xjf
 *
 */
public class AlgorithmCashFlow {

	/**
	 * 到期一次付息还本 获取资产现金流
	 * 
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @param end_date
	 *            到期日
	 * @return
	 * @throws ParseException
	 */
	public static List<Cashflow> getOnceRepayList(String coupon_rate, String begin_date, String end_date)
			throws ParseException {

		List<Cashflow> list = new ArrayList<Cashflow>();
		BigDecimal endMoney = DateUtils.HUNDRED;
		Cashflow cash1 = new Cashflow();
		// 第一条记录
		cash1.setCoupon_rate(coupon_rate);
		cash1.setStart_date(begin_date);
		cash1.setPay_date(begin_date);
		cash1.setAmount("0");
		cash1.setIs_change(0);
		cash1.setChange_id(1);
		cash1.setEnd_money(endMoney.toString());
		list.add(cash1);
		// 第二条记录，付息
		Cashflow cash2 = new Cashflow();
		cash2.setCoupon_rate(coupon_rate);
		cash2.setStart_date(begin_date);
		cash2.setPay_date(end_date);

		// double amount = 0.0;
		// amount = coupon_rate * DateUtils.DateApartPra(begin_date, end_date);
		String amount = getAmount(coupon_rate, begin_date, end_date, begin_date);
		cash2.setAmount(amount);
		cash2.setIs_change(0);
		cash2.setChange_id(10);
		cash2.setEnd_money("0");
		list.add(cash2);
		// 第三条记录，还本
		Cashflow cash3 = new Cashflow();
		cash3.setCoupon_rate(coupon_rate);
		cash3.setStart_date(begin_date);
		cash3.setPay_date(end_date);
		cash3.setIs_change(0);
		cash3.setChange_id(11);
		cash3.setAmount(endMoney.toString());
		cash3.setEnd_money("0.0");
		list.add(cash3);
		return list;
	}

	/**
	 * 获取贴现债资金流
	 * 
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @param end_date
	 *            到期日
	 * @return
	 * @throws ParseException
	 */
	public static List<Cashflow> getDiscountList(String coupon_rate, String begin_date, String end_date)
			throws ParseException {
		List<Cashflow> list = new ArrayList<Cashflow>();
		BigDecimal endMoney = DateUtils.HUNDRED;
		Cashflow cash1 = new Cashflow();
		// 第一条记录
		cash1.setCoupon_rate(coupon_rate);
		cash1.setStart_date(begin_date);
		cash1.setPay_date(begin_date);
		cash1.setAmount("0");
		cash1.setIs_change(0);
		cash1.setChange_id(1);
		cash1.setEnd_money(endMoney.toString());
		list.add(cash1);
		// 第二条记录，还本
		Cashflow cash3 = new Cashflow();
		cash3.setCoupon_rate(coupon_rate);
		cash3.setStart_date(begin_date);
		cash3.setPay_date(end_date);
		cash3.setIs_change(0);
		cash3.setChange_id(11);
		cash3.setAmount(endMoney.toString());
		cash3.setEnd_money("0.0");
		list.add(cash3);
		return list;
	}

	/**
	 * 获取单位资金流 （发生金额）
	 * 
	 * @param cash
	 *            资金流
	 * @param month
	 *            付息、还本间隔月份
	 * @param coupon_rate
	 *            利率
	 * @param interest_type
	 *            息票品种
	 * @param begin_date
	 *            开始时间
	 * @param f
	 *            付息/还本频率 次/年
	 * @return
	 * @throws ParseException
	 */
	public static Cashflow getCashAmount(Cashflow cash, int month, String coupon_rate, int interest_type,
			String begin_date, int f) throws ParseException {
		int d1 = (int) DateUtils.DateApart(cash.getStart_date(), cash.getPay_date());
		int d2 = DateUtils.getDaysByMonth(cash.getStart_date(), month);
		// 付息
		if (interest_type == 2) {// 零息
			cash.setAmount(getAmount(coupon_rate, cash.getStart_date(), cash.getPay_date(), begin_date));
		} else {
			BigDecimal al = AlgorithmAI.getAIBankCouponByAvg(BigDecimal.valueOf(f), new BigDecimal(coupon_rate),
					BigDecimal.valueOf(d1), BigDecimal.valueOf(d2));
			cash.setAmount(al.toString());
		}
		return cash;

	}

	/**
	 * 
	 * @param coupon_rate
	 *            利率
	 * @param start_date
	 *            开始日
	 * @param pay_date
	 *            付息日
	 * @param begin_date
	 *            起息日
	 * @return 起息到结算的整年数 * 票面利率 + 票面利率 * 上一付息到结算的实际天数 / 当前计息年度的实际天数
	 * @throws ParseException
	 */
	public static String getAmount(String coupon_rate, String start_date, String pay_date, String begin_date)
			throws ParseException {
		BigDecimal year = BigDecimal.valueOf(DateUtils.getYear(begin_date, pay_date));
		BigDecimal realDays = BigDecimal.valueOf(DateUtils.DateApart(start_date, pay_date));
		BigDecimal nowYearDays = BigDecimal.valueOf(DateUtils.getYearDays(pay_date));
		if (realDays.compareTo(nowYearDays) == 0) {
			year = year.subtract(BigDecimal.valueOf(1));
		}
		BigDecimal ai = AlgorithmAI.getAIBankInterest(year, new BigDecimal(coupon_rate), realDays, nowYearDays);
		return ai.toString();
	}

	/**
	 * 获取利率
	 * 
	 * @param occurDate
	 *            付息开始日期
	 * @param coupon_rate
	 *            利率
	 * @param interest_sort
	 *            附息利率品种
	 * @param bond_spread
	 *            利差
	 * @param is_exercise
	 *            是否行权
	 * @param bondexerciseList
	 *            行权信息
	 * @param bondfloatrateList
	 *            浮动利率信息
	 * @return
	 */
	public static String countRate(String occurDate, String coupon_rate, Integer interest_sort, String bond_spread,
			Integer is_exercise, List<Exercise> bondexerciseList, List<FloatRate> bondfloatrateList) {
		BigDecimal couponrate = new BigDecimal(coupon_rate);
		BigDecimal pc = DateUtils.ZERO;
		BigDecimal lc = DateUtils.ZERO;
		// 债券基本利率
		lc = new BigDecimal(bond_spread == null || bond_spread.compareTo("") == 0 ? "0" : bond_spread);// 债券基本利差

		// 是否行权判断
		if (is_exercise == 1 && bondexerciseList!= null) { // 行权
			// 查询债券行权维护信息
			for (int i = 0; i < bondexerciseList.size(); i++) {
				if (bondexerciseList.get(i).getExercise_date().compareTo(occurDate) <= 0) {
					pc = new BigDecimal(bondexerciseList.get(i).getCoupon_rate()).add(pc);// 票面补偿
				}
			}
		}

		// 付息债判断
		if (interest_sort == 1 && bondfloatrateList!= null) {
			// 查询浮息债的浮动利率
			for (int i = 0; i < bondfloatrateList.size(); i++) {
				if (bondfloatrateList.get(i).getBegin_date().compareTo(occurDate) <= 0
						&& bondfloatrateList.get(i).getEnd_date().compareTo(occurDate) >= 0) {
					couponrate = bondfloatrateList.get(i).getBase_rate();// 票面补偿
				}
			}

			couponrate = couponrate.add(lc);
		}
		// 基础利率加上补偿利率
		couponrate = couponrate.add(pc);

		return couponrate.toString();
	}

	/**
	 * 获取起息的cashflow 记录
	 * 
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @return
	 */
	public static Cashflow getFirCash(String coupon_rate, String begin_date) {
		Cashflow cash = new Cashflow();

		// 第一条记录
		cash.setCoupon_rate(coupon_rate);
		cash.setStart_date(begin_date);
		cash.setPay_date(begin_date);
		cash.setAmount("0");
		cash.setIs_change(0);
		cash.setChange_id(1);
		cash.setEnd_money(DateUtils.HUNDRED.toString());
		return cash;
	}

	/**
	 * 获取到期还本的cashflow 记录
	 * 
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @return
	 */
	public static Cashflow getLastCash(String coupon_rate, String begin_date, String end_date, String endMoney) {
		Cashflow cash = new Cashflow();
		// 最后一条记录
		cash.setCoupon_rate(coupon_rate);
		cash.setStart_date(begin_date);
		cash.setPay_date(end_date);
		cash.setAmount(endMoney);
		cash.setIs_change(0);
		cash.setChange_id(11);
		cash.setEnd_money("0");
		return cash;
	}

}
