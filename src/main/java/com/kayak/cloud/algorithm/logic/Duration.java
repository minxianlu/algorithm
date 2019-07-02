package com.kayak.cloud.algorithm.logic;


import com.kayak.cloud.algorithm.comm.CALCException;
import com.kayak.cloud.algorithm.comm.CALCLogMsg;
import com.kayak.cloud.algorithm.comm.DateUtils;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.Exercise;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



public class Duration {
    /**
     * 常量SCALE，设置BigDecimal 类型的精度
     */
    private static final int SCALE = 16;
    /**
     * 常量YDATE-365
     */
    private static final BigDecimal YDATE = new BigDecimal(365);
    /**
     * 常量YEARS=365
     */
    private static final int YEARS = 365;
    /**
     * 常量MONEY-100
     */
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    /**
     * 资产久期
     *
     * @param netPrice      百元净价
     * @param isDoExercise 是否行权:0否  1是
     * @param settleDate    结算日，即输入的结算日期
     * @param bondInfo      债券信息
     *                      interest_type  票息品种
     *                      List<Cashflow> 现金流
     *                      List<Exercise> 行权信息
     *                      is_exercise    是否含权
     *                      pay_freq       付息频率
     *                      begin_date     起息日
     *                      end_date       到期日
     * @return 百元净价
     * @throws ParseException 传入的日期类型错误时抛出的异常
     * @throws CALCException  以下四种种情况抛出该异常：(1)资产类型不是债券，或者算法不是中债算法抛出该异常 (2)传入的结算时间不在债券周期范围内
     *                        (3)净价不能小于0或大于200 (4)输入的起息日大于等于到期日
     */
    public static BigDecimal getDuration(String settleDate, int isDoExercise, BigDecimal netPrice, BondInfo bondInfo) throws ParseException, CALCException {
        //资产类型:1-债券
        int bond_type = 1;
        //算法类型：1-中债算法，2-外汇算法
        int arithmetic_type = 1;
        int interest_type = bondInfo.getInterest_type();
        settleDate = settleDate.replaceAll("-","");
        //现金流
        List<Cashflow> cashFlows = bondInfo.getCashflowList();
        //行权信息
        List<Exercise> bondExercise = bondInfo.getBondexerciseList();
        // 查询的时候已经按照日期排序
        List<String> exerciseDates = new ArrayList<>();
        for (Exercise be : bondExercise) {
            exerciseDates.add(be.getExercise_date());
        }
        int isExercise = bondInfo.getIs_exercise();
        int payFreq = bondInfo.getPay_freq();
        String beginDate = bondInfo.getBegin_date();
        String endDate = bondInfo.getEnd_date();

        // 检查参数有效性
        if (beginDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateError.getMsg());
        } else if (netPrice.compareTo(new BigDecimal(200)) > 0 || netPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CALCException(CALCLogMsg.NetPriceIllegal.getMsg());
        }
        // 资产久期
        BigDecimal D = new BigDecimal("0.0");
        if (bond_type == 1 && (arithmetic_type == 1 || arithmetic_type == 2)) {
            switch (interest_type) {// 息票品种
                case 3:// 贴现债券
                    return D;
                case 2:
                    return D;
                default:// 附息债券和零息债券
                    if (isDoExercise != 0) {// 行权
                        // 如果是含权债，改变结算日。否则，返回0；
                        if (isExercise == 1) {
                            endDate = changeEndDate(exerciseDates);
                        } else {
                            return new BigDecimal(0);
                        }
                    }
                    // 资产久期
                    BigDecimal duration;
                    // P-全价，调用净价推全价的算法得到
                    BigDecimal P = LogicPrice.NetPriceToFullPrice(netPrice, settleDate, bondInfo);
                    Map<String, Object> map = getCash(cashFlows, settleDate);
                    String pay_date = (String) map.get("pay_date");
                    // Ct-票面利率/付息频率
                    // rate-票面利率
                    BigDecimal rate = new BigDecimal(map.get("rate").toString());
                    // freq-付息频率
                    BigDecimal freq = new BigDecimal(Integer.toString(DateUtils
                            .getYearFreq(payFreq)));
                    BigDecimal Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP);
                    // y-到期收益率
                    BigDecimal y = PriceAndYtm.getYTMByRealPrice(bondInfo, netPrice, settleDate);
                    // t-（下一次付息日-计算日期）/365
                    BigDecimal t = new BigDecimal("0.0");
                    BigDecimal a = new BigDecimal("0.0");
                    int k;

                    if (freq.intValue() == 1) {// 付息频率为按年支付的债券
                        // 付息日至到期日之间的年整数
                        k = (DateUtils.DateApart(pay_date, endDate) / YEARS);
                        // 计算日期至付息日之间的天数
                        a = getDate(settleDate, pay_date);
                        for (int i = 0; i < k; i++) {
                            if (i == 0) {// 第一年的情况
                                // t=a/365
                                t = a.divide(YDATE, SCALE,
                                        BigDecimal.ROUND_HALF_UP);
                            } else {// 从第二年至期限完毕的整年度的情况
                                // t=t+1
                                t = t.add(BigDecimal.ONE);
                            }
                            // duration=c*t/(1+y/100)^t
                            duration = formulaDur(Ct, t, y);
                            // D=累加duration
                            D = D.add(duration);
                        }
                        // 最后一期，加上本金（初始本金100），则Ct=c+100
                        Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP).add(HUNDRED);
                        // t=计算日期至付息日之间的天数/365+k
                        t = a.divide(YDATE, SCALE, BigDecimal.ROUND_HALF_UP).add(
                                new BigDecimal(k));
                        // duration=c*t/(1+y/100)^t
                        duration = formulaDur(Ct, t, y);
                        // D=累加duration
                        D = D.add(duration);
                    } else {// 支付频率为：按月支付，按季支付，半年支付
                    /*
                    //算法待定
					return new BigDecimal("0.0");
					*/
                        // pay-该List是从当前付息日开始，至到期日之间的所有付息日
                        List<String> pay = getPayDate(settleDate, cashFlows);
                        for (int i = 0; i < pay.size(); i++) {// 将pay中的付息日循环
                            if (i < pay.size() - 1) {
                                // 循环付息日，确定pay_date，以计算出t
                                pay_date = pay.get(i);
                                // a-得到BigDecimal类型的两个日期之间的天数
                                a = getDate(settleDate, pay_date);
                                t = a.divide(YDATE, SCALE,
                                        BigDecimal.ROUND_HALF_UP);
                                // duration=c*t/(1+y/100)^t
                                duration = formulaDur(Ct, t, y);
                                // D=累加duration
                                D = D.add(duration);
                            } else if (i == pay.size() - 1) {// 最后一次付息
                                // Ct=票面利率/付息频率+100
                                Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP).add(HUNDRED);
                                pay_date = pay.get(i);
                                a = getDate(settleDate, pay_date);
                                t = a.divide(YDATE, SCALE,
                                        BigDecimal.ROUND_HALF_UP);
                                duration = formulaDur(Ct, t, y);
                                D = D.add(duration);
                            }
                        }
                    }
                    D = D.divide(P, 16, BigDecimal.ROUND_HALF_UP);
                    return D;
            }// end switch
        } else {
            throw new CALCException("该类型暂未录入");
        }
    }

    /**
     * 修正久期
     *
     * @param netPrice      百元净价
     * @param isDoExercise 是否行权，1--行权，0--不行权
     * @param bondInfo      债券信息
     * @return 修正久期
     * @throws ParseException 传入的日期类型错误时抛出的异常
     * @throws CALCException  以下四种种情况抛出该异常：(1)资产类型不是债券，或者算法不是中债算法抛出该异常 (2)传入的结算时间不在债券周期范围内
     *                        (3)净价不能小于0或大于200 (4)输入的起息日大于等于到期日
     */
    public static BigDecimal getMODDuration(BigDecimal netPrice, String settleDate,
                                            int isDoExercise, BondInfo bondInfo) throws ParseException, CALCException {
        //行权信息
        List<Exercise> bondExercise = bondInfo.getBondexerciseList();
        // 查询的时候已经按照日期排序
        List<String> exerciseDates = new ArrayList<>();
        for (Exercise be : bondExercise) {
            exerciseDates.add(be.getExercise_date());
        }

        String begin_date = bondInfo.getBegin_date();
        String end_date = bondInfo.getEnd_date();
        settleDate = settleDate.replaceAll("-","");

        // 检查参数有效性
        if (begin_date.compareTo(end_date) >= 0) {
            throw new CALCException("日期有误！");
        } else if (netPrice.compareTo(new BigDecimal(200)) > 0 || netPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CALCException("净价不能小于0或大于200！");
        }
        // MODDuration-修正久期
        BigDecimal MODDuration = new BigDecimal("0.0");
        // Duration-久期
        BigDecimal Duration = getDuration(settleDate, isDoExercise, netPrice, bondInfo);
        //如果久期等于零，返回0
        if (Duration.compareTo(new BigDecimal("0.0")) == 0) {
            return MODDuration;
        }
        // y-到期收益率
        BigDecimal y = PriceAndYtm.getYTMByRealPrice(bondInfo, netPrice, settleDate);
        // Y=1+y/100
        BigDecimal Y = y.divide(HUNDRED, SCALE, BigDecimal.ROUND_HALF_UP).add(
                BigDecimal.ONE);
        // MODDuration =duration/(1+y/100)
        MODDuration = Duration.divide(Y, 16, BigDecimal.ROUND_HALF_UP);
        return MODDuration;
    }

    /**
     * 资产凸性
     *
     * @param netPrice      百元净价
     * @param settleDate    结算日，即输入的结算日期
     * @param isDoExercise 是否行权，1--行权，0--不行权
     * @param bondInfo      债券信息
     * @return 资产凸性
     * @throws ParseException 传入的日期类型错误时抛出的异常
     * @throws CALCException  以下四种种情况抛出该异常：(1)资产类型不是债券，或者算法不是中债算法抛出该异常 (2)传入的结算时间不在债券周期范围内
     *                        (3)净价不能小于0或大于200 (4)输入的起息日大于等于到期日
     */
    public static BigDecimal getConvexity(BigDecimal netPrice, String settleDate, int isDoExercise,
                                          BondInfo bondInfo) throws ParseException, CALCException {

        int bond_type = 1;
        int arithmetic_type = 1;
        int interest_type = bondInfo.getInterest_type();
        settleDate = settleDate.replaceAll("-","");
        //资金流
        List<Cashflow> cashFlows = bondInfo.getCashflowList();
        //行权信息
        List<Exercise> bondExercise = bondInfo.getBondexerciseList();
        // 查询的时候已经按照日期排序
        List<String> exerciseDates = new ArrayList<>();
        for (Exercise be : bondExercise) {
            exerciseDates.add(be.getExercise_date());
        }

        int is_exercise = bondInfo.getIs_exercise();
        int pay_freq = bondInfo.getPay_freq();
        String begin_date = bondInfo.getBegin_date();
        String end_date = bondInfo.getEnd_date();

        // 检查参数有效性
        if (settleDate.compareTo(begin_date) < 0
                || settleDate.compareTo(end_date) > 0) {
            throw new CALCException(CALCLogMsg.DateNotInRange.getMsg());
        } else if (begin_date.compareTo(end_date) >= 0) {
            throw new CALCException(CALCLogMsg.DateError.getMsg());
        } else if (netPrice.compareTo(new BigDecimal(200)) > 0 || netPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CALCException(CALCLogMsg.NetPriceIllegal.getMsg());
        }
        // Convexity--资产凸性
        BigDecimal Convexity = new BigDecimal("0.0");
        if (bond_type == 1 && (arithmetic_type == 1 || arithmetic_type == 2)) {
            switch (interest_type) {
                case 3:
                    return Convexity;
                case 2:
                    return Convexity;
                default:
                    if (isDoExercise != 0) {// 行权
                        // 如果是含权债，改变结算日。否则，返回0；
                        if (is_exercise == 1) {
                            end_date = changeEndDate(exerciseDates);
                        } else {
                            return new BigDecimal(0);
                        }
                    }
                    // y--到期收益率
                    BigDecimal y = PriceAndYtm.getYTMByRealPrice(bondInfo, netPrice, settleDate);
                    // pay_date 付息日，由传入的date参数确定
                    // 循环资金流，确定当前pay_date
                    // 确定当前票面利率
                    Map<String, Object> map = getCash(cashFlows, settleDate);
                    String pay_date = (String) map.get("pay_date");
                    // Ct--票面利率/付息频率
                    BigDecimal rate = new BigDecimal(map.get("rate").toString());
                    BigDecimal freq = new BigDecimal(Integer.toString(DateUtils
                            .getYearFreq(pay_freq)));
                    BigDecimal Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP);
                    // t （下一次付息日-计算日期）/365
                    BigDecimal t = new BigDecimal("0.0");
                    BigDecimal a;
                    int k;
                    BigDecimal sum1 = new BigDecimal("0.0"),
                            sum2 = new BigDecimal("0.0");
                    BigDecimal s1, s2;

                    if (freq.intValue() == 1) {
                        // 付息日至到期日之间的年整数
                        k =  ((DateUtils.DateApart(pay_date, end_date)) / YEARS);
                        a = getDate(settleDate, pay_date);
                        for (int i = 0; i < k; i++) {
                            if (i == 0) {// 第一年
                                t = a
                                        .divide(YDATE, SCALE,
                                                BigDecimal.ROUND_HALF_UP);
                            } else {// 第二年至到期前的整年数内
                                t = t.add(BigDecimal.ONE);
                            }
                            // s1=c/(1+y/100)^t
                            s1 = formulaCon_1(Ct, t, y);
                            // sum1=s1累加
                            sum1 = sum1.add(s1);
                            // s2=c*t*(t+1)/(1+y/100)^t;
                            s2 = formulaCon_2(Ct, t, y);
                            // sum2=s2的累加
                            sum2 = sum2.add(s2);
                        }
                        // 最后一期，大于整年数的部分
                        // Ct=rate/付息频率+100
                        Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP).add(HUNDRED);
                        // t=a/365+k
                        t = a.divide(YDATE, SCALE, BigDecimal.ROUND_HALF_UP).add(
                                new BigDecimal(k));
                        // s1=c/(1+y/100)^t
                        s1 = formulaCon_1(Ct, t, y);
                        // sum1=s1累加
                        sum1 = sum1.add(s1);
                        // s2=c*t*(t+1)/(1+y/100)^t;
                        s2 = formulaCon_2(Ct, t, y);
                        // sum2=s2的累加
                        sum2 = sum2.add(s2);
                        // convecity=(1/(1+y/100)^2)*(sum2/sum1)
                        // x=1+y/100)^2
                        double x = Math.pow((y.divide(HUNDRED, SCALE,
                                BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE)
                                .doubleValue()), 2);
                        if (sum1.compareTo(new BigDecimal("0.0")) == 0) {
                            return new BigDecimal(0);
                        } else {
                            // sum=sum2/sum1
                            BigDecimal sum = sum2.divide(sum1, SCALE,
                                    BigDecimal.ROUND_HALF_UP);
                            // convexity=(1/x)*sum
                            Convexity = BigDecimal.ONE.divide(
                                    new BigDecimal(Double.toString(x)), SCALE,
                                    BigDecimal.ROUND_HALF_UP).multiply(sum);

                        }
                    } else {
					/*//算法待定
					return new BigDecimal("0");
					*/

                        // pay-该List是从当前付息日开始，至到期日之间的所有付息日
                        List<String> pay = getPayDate(settleDate, cashFlows);
                        for (int i = 0; i < pay.size(); i++) {// 将pay中的付息日循环
                            if (i < pay.size() - 1) {
                                // 循环付息日，确定pay_date，以计算出t
                                pay_date = pay.get(i);
                                // a-得到BigDecimal类型的两个日期之间的天数
                                a = getDate(settleDate, pay_date);
                                // t=a/365
                                t = a.divide(YDATE, SCALE,
                                        BigDecimal.ROUND_HALF_UP);
                                // s1=c/(1+y/100)^t
                                s1 = formulaCon_1(Ct, t, y);
                                // sum1=s1累加
                                sum1 = sum1.add(s1);
                                // s2=c*t*(t+1)/(1+y/100)^t;
                                s2 = formulaCon_2(Ct, t, y);
                                // sum2=s2的累加
                                sum2 = sum2.add(s2);
                            } else if (i == pay.size() - 1) {// 最后一次付息
                                // Ct=rate/付息频率+100
                                Ct = rate.divide(freq, 16, BigDecimal.ROUND_HALF_UP).add(HUNDRED);
                                pay_date = pay.get(i);
                                // a-得到BigDecimal类型的两个日期之间的天数
                                a = getDate(settleDate, pay_date);
                                // t=a/365
                                t = a.divide(YDATE, SCALE,
                                        BigDecimal.ROUND_HALF_UP);
                                // s1=c/(1+y/100)^t
                                s1 = formulaCon_1(Ct, t, y);
                                // sum1=s1累加
                                sum1 = sum1.add(s1);
                                // s2=c*t*(t+1)/(1+y/100)^t;
                                s2 = formulaCon_2(Ct, t, y);
                                // sum2=s2的累加
                                sum2 = sum2.add(s2);
                            }
                        }
                        // convecity=(1/(1+y/100)^2)*(sum2/sum1)
                        // x=1+y/100)^2
                        double x = Math.pow((y.divide(HUNDRED, SCALE,
                                BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE)
                                .doubleValue()), 2);
                        // sum=sum2/sum1
                        if (sum1.compareTo(new BigDecimal("0.0")) == 0) {
                            return new BigDecimal("0");
                        } else {
                            BigDecimal sum = sum2.divide(sum1, SCALE,
                                    BigDecimal.ROUND_HALF_UP);
                            // convexity=(1/x)*sum
                            Convexity = BigDecimal.ONE.divide(
                                    new BigDecimal(Double.toString(x)), SCALE,
                                    BigDecimal.ROUND_HALF_UP).multiply(sum);
                        }
                    }
                    return Convexity.divide(BigDecimal.ONE, SCALE,
                            BigDecimal.ROUND_HALF_UP);
            }

        } else {
            throw new ArithmeticException("该类型暂未录入");
        }
    }

    /**
     * pvbp
     *
     * @param netPrice     百元净价
     * @param isDoExercise 是否行权，1--行权，0--不行权
     * @param bondInfo     债券信息
     * @return pvbp
     * @throws ParseException 传入的日期类型错误时抛出的异常
     * @throws CALCException  以下四种种情况抛出该异常：(1)资产类型不是债券，或者算法不是中债算法抛出该异常 (2)传入的结算时间不在债券周期范围内
     *                        (3)净价不能小于0或大于200 (4)输入的起息日大于等于到期日
     */
    public static BigDecimal getPvbp(BigDecimal netPrice, String settleDate, int isDoExercise,
                                     BondInfo bondInfo) throws CALCException, ParseException {
        settleDate = settleDate.replaceAll("-","");
        //修正久期
        BigDecimal modDuration = getMODDuration(netPrice, settleDate, isDoExercise, bondInfo);
        //全价
        BigDecimal fullPrice = LogicPrice.NetPriceToFullPrice(netPrice, settleDate, bondInfo);
        //pvbp = 修正久期*0.0001*全价
        BigDecimal pvbp = modDuration.multiply(new BigDecimal(0.0001)).multiply(
                fullPrice).divide(new BigDecimal(1), 16, BigDecimal.ROUND_HALF_UP);
        return pvbp;
    }

    /**
     * 公式result= c*t/(1+y/100)^t
     *
     * @param c 票面利率/付息频率
     * @param t （下一次付息日-计算日期）/365
     * @param y 到期收益率
     * @return BigDecimal
     */
    private static BigDecimal formulaDur(BigDecimal c, BigDecimal t,
                                         BigDecimal y) {
        BigDecimal result = new BigDecimal("0.0");
        double Y = Math.pow((y.divide(new BigDecimal("100"), 16,
                BigDecimal.ROUND_HALF_UP).add(new BigDecimal("1"))
                .doubleValue()), t.doubleValue());
        String reg = "^-?\\d+\\.?\\d*$";
        if (!Double.toString(Y).matches(reg)) {
            return result;
        }
        result = c.multiply(t).divide(new BigDecimal(Double.toString(Y)), 16,
                BigDecimal.ROUND_HALF_UP);
        return result;
    }

    /**
     * 公式 result=c*t*(t+1)/(1+y/100)^t
     *
     * @param c 票面利率/付息频率
     * @param t （下一次付息日-计算日期）/365
     * @param y 到期收益率
     * @return BigDecimal
     */
    private static BigDecimal formulaCon_2(BigDecimal c, BigDecimal t,
                                           BigDecimal y) {
        BigDecimal result = new BigDecimal("0.0");
        double Y = Math.pow((y.divide(new BigDecimal("100"), 16,
                BigDecimal.ROUND_HALF_UP).add(new BigDecimal("1"))
                .doubleValue()), t.doubleValue());
        String reg = "^-?\\d+\\.?\\d*$";
        if (!Double.toString(Y).matches(reg)) {
            return result;
        }
        result = c.multiply(t).multiply(t.add(new BigDecimal("1"))).divide(
                new BigDecimal(Double.toString(Y)), 16,
                BigDecimal.ROUND_HALF_UP);
        return result;
    }

    /**
     * 公式 result=c/(1+y/100)^t
     *
     * @param c 票面利率/付息频率
     * @param t （下一次付息日-计算日期）/365
     * @param y 到期收益率
     * @return BigDecimal
     */
    private static BigDecimal formulaCon_1(BigDecimal c, BigDecimal t,
                                           BigDecimal y) {
        BigDecimal result = new BigDecimal("0.0");
        double Y = Math.pow((y.divide(new BigDecimal("100"), 16,
                BigDecimal.ROUND_HALF_UP).add(new BigDecimal("1"))
                .doubleValue()), t.doubleValue());
        String reg = "^-?\\d+\\.?\\d*$";
        if (!Double.toString(Y).matches(reg)) {
            return result;
        }
        result = c.divide(new BigDecimal(Double.toString(Y)), 16,
                BigDecimal.ROUND_HALF_UP);
        return result;
    }

    /**
     * 当含权债行权时，选择合适的结算时间
     *
     * @param dates 行权时间
     * @return  String
     */
    private static String changeEndDate(List<String> dates) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        if (dates.size() == 1) {
            return dates.get(0);
        } else {
            List<Integer> list = new ArrayList<>();
            for (String s : dates) {
                list.add(Integer.parseInt(s));
            }
            int d = Integer.parseInt(date);
            Collections.sort(list);
            for (Integer i : list) {
                if (i >= d) {
                    return i.toString();
                }
            }
            return list.get(list.size() - 1).toString();
        }
    }

    /**
     * 得到两个日期之间的BigDecimal类型的天数
     *
     * @param settle_date 第一个日期
     * @param pay_date    第二个日期
     * @return 第二个日期至第一个日期的天数
     * @throws ParseException 传入的String不符合日期格式的时候，抛出的转换异常
     */
    private static BigDecimal getDate(String settle_date, String pay_date)
            throws ParseException {
        int date = DateUtils.DateApart(settle_date, pay_date);
        return new BigDecimal(date);
    }

    /**
     * 得到一个从settle_date对应的当期付息日至到期日之间的所有付息日的List集合
     *
     * @param settle_date 计算日期
     * @param list        单位资金流的list
     * @return 从settle_date对应的当期付息日至到期日之间的所有付息日的集合
     */
    private static List<String> getPayDate(String settle_date,
                                           List<Cashflow> list) {
        // 计算日期小于付息日
        List<String> result = new ArrayList<>();
        for (Cashflow cashflow : list) {
            if (cashflow.getChange_id() == 10) {
                if (cashflow.getPay_date().compareTo(settle_date) > 0) {
                    String pay_date = cashflow.getPay_date();
                    result.add(pay_date);
                }
            }
        }
        return result;
    }

    /**
     * 根据计算日期定位在资金流中的一个周期数据，包括票面利率、开始日期、发生日期
     *
     * @param list        单位资金流
     * @param settle_date 计算日期
     * @return 根据settle_date得到的list资金流中的一条数据，包括票面利率、开始日期、发生日期
     */
    private static Map<String, Object> getCash(List<Cashflow> list,
                                               String settle_date) {
        Map<String, Object> map = new HashMap<>();
        String start_date, pay_date;
        for (Cashflow cashflow : list) {
            if (cashflow.getChange_id() != 10) {
                continue;
            }
            // 计算日期大于或等于开始日期,小于兑付日
            if (settle_date.compareTo(cashflow.getStart_date()) >= 0
                    && cashflow.getPay_date().compareTo(settle_date) > 0) {
                start_date = cashflow.getStart_date();
                pay_date = cashflow.getPay_date();
                String rate = cashflow.getCoupon_rate();
                map.put("start_date", start_date);
                map.put("pay_date", pay_date);
                map.put("rate", rate);
            }
        }
        return map;
    }

    public static void main(String[] args) {

    }
}
