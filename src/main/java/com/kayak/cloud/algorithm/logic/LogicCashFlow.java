package com.kayak.cloud.algorithm.logic;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kayak.cloud.algorithm.base.AlgorithmAI;
import com.kayak.cloud.algorithm.base.AlgorithmCashFlow;
import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.Exercise;
import com.kayak.cloud.algorithm.model.FloatRate;
import com.kayak.cloud.algorithm.model.RedempVO;


public class LogicCashFlow {

	/**
	 * 获取资金流list
	 * @param bondId 债券信息
	 * @param is_bond 是否为债券
	 * @return
	 * @throws ParseException
	 */
	public static List<Cashflow> getCashFlowList(BondInfo bondId,int is_bond) throws ParseException{
		//参数检查
		initcheck(bondId);
		// 获取债券基础信息
		if(is_bond == 1){
			return getBondCashflow(bondId);
		}
		return bCashflow(bondId.getCoupon_rate(), bondId.getBegin_date(), bondId.getFirst_pay_date(), bondId.getEnd_date(), 
				bondId.getPay_freq(), bondId.getRepayment_type(), bondId.getBondredempvoList(), bondId.getBondexerciseList(),
				bondId.getBondfloatrateList(), bondId.getInterest_sort(), bondId.getBond_spread(), bondId.getIs_exercise(), bondId.getData_list());
	}
	
	
	/**
	 * 
	 * 针对类似于债券的产品 ，生成资金流list
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @param first_date
	 * 			  首次付息日
	 * @param end_date
	 *            到期日
	 * @param pay_freq
	 *            付息频率
	 * @param repayperiod
	 *            还本方式
	 * @throws ParseException
	 */
	public static List<Cashflow> bCashflow(String coupon_rate,String begin_date,String first_date, String end_date, 
			int pay_freq, int repayperiod,List<RedempVO> bondRedempList,List<Exercise> bondexerciseList,
			List<FloatRate> bondfloatrateList,Integer interest_sort,String bond_spread,Integer is_exercise,String data_list
			) throws ParseException {		
		List<Cashflow> list = new ArrayList<Cashflow>();
		BigDecimal endMoney = DateUtils.HUNDRED;
		String pay_date1 = null;
		BigDecimal rate =new BigDecimal(coupon_rate);
		int months = DateUtils.getMonths(begin_date, end_date);
		if(first_date != null && !first_date.equals("")){
			months=DateUtils.getMonths(first_date, end_date);
		}
		int times = 0;// 多少次还完?
		int month = 0;// 多少个月还一次?
		if(pay_freq == 5){ //到期一次付息还本
			list = AlgorithmCashFlow.getOnceRepayList(coupon_rate,begin_date,end_date);
			return list;
		} else if(pay_freq == 6){//自定义
			list = getCustomizeList(coupon_rate,begin_date,end_date,bondRedempList,interest_sort,bond_spread,is_exercise,data_list,bondexerciseList, bondfloatrateList);
			return list;
		} else if (pay_freq == 1) {//月
			rate = rate.divide(new BigDecimal("12"));
			times = months;
			month = 1;
		} else if (pay_freq == 2) {//季
			rate = rate.divide(new BigDecimal("4"));
			times = (int) Math.ceil(months / 3.0);
			month = 3;
		} else if (pay_freq == 3) {//半年
			rate = rate.divide(new BigDecimal("2"));
			times = (int) Math.ceil(months / 6.0);
			month = 6;
		} else if (pay_freq == 4){//一年
			rate = rate.divide(new BigDecimal("1"));
			times = (int) Math.ceil(months / 12.0);
			month = 12;
		}
		if(first_date != null && !first_date.equals("")){
			times =times + 1;
		}
		if (times == 0) {
			times = 1;
		}
		//第一条记录，是起息。
		list.add(AlgorithmCashFlow.getFirCash(coupon_rate, begin_date));
		//每次提前还本本金
		BigDecimal amount =DateUtils.HUNDRED.divide(BigDecimal.valueOf(times));
		if (repayperiod == 2) {// 提前还本的情况
			for (int i = 0; i < times; i++) {//先算付息
				Cashflow cash = new Cashflow();
				cash.setCoupon_rate(rate.toString());
				// 获取付息现金流
				cash = getPayCashflow(i, cash, begin_date, first_date, end_date,month, rate, times, pay_date1);
				//下一周期现金流开始日
				pay_date1 = cash.getPay_date();
				//票面利息未发生变化
				cash.setIs_change(0);
				cash.setChange_id(10);
				//因为，有提前还本，在同一天，先结算付息，后结算本金，所以，本金也要相对应
				if(i == (times-1)){
					cash.setEnd_money("0");
				}else if(i == 0){
					if(first_date!="" && first_date!=begin_date){
						//首次付息日与起息日间隔天数
						int d1 = DateUtils.DateApart(begin_date,first_date);
						//首次付息周期天数
						int d2 = DateUtils.DateApart(begin_date,DateUtils.addMonths(cash.getStart_date(), month,end_date));
						//剩余本金
						endMoney = endMoney.subtract(AlgorithmAI.getAIBankCouponByAvg(BigDecimal.valueOf(times),DateUtils.HUNDRED,BigDecimal.valueOf(d1), BigDecimal.valueOf(d2)));
						//下次提前还本本金
						amount = endMoney.divide(BigDecimal.valueOf(times-1));
						cash.setEnd_money(endMoney.toString());
					}
				}else{
					//剩余本金 = 上周期剩余本金-提前还本本金
					endMoney =endMoney.subtract(amount);
					cash.setEnd_money(endMoney.toString());
				}
				list.add(cash);
			}
			//重新赋值为100
			endMoney = DateUtils.HUNDRED;
			for (int i = 0; i < times; i++) {//结算还本金额
				Cashflow cash = new Cashflow();
				cash.setCoupon_rate(rate.toString());
				
				// 获取提前还本现金流 -- 单位资金流之后重新赋值
				cash = getPayCashflow(i, cash, begin_date, first_date, end_date,month, rate, times, pay_date1);

				pay_date1 = cash.getPay_date();
				cash.setIs_change(0);
				//第一次提前还本 且 首次付息日不为空 不为起息日
				if( i==0 && first_date!="" && first_date!=begin_date){
					//首次付息日与起息日间隔天数
					int d1 = (int)DateUtils.DateApart(begin_date,first_date);
					//首次付息周期天数
					int d2 = (int)DateUtils.DateApart(begin_date,DateUtils.addMonths(cash.getStart_date(), month,end_date));
					cash.setAmount((AlgorithmAI.getAIBankCouponByAvg(BigDecimal.valueOf(times),DateUtils.HUNDRED,BigDecimal.valueOf(d1), BigDecimal.valueOf(d2))).toString());
					//剩余本金
					endMoney = endMoney.subtract(new BigDecimal(cash.getAmount()));
					//下次提前还本本金
					amount = endMoney.divide(BigDecimal.valueOf(times-1));
					cash.setEnd_money(endMoney.toString());
					cash.setChange_id(12);
				}else if(i == (times-1)){
					//最后还本周期 为到期还本
					cash.setAmount(endMoney.toString());
					cash.setEnd_money("0");
					cash.setChange_id(11);
					cash.setStart_date(begin_date);
				}else {
					cash.setAmount(amount.toString());
					endMoney = endMoney.subtract(new BigDecimal(cash.getAmount()));
					cash.setEnd_money(endMoney.toString());
					cash.setChange_id(12);
				}
				list.add(cash);
			}
		} else {// 到期还本
			for (int i = 0; i < times; i++) {
				Cashflow cash = new Cashflow();
				//coupon_rate 修改此值
				cash.setCoupon_rate(rate.toString());
				// 起息的时间
				cash = getPayCashflow(i, cash, begin_date, first_date, end_date,month, rate, times, pay_date1);
				pay_date1 = cash.getPay_date();
				cash.setIs_change(0);
				cash.setChange_id(10);
				//因为，有提前还本，在同一天，先结算付息，后结算本金，所以，本金也要相对应
				if(i == (times-1)){
					cash.setEnd_money("0");
				}else{
					cash.setEnd_money(endMoney.toString());
				}
				
				list.add(cash);
			}
			
			Cashflow cash2 = new Cashflow();
			cash2 = AlgorithmCashFlow.getLastCash(rate.toString(), pay_date1, end_date, endMoney.toString());
			list.add(cash2);
		}
		
		return list;
	}
	
	
	/**
	 * 	
	 * 债券获取资金流水
	 * 股息债券
	 * @param bondId 债券信息
	 * @return
	 * @throws ParseException
	 */
	public static List<Cashflow> getBondCashflow( BondInfo bondId)
			throws ParseException{
		
		String begin_date = bondId.getBegin_date();//起息日
		String first_date =bondId.getFirst_pay_date();//首次付息日
		String end_date =  bondId.getEnd_date();//到期日
		int pay_freq =  bondId.getPay_freq();//付息频率
		String coupon_rate = bondId.getCoupon_rate();//利率
		List<RedempVO> bondRedempList = bondId.getBondredempvoList();//提前还本信息
		List<Exercise> bondexerciseList = bondId.getBondexerciseList();//行权信息
		List<FloatRate> bondfloatrateList = bondId.getBondfloatrateList();//浮动利率信息

		Integer interest_sort =  bondId.getInterest_sort();//附息利率品种:1——浮动利率 2——固定利率 3——累进利率
		String bond_spread = bondId.getBond_spread();//利差
		Integer is_exercise = bondId.getIs_exercise();//是否行权
		String data_list = bondId.getData_list();//自定义付息日
	
		List<Cashflow> list = new ArrayList<Cashflow>();//现金流
		int interest_type = bondId.getInterest_type();//付息品种，2--零息，1--附加利息，3--贴现
		int months = DateUtils.getMonths(begin_date, end_date);
		int times = 0;// 多少次还完?
		int month = 0;// 多少个月还一次?
		if (pay_freq == 5) { // 到期一次付息还本
			if(interest_type == 3){
				//贴现资金现金流
				list = AlgorithmCashFlow.getDiscountList(coupon_rate,begin_date,end_date);
			}else{
				//其他债券到期还本资金流
				list = AlgorithmCashFlow.getOnceRepayList(coupon_rate,begin_date,end_date);
			}
			return list;
		} else if (pay_freq == 6) {// 自定义
			list = getCustomizeList(coupon_rate,begin_date,end_date,bondRedempList,interest_sort,bond_spread,is_exercise,data_list,bondexerciseList, bondfloatrateList);
			return list;
		} else if (pay_freq == 1) {// 月
			times = months;
			month = 1;
		} else if (pay_freq == 2) {// 季
			times = months / 3;
			month = 3;
		} else if (pay_freq == 3) {// 半年
			times = months / 6;
			month = 6;
		} else if (pay_freq == 4) {// 一年
			times = months / 12;
			month = 12;
		}
		//首次付息日不为起息日则付息次数+1
		if(first_date!="" && first_date!=begin_date){
			times =times + 1;
		}
		if (times == 0) {
			times = 1;
		}		
		
		return getRegularList(coupon_rate,begin_date,first_date,end_date,interest_type,
				times, month, bondRedempList,interest_sort, bond_spread,is_exercise,bondexerciseList, bondfloatrateList);
	}
	
