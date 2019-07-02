package com.kayak.cloud.algorithm.comm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.Exercise;

public class ModelUtils {	
	
	/**
	 * 根据计算日期定位在资金流中的一个周期数据，包括票面利率、开始日期、发生日期
	 * 
	 * @param list
	 *            单位资金流
	 * @param settle_date
	 *            计算日期
	 * @return 根据settle_date得到的list资金流中的一条数据，包括票面利率、开始日期、发生日期、若有提前还本发生，票面利率折算当前剩余票面价值
	 */
	public static Map<String, Object> getCash(List<Cashflow> list,String settle_date) {
		Map<String, Object> map = new HashMap<String, Object>();
		String start_date = "", pay_date = "";
		BigDecimal rate = DateUtils.ZERO, end_money = DateUtils.ZERO, quan_money = DateUtils.ZERO;
		boolean is_repayment = false;
		if(list == null){
			return map;
		}
		for (Cashflow cashflow : list) {
			if (cashflow.getChange_id() != 10) {
				continue;
			}
			// 计算日期大于或等于开始日期,小于兑付日
			if (settle_date.compareTo(cashflow.getStart_date()) >= 0
					&& cashflow.getPay_date().compareTo(settle_date) > 0) {
				start_date = cashflow.getStart_date();
				pay_date = cashflow.getPay_date();
				rate =new BigDecimal(cashflow.getCoupon_rate()) ;
				end_money = new BigDecimal(cashflow.getEnd_money());
				map.put("start_date", start_date);
				map.put("pay_date", pay_date);
				map.put("rate", rate);
				map.put("end_money", end_money);
			}
		}
		//20170529新增资金流的折算方法
		for(Cashflow cashflow : list){
			if (cashflow.getChange_id() == 12) {
				if ( settle_date.compareTo(cashflow.getPay_date()) > 0&& cashflow.getPay_date().equals(start_date)) {
					end_money = new BigDecimal(cashflow.getEnd_money());
					map.put("end_money", end_money);
					is_repayment = true;
				}
			}else if (cashflow.getChange_id() == 1) {
				quan_money = new BigDecimal(cashflow.getEnd_money());
			}
		}
		if (is_repayment) {
			rate = rate.multiply(end_money.divide(quan_money));
			map.put("rate", rate);
		}
		return map;
	}

	/**
	 * 获取本期浮息债票面利率
	 * @param bondexerciseList
	 * @param date 
	 * @return
	 */
	public static String getRate(List<Exercise> bondexerciseList,String date)  {
		String rate = "0";
		List<Exercise> ex = bondexerciseList;
		for(int i=0;i<ex.size();i++){
			Exercise exe = ex.get(i);
			if(exe.getIs_rate()==0 && i!=0){
				exe.setCoupon_rate(ex.get(i-1).getCoupon_rate());
			}
			if(exe.getExercise_date().compareTo(date)<0){
				rate = exe.getCoupon_rate();
			}
		}
		return rate;
	}

}
