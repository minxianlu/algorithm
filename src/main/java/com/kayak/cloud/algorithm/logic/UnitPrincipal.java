package com.kayak.cloud.algorithm.logic;

import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.CALCLogMsg;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.FixedIncome;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

public class UnitPrincipal {

    /**
     * 获取单位本金
     *
     * @param bondInfo    债券信息
     *            List<Cashflow>  现金流
     *            beginDate       起息日
     *            endDate         到期日
     * @param settleDate  结算日期
     * @return BigDecimal 单位本金
     * @throws ParseException 传入的日期类型错误时抛出的异常
     * @throws CALCException  传入的时间有误或传入的结算时间不在债券周期范围内时抛出异常
     */
    public static BigDecimal getUnitPrinciple(BondInfo bondInfo, String settleDate) throws ParseException, CALCException {
        //现金流
        List<Cashflow> cashFlows = Optional.ofNullable(bondInfo)
                .map(FixedIncome::getCashflowList)
                .orElseThrow(() -> new CALCException("债券信息里【现金流列表】字段不能为空！"));
        //起息日
        String beginDate = Optional.ofNullable(bondInfo)
                .map(FixedIncome::getBegin_date)
                .orElseThrow(() -> new CALCException("债券信息里【起息日】字段不能为空！")).replaceAll("-","");
        //到期日
        String endDate = Optional.ofNullable(bondInfo)
                .map(FixedIncome::getBegin_date)
                .orElseThrow(() -> new CALCException("债券信息里【到期日】字段不能为空！")).replaceAll("-","");
        if (beginDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateError.getMsg());
        }
        BigDecimal end_money = new BigDecimal("0");
        //当计算日期在债券期限范围内的时候，才进行查找与逻辑判断
        if (settleDate.compareTo(beginDate) >= 0 && endDate.compareTo(settleDate) >= 0) {
            for (int i = 0; i < cashFlows.size(); i++) {
                Cashflow cashFlow = cashFlows.get(i);
                //结算日期在本次付息周期内，取上一付息周期的本金余额
                if (settleDate.compareTo(cashFlow.getStart_date()) >= 0 && cashFlow.getPay_date().compareTo(settleDate) > 0) {
                    end_money = new BigDecimal(cashFlows.get(i - 1).getEnd_money());
                    break;
                }
            }
        } else if (beginDate.compareTo(settleDate) > 0) {
            throw new CALCException(CALCLogMsg.DateNotInRange.getMsg());
        }

        return end_money;
    }
}