	/**
	 * 自定义付息方式 获取资产现金流 固息 
	 *
	 * @param coupon_rate
	 *            利率
	 * @param begin_date
	 *            起息日
	 * @param end_date
	 *            到期日
	 * @return
	 * @throws ParseException
	 * @throws SQLException
	 */
	public static List<Cashflow> getCustomizeList(String coupon_rate, String begin_date, String end_date,
			List<RedempVO> bondRedempList, Integer interest_sort, String bond_spread, Integer is_exercise,
			String data_list, List<Exercise> bondexerciseList, List<FloatRate> bondfloatrateList)
					throws ParseException {

		List<Cashflow> list = new ArrayList<Cashflow>();
		BigDecimal endMoney = DateUtils.HUNDRED;
		// 第一条记录
		list.add(AlgorithmCashFlow.getFirCash(coupon_rate, begin_date));
		// 付息
		String other = data_list.toString();
		List<String> others = new ArrayList<String>();
		others.addAll(Arrays.asList(other.split("\\|")));
		for (int i = 0; i < others.size(); i++) {
			Cashflow cash2 = new Cashflow();
			Cashflow casht = new Cashflow();
			BigDecimal coup = new BigDecimal(coupon_rate);//赋初始值，利率
			if (i == 0) {
				//第一条付息 起息日为债券起息日
				cash2.setStart_date(begin_date);
			}  else {
				//其他付息周期
				cash2.setStart_date(others.get(i - 1));
			}
			//付息周期到期日为自定义付息周期到期日
			cash2.setPay_date(others.get(i));
			//票面利率与上次比较，未变化置0，变化置1
			cash2.setIs_change(coup.compareTo(new BigDecimal(cash2.getCoupon_rate())) == 0 ? 0 : 1);
			//付息
			cash2.setChange_id(10);
			//计算利率
			coupon_rate = AlgorithmCashFlow.countRate(cash2.getStart_date(), coupon_rate, interest_sort, bond_spread, is_exercise,
					bondexerciseList, bondfloatrateList);
			cash2.setCoupon_rate(coupon_rate);
			//计算利息
			String amount = AlgorithmCashFlow.getAmount(coupon_rate, cash2.getStart_date(), cash2.getPay_date(), begin_date);
			cash2.setAmount(amount);
			
			// 存在提前记录进行单独处理 提前还本时间一般在付息日
			if (bondRedempList != null && bondRedempList.size() > 0) {
				for (int j = 0; j < bondRedempList.size(); j++) {
					RedempVO bondRedemp = (RedempVO) bondRedempList.get(j);
					if (bondRedemp.getExerciseDate().equals(cash2.getPay_date())) {// 提前还本的日期是发生日期
						casht.setChange_id(12);
						casht.setPay_date(bondRedemp.getExerciseDate());
						casht.setStart_date(""); // 付息存，其它都不用存
						casht.setCoupon_rate("0");
						casht.setAmount(bondRedemp.getUnitPrincipal().toString());
						casht.setIs_change(cash2.getChange_id() == null ? 0 : cash2.getChange_id());// 利率是否变化和上次一样
						//剩余本金 = 原始剩余本金-提前还本本金
						endMoney = endMoney.subtract(bondRedemp.getUnitPrincipal());
						casht.setEnd_money(endMoney.toString());
					}
				}
			}
			// 付息日大于或等于到期日，剩余本金置0
			if (cash2.getPay_date().compareTo(end_date) >= 0) {
				cash2.setEnd_money("0");
			} else {
				cash2.setEnd_money(endMoney.toString());
			}
			list.add(cash2);
			// 添加还本记录
			if (casht.getPay_date() != null && !"".equals(casht.getPay_date())) {
				casht.setCoupon_rate(coupon_rate);
				list.add(casht);
			}
			coup = new BigDecimal(cash2.getCoupon_rate());
		}/* for循环结束 */
		// 如果最后的付息日比到期日小，则到期日前最后付息日至到期日为最后一个付息周期
		if (others.get(others.size() - 1).compareTo(end_date) < 0) {
			Cashflow cash4 = new Cashflow();
			//最后付息周期起息日为前一个付息周期到期日，到期日为债券到期日
			cash4.setStart_date(others.get(others.size() - 1));
			cash4.setCoupon_rate(coupon_rate);
			cash4.setPay_date(end_date);
			//最后付息周期付息天数
			int days = DateUtils.DateApart(cash4.getStart_date(), cash4.getPay_date());
			//计算利息
			BigDecimal amount =AlgorithmAI.getAIExchangeCoupon(new BigDecimal(coupon_rate), new BigDecimal(days));
			cash4.setAmount(amount.toString());
			cash4.setIs_change(0);
			cash4.setChange_id(10);
			cash4.setEnd_money("0");
			list.add(cash4);
		}
		// 还本
		list.add(AlgorithmCashFlow.getLastCash(coupon_rate, begin_date, end_date, endMoney.toString()));
		return list;
	}
	
	
	/**
	 * 获取资产现金流 固息/浮息 行权
	 * @param coupon_rate 利率
	 * @param begin_date 起息日
	 * @param first_date 首次付息日
	 * @param end_date 到期日
	 * @param interest_type 息票品种
	 * @param times 还息次数
	 * @param month 还息间隔月数
	 * @param bondRedempList 提前还本信息
	 * @param interest_sort 附息利率品种
	 * @param bond_spread 利差
	 * @param is_exercise 是否行权
	 * @param bondexerciseList 行权信息
	 * @param bondfloatrateList 浮动利率信息
	 * @return
	 * @throws ParseException
	 */
	public static List<Cashflow> getRegularList(String coupon_rate, String begin_date, String first_date,
			String end_date, int interest_type, int times, int month, List<RedempVO> bondRedempList,
			Integer interest_sort, String bond_spread, Integer is_exercise, List<Exercise> bondexerciseList,
			List<FloatRate> bondfloatrateList) throws ParseException {
		
		BigDecimal endMoney = DateUtils.HUNDRED;
		List<Cashflow> list = new ArrayList<Cashflow>();
		String pay_date1 = null;
		int f = 12 / month;// 年还息次数

		// 第一条记录，是起息。
		list.add(AlgorithmCashFlow.getFirCash(coupon_rate, begin_date));

		// 到期还本
		BigDecimal coup = new BigDecimal(0);
		for (int i = 0; i < times; i++) {
			Cashflow cash = new Cashflow();
			Cashflow casht = new Cashflow();
			// coupon_rate 修改此值
			if (i == 0) {// 起息的时间，第一条付息
				cash.setStart_date(begin_date);
				if (first_date != "" && !first_date.equals(begin_date)) {// 首次付息不为空或不是起息日
					cash.setPay_date(first_date);// 发生日期为首次付息日
				} else {// 首次付息不明确，根据算法算出发生日期
					cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month, end_date));
				}
			} else if (i == times - 1) {// 最后一条付息
				cash.setStart_date(pay_date1);// 开始日期是上条记录的发生日期
				cash.setPay_date(end_date);// 发生日期是结束日期
			} else {
				if (first_date != "" && !first_date.equals(begin_date) && i == 1) {
					cash.setStart_date(first_date);// 开始日期是首次付息日
				} else {// 首次付息日不明确
					cash.setStart_date(pay_date1);// 开始日期是上次的发生日期
				}
				cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month, end_date));
			}
			// 处理提前还本记录进行单独处理；
			// 存在提前记录进行单独处理
			if (bondRedempList != null && bondRedempList.size() > 0) {
				for (int j = 0; j < bondRedempList.size(); j++) {
					RedempVO bondRedemp = (RedempVO) bondRedempList.get(j);
					if (bondRedemp.getExerciseDate().equals(cash.getPay_date())) {// 提前还本的日期是发生日期
						casht.setChange_id(12);
						casht.setPay_date(bondRedemp.getExerciseDate());
						//符合需求文档 开始日期为付息周期开始日期
						casht.setStart_date(cash.getStart_date()); // 付息存，其它都不用存
						casht.setCoupon_rate("0");
						casht.setAmount(bondRedemp.getUnitPrincipal().toString());
						casht.setIs_change(cash.getChange_id() == null ? 0 : cash.getChange_id());// 利率是否变化和上次一样
						//剩余本金 = 上期剩余本金 - 提前还本单位金额
						endMoney = endMoney.subtract(bondRedemp.getUnitPrincipal());
						casht.setEnd_money(endMoney.toString());
					}
				}
			}
			coupon_rate = AlgorithmCashFlow.countRate(cash.getStart_date(), coupon_rate, interest_sort, bond_spread, is_exercise,
					bondexerciseList, bondfloatrateList);
			// 设置单位资金流
			cash = AlgorithmCashFlow.getCashAmount(cash, month, coupon_rate, interest_type, begin_date, f);

			cash.setCoupon_rate(coupon_rate);
			pay_date1 = cash.getPay_date();
			//票面利率是否发生变化
			cash.setIs_change(coup.compareTo(new BigDecimal(cash.getCoupon_rate())) == 0 ? 0 : 1);
			cash.setChange_id(10);
			// 因为，有提前还本，在同一天，先结算付息，后结算本金，所以，本金也要相对应
			if (i == (times - 1)) {
				cash.setEnd_money("0");
			} else {
				cash.setEnd_money(endMoney.toString());
			}
			if (cash.getStart_date().equals(cash.getPay_date()))
				continue;
			list.add(cash);
			// 添加还本记录
			if (casht.getPay_date() != null && !"".equals(casht.getPay_date())) {
				casht.setCoupon_rate(coupon_rate);
				list.add(casht);
			}
			coup = new BigDecimal(cash.getCoupon_rate());
		} /* for循环结束 */

		// 到期
		coupon_rate = AlgorithmCashFlow.countRate(end_date, coupon_rate, interest_sort, bond_spread, is_exercise, bondexerciseList,
				bondfloatrateList);
		list.add(AlgorithmCashFlow.getLastCash(coupon_rate, begin_date, end_date, endMoney.toString()));
		

		return list;
	}

	/**
	 * 获取付息现金流
	 * @param i  第i个付息
	 * @param cash 现金流
	 * @param begin_date 起息日
	 * @param first_date 首次付息日
	 * @param end_date 到期日
	 * @param month 付息间隔月份
	 * @param rate 利率
	 * @param times 年付息次数
	 * @param pay_date1 上次付息日
	 * @return 
	 * @throws ParseException
	 */
	public static Cashflow getPayCashflow(int i ,Cashflow cash,String begin_date,String first_date,String end_date,
			int month,BigDecimal rate,int times,String pay_date1) throws ParseException{
		if (i == 0) {
			//第一周期 开始日期为起息日 
			cash.setStart_date(begin_date);
			//首次付息日不为空 且不为起息日 
			if(first_date!="" && !first_date.equals(begin_date)){
				cash.setPay_date(first_date);
				//首次付息日与起息日间隔天数
				int d1 = DateUtils.DateApart(begin_date,first_date);
				//首次付息原周期（间隔月数）
				int d2 = DateUtils.DateApart(begin_date,DateUtils.addMonths(cash.getStart_date(), month,end_date));
				// 付息
				BigDecimal amount2 = AlgorithmAI.getAIBankCouponOnYear(rate, BigDecimal.valueOf(d1), BigDecimal.valueOf(d2));
				cash.setAmount(amount2.toString());
			}else{
				//付息时间 起息日间隔月数后日期
				cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month,end_date));
				//按期付息 单位资金流 = 利率*100/每年期数
				cash.setAmount(rate.toString());
			}
		} else if(i == 1){
			//第二个周期
			if(first_date!="" && !first_date.equals(begin_date)){
				cash.setStart_date(first_date);
				cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month,end_date));
			}else{
				cash.setStart_date(pay_date1);
				cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month,end_date));
			}
			cash.setAmount(rate.toString());
		}else if(i == times -1){
			//最后周期
			cash.setStart_date(pay_date1);
			cash.setPay_date(end_date);
			//发生日与开始日间隔天数
			int d1 = DateUtils.DateApart(cash.getStart_date(),cash.getPay_date());
			//原周期实际天数（间隔月数）
			int d2 = DateUtils.DateApart(cash.getStart_date(),DateUtils.addMonths(cash.getStart_date(), month,end_date));
			BigDecimal amount2 = DateUtils.ZERO;
			if(d1 != 0 && d2 != 0){
				//单位资金流 c*d1/d2
				amount2 = AlgorithmAI.getAIBankCouponOnYear(rate, BigDecimal.valueOf(d1), BigDecimal.valueOf(d2));
			}
			// 付息
			cash.setAmount(amount2.toString());
		}else {
			//其他周期
			cash.setStart_date(pay_date1);
			cash.setPay_date(DateUtils.addMonths(cash.getStart_date(), month,end_date));
			cash.setAmount(rate.toString());
		}
		return cash;
	}
	
	
	/**
	 * 参数 约束 检查
	 * 数字属性 已设置默认值 无需非空检查
	 * @param bondId
	 */
	public static void initcheck(BondInfo bondId){

		if(StringUtils.isBlank(bondId.getBegin_date())){
			throw new CALCException("【起息日】不能为空");
		}
		if(StringUtils.isBlank(bondId.getFirst_pay_date())){
			throw new CALCException("【首次付息日】不能为空");
		}
		if(StringUtils.isBlank(bondId.getEnd_date())){
			throw new CALCException("【到期日】不能为空");
		}
		if(StringUtils.isBlank(bondId.getCoupon_rate())){
			throw new CALCException("【利率】不能为空");
		}
		if(StringUtils.isBlank(bondId.getBond_spread())){
			throw new CALCException("【利差】不能为空");
		}
		if("6".equals(bondId.getPay_freq()) && StringUtils.isBlank(bondId.getData_list())){
			throw new CALCException("自定义债券【自定义付息日】不能为空");
		}
	}
	
}
