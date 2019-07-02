package com.kayak.cloud.algorithm.logic;


import com.kayak.cloud.algorithm.comm.*;
import com.kayak.cloud.algorithm.model.BondInfo;
import com.kayak.cloud.algorithm.model.Cashflow;
import com.kayak.cloud.algorithm.model.Exercise;
import com.kayak.cloud.algorithm.model.FixedIncome;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 该类用于算法：
 *      1.全价推收益率：GetYTMByFullPrice
 *      2.收益率推全价：GetFullPriceByYTM
 *      3.净价推收益率：getYTMByRealPrice
 *      4.收益率推净价：YTMToNetDateUtils
 */
public class PriceAndYtm {
    /**
     * 日期字符格式转换
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");
    /**
     * 二分法循环的次数
     */
    private static final int TIMES = 100;
    /**
     * 使用二分法时控制精度
     */
    private static final BigDecimal ACCURACY = new BigDecimal("0.0000001");

    /**
     * 全价推收益率算法
     * 1.对处于最后付息周期的固定利率债券、待偿期在一年及以内的到期一次还本付息零息债和贴现债券，到期收益率按单利计算
     * 2.对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算
     * 3.对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 4.对不处于最后周期的浮息债，到期收益率的计算
     * 5.对不处于最后周期的提前还本固定利率债券，到期收益率的计算
     *
     * @param  fullPrice       全价
     * @param  settleDate      结算日期
     * @param  bondInfo        债券信息
     *         List<CashFlows> 现金流
     *         List<Exercise>  行权信息
     *         begin_date	   起息日
     *         end_date        到期兑付日
     *         pay_freq        付息频率
     *         is_exercise     是否含权：1--含权，0--不含权
     *         interest_type   债券的品种：1--附加利息，2--零息，3--贴现
     *         interest_sort   付息利率品种：1--浮动利率，2--固定利率
     *         interestMode    计息模式：1--分段计息，2--逐日计息
     *
     * @return BigDecimal      收益率
     *
     * @throws ParseException  日期格式错误
     * @throws CALCException   计算异常
     */
    public static BigDecimal getYTMByFullPrice(BondInfo bondInfo,BigDecimal fullPrice,String settleDate) throws ParseException,CALCException {
        //返回值
        BigDecimal ret = new BigDecimal("0");
        //现金流
        List<Cashflow> cashFlows =  Optional.of(bondInfo).map(FixedIncome::getCashflowList)
                .orElseThrow(() -> new CALCException("债券信息里【现金流列表】字段不能为空！"));
        //行权信息
        List<Exercise> bondExercise = bondInfo.getBondexerciseList();
        //是否含权:0——不含权 1——含权
        int haveExercise = Optional.of(bondInfo).map(BondInfo::getIs_exercise)
                .orElseThrow(() -> new CALCException("债券信息里【含权】字段不能为空！"));
        //付息频率:1—— 按月付息 2—— 按季付息 3——半年付息 4—— 按年付息 5——到期付息
        int payFreq = Optional.of(bondInfo).map(BondInfo::getPay_freq)
                .orElseThrow(() -> new CALCException("债券信息里【付息频率】字段不能为空！"));
        //起息日
        String beginDate = Optional.of(bondInfo).map(BondInfo::getBegin_date)
                .orElseThrow(() -> new CALCException("债券信息里【起息日】字段不能为空！")).replaceAll("-","");
        //到期日
        String endDate = Optional.of(bondInfo).map(BondInfo::getEnd_date)
                .orElseThrow(() -> new CALCException("债券信息里【到期日】字段不能为空！")).replaceAll("-","");
        //息票品种:1——附加息 2——零息 3——贴现
        int interestType = Optional.of(bondInfo).map(BondInfo::getInterest_type)
                .orElseThrow(() -> new CALCException("债券信息里【息票品种】字段不能为空！"));
        //附息利率品种:1——浮动利率 2——固定利率 3——累进利率
        int interestSort = Optional.of(bondInfo).map(BondInfo::getInterest_sort)
                .orElseThrow(() -> new CALCException("息票品种【附息利率品种】字段不能为空！"));
        //计息基础 上一年的计息天数：1.为实际天数，2.为360天
        int bondBaseday = Optional.of(bondInfo).map(BondInfo::getBond_baseday)
                .orElseThrow(() -> new CALCException("息票品种【计息基础】字段不能为空！"));
        //计息模式：1为分段计息，2为逐日计息
        //int interestMode = Optional.ofNullable(bondInfo).map(BondInfo::getInterest_mode).orElseThrow(() -> new CALCException("息票品种【计息模式】字段不能为空！"));

        settleDate = settleDate.replaceAll("-","");
        if (interestType==2||interestType==3) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(SDF.parse(settleDate));
            calendar.add(Calendar.DAY_OF_MONTH, +1);
            settleDate=SDF.format(calendar.getTime());
        }

        //如果行权
        if (bondExercise!=null && bondExercise.size() > 0) {
            // 查询的时候已经按照日期排序
            List<String> exerciseDates = new ArrayList<>();
            for (Exercise be : bondExercise) {
                exerciseDates.add(be.getExercise_date());
            }
            //含权债，改变结算日
            if (haveExercise == 1) {
                String exerciseDate = changeEndDate(exerciseDates);
                if (exerciseDate != null) {
                    endDate = exerciseDate;
                }
            }
            //不含权债，返回0
            if (haveExercise != 1) {
                return ret;
            }
        }

        //检查参数有效性
        if (settleDate.compareTo(beginDate) < 0 || settleDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateNotInRange.getMsg());
        } else if (beginDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateError.getMsg());
        } else if (fullPrice.doubleValue() <= 0) {
            throw new CALCException(CALCLogMsg.FullPriceLessZero.getMsg());
        }

        //获取交易后债券理论付息日、票面利率和剩余本金
        Map<String, Object> map = countPrepAndNextDate(settleDate, cashFlows, interestType);
        //用于判断走哪个算法
        int pd = pand(interestType, interestSort, endDate, settleDate, cashFlows);
        System.out.println(pd);
        switch (pd) {
            case 1:
                //对处于最后付息周期的固定利率债券、待偿期在一年及以内的到期一次还本付息零息债和贴现债券，到期收益率按单利计算
                //核心算法
                ret = calcYTMByFullPrice_1(interestType, interestSort, beginDate, endDate, settleDate, payFreq, fullPrice, map, bondInfo);
                break;
            case 2:
                //对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算。
                ret = calcYTMByFullPrice_2(interestType, interestSort, beginDate, endDate, settleDate, payFreq, fullPrice, map, bondInfo);
                break;
            case 3:
                //对不处于最后付息周期的固定利率债券，到期收益率按复利计算
                ret = calcYTMByFullPrice_3(interestType, endDate, settleDate, payFreq, fullPrice, cashFlows, bondInfo);
                break;
            case 4:
                //对不处于最后周期的浮息债，到期收益率的计算
                ret = calcYTMByFullPrice_4(interestType, endDate, settleDate, payFreq, fullPrice, cashFlows, bondInfo);
                break;
            case 5:
                //对不处于最后周期的固定利率债券，提前还本的计算
                ret = calcYTMByFullPrice_5(interestType, endDate, settleDate, payFreq, bondBaseday, fullPrice, cashFlows);
                break;
            default:
                throw new CALCException(CALCLogMsg.BondTypeMatchFail.getMsg());
        }
        return ret;
    }

    /**
     * 收益率推全价
     * 1.对处于最后付息周期的固定利率债券、待偿期在一年及以内的零息债和贴现债券、处于最后付息周期的浮息债，到期收益率按单利计算
     * 2.对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算。
     * 3.对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 4.对不处于最后付息周期的浮息债，到期收益率按复利计算
     *
     * @param  settleDate      结算日期
     * @param  ytm             收益率
     * @param  bondInfo        债券信息
     *         List<CashFlows> 现金流
     *         List<Exercise>  行权信息
     *         exercise_date   行权日期
     *         begin_date	   起息日
     *         end_date        到期兑付日
     *         pay_freq        付息频率
     *         is_exercise     是否含权：1--含权，0--不含权
     *         interest_type   债券的品种：1--附加利息，2--零息，3--贴现
     *         interest_sort   付息利率品种：1--浮动利率，2--固定利率
     *         interestMode    计息模式：1--分段计息，2--逐日计息
     *
     * @return BigDecimal      全价
     *
     * @throws ParseException  日期格式错误
     * @throws CALCException   计算异常
     */
    public static BigDecimal getFullPriceByYTM(BondInfo bondInfo,BigDecimal ytm,String settleDate) throws ParseException {
        //返回值
        BigDecimal ret = new BigDecimal("0");

        //现金流
        List<Cashflow> cashFlows =  Optional.of(bondInfo).map(FixedIncome::getCashflowList)
                .orElseThrow(() -> new CALCException("债券信息里【现金流列表】字段不能为空！"));
        //行权信息
        List<Exercise> bondExercise = bondInfo.getBondexerciseList();
        //是否含权:0——不含权 1——含权
        int haveExercise = Optional.of(bondInfo).map(BondInfo::getIs_exercise)
                .orElseThrow(() -> new CALCException("债券信息里【含权】字段不能为空！"));
        //付息频率:1—— 按月付息 2—— 按季付息 3——半年付息 4—— 按年付息 5——到期付息
        int payFreq = Optional.of(bondInfo).map(BondInfo::getPay_freq)
                .orElseThrow(() -> new CALCException("债券信息里【付息频率】字段不能为空！"));
        //起息日
        String beginDate = Optional.of(bondInfo).map(BondInfo::getBegin_date)
                .orElseThrow(() -> new CALCException("债券信息里【起息日】字段不能为空！")).replaceAll("-","");
        //到期日
        String endDate = Optional.of(bondInfo).map(BondInfo::getEnd_date)
                .orElseThrow(() -> new CALCException("债券信息里【到期日】字段不能为空！")).replaceAll("-","");
        //息票品种:1——附加息 2——零息 3——贴现
        int interestType = Optional.of(bondInfo).map(BondInfo::getInterest_type)
                .orElseThrow(() -> new CALCException("债券信息里【息票品种】字段不能为空！"));
        //附息利率品种:1——浮动利率 2——固定利率 3——累进利率
        int interestSort = Optional.of(bondInfo).map(BondInfo::getInterest_sort)
                .orElseThrow(() -> new CALCException("债券信息里【附息利率品种】字段不能为空！"));
        settleDate = settleDate.replaceAll("-","");

        //计息基础 上一年的计息天数：1.为实际天数，2.为360天
        //int bondBaseday = Optional.ofNullable(bondInfo).map(BondInfo::getBond_baseday).orElseThrow(() -> new CALCException("债券信息里【计息基础】字段不能为空！"));
        //计息模式：1为分段计息，2为逐日计息
        //int interestMode = Optional.ofNullable(bondInfo).map(BondInfo::getInterest_mode).orElseThrow(() -> new CALCException("债券信息里【计息模式】字段不能为空！"));

        if (bondExercise.size() > 0) {
            // 查询的时候已经按照日期排序
            List<String> exerciseDates = new ArrayList<>();
            for (Exercise be : bondExercise) {
                exerciseDates.add(be.getExercise_date());
            }
            //如果是含权债，改变结算日。否则，返回0；
            if (haveExercise == 1) {
                endDate = changeEndDate(exerciseDates);
            } else {
                return ret;
            }
        }

        //检查参数有效性
        if (settleDate.compareTo(beginDate) < 0 || settleDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateNotInRange.getMsg());
        } else if (beginDate.compareTo(endDate) >= 0) {
            throw new CALCException(CALCLogMsg.DateError.getMsg());
        }

        //收益率以百分数表示的，要除以100
        ytm = ytm.divide(new BigDecimal(100), 16, RoundingMode.HALF_UP);

        //资金流解析的map数据
        Map<String, Object> map = countPrepAndNextDate(settleDate, cashFlows, interestType);
        //判断债券的种类，由不同的算法计算全价
        int type = pand(interestType, interestSort, endDate, settleDate, cashFlows);
        switch (type) {
            case 1:
                ret = calcFullPriceByYTM_1(interestType, interestSort, beginDate, endDate, settleDate, payFreq, ytm, map);
                break;
            case 2:
                ret = calcFullPriceByYTM_2(interestType, interestSort, beginDate, endDate, settleDate, payFreq, ytm, map);
                break;
            case 3:
                ret = calcFullPriceByYTM_3(interestType, endDate, settleDate, payFreq, ytm, cashFlows);
                break;
            case 4:
                ret = calcFullPriceByYTM_4(interestType, endDate, settleDate, payFreq, ytm, cashFlows);
                break;
            default:
                throw new CALCException(CALCLogMsg.BondTypeMatchFail.getMsg());
        }

        return ret;
    }

    /**
     * 净价推收益率
     * @param  settleDate      结算日期
     * @param  netPrice        净价
     * @param  bondInfo        债券信息
     *         List<CashFlows> 现金流
     *         List<Exercise>  行权信息
     *         begin_date	   起息日
     *         end_date        到期兑付日
     *         pay_freq        付息频率
     *         is_exercise     是否含权：1--含权，0--不含权
     *         interest_type   债券的品种：1--附加利息，2--零息，3--贴现
     *         interest_sort   付息利率品种：1--浮动利率，2--固定利率
     *         interestMode    计息模式：1--分段计息，2--逐日计息
     *
     * @return BigDecimal      收益率
     *
     * @throws ParseException  日期格式错误
     * @throws CALCException   计算异常
     */
	public static BigDecimal getYTMByRealPrice
            (BondInfo bondInfo,BigDecimal netPrice,String settleDate) throws ParseException,CALCException {
		//净价推全价
	    BigDecimal fullPrice =  LogicPrice.NetPriceToFullPrice( netPrice, settleDate,bondInfo);
        fullPrice = fullPrice.setScale(4, RoundingMode.HALF_UP);
        settleDate = settleDate.replaceAll("-","");
        //全价推收益率
		return getYTMByFullPrice(bondInfo,fullPrice,settleDate);
	}

    /**
     * 收益率推净价
     * @param  settleDate      结算日期
     * @param  ytm             收益率
     * @param  bondInfo        债券信息
     *         List<CashFlows> 现金流
     *         List<Exercise>  行权信息
     *         begin_date	   起息日
     *         end_date        到期兑付日
     *         pay_freq        付息频率
     *         is_exercise     是否含权：1--含权，0--不含权
     *         interest_type   债券的品种：1--附加利息，2--零息，3--贴现
     *         interest_sort   付息利率品种：1--浮动利率，2--固定利率
     *         interestMode    计息模式：1--分段计息，2--逐日计息
     *
     * @return BigDecimal      净价
     *
     * @throws ParseException  日期格式错误
     * @throws CALCException   计算异常
     */
	public static BigDecimal getRealPriceByYTM
    (BondInfo bondInfo,BigDecimal ytm,String settleDate) throws ParseException,CALCException{
		//推出全价
		BigDecimal fullPrice = getFullPriceByYTM(bondInfo,ytm,settleDate);
        settleDate = settleDate.replaceAll("-","");
        //全价推净价
		return LogicPrice.FullPriceToNetPrice( fullPrice, settleDate,bondInfo);
	}

    /**
     * 针对处于最后付息周期的固定利率债券、待偿期在一年及以内的零息债和贴现债券，按单利计算
     * 公式：y=(FV-PV)/PV/(d/365) 或 Y=(FV-PV)/PV/(D/TY)
     * 变量：
     * y ：到期收益率
     * D ：债券结算日至到期兑付日的实际天数
     * FV ：到期兑付日债券本息和，固定利率债券
     * PV ：债券全价
     * TY :当前计息年度的实际天数，算头不算尾
     *
     * @param interestType 息票品种
     * @param interestSort 附息利率品种
     * @param beginDate    起息日期
     * @param endDate      到期兑付日
     * @param settleDate   结算日期
     * @param payFreq      付息频率
     * @param fullPrice    全价
     * @param map          资金流信息
     * @param bondInfo     债券基本信息
     * @return BigDecimal  收益率
     * @throws ParseException 日期格式错误
     */
    private static BigDecimal calcYTMByFullPrice_1
    (int interestType, int interestSort, String beginDate, String endDate, String settleDate,
     int payFreq, BigDecimal fullPrice, Map<String, Object> map, BondInfo bondInfo) throws ParseException {
        //利率
        BigDecimal couponRate = (BigDecimal) map.get("coupon_rate");

        //本息和
        BigDecimal fv = doFv(interestType, interestSort, couponRate, payFreq, beginDate, endDate, settleDate);
        fv = fv.setScale(16, RoundingMode.HALF_UP);
        System.out.println("fv:" + fv);

        //剩余本金
        BigDecimal end_money = (BigDecimal) map.get("end_money");
        BigDecimal hundred = new BigDecimal("100");
        //对于有提前还本的数据，如果剩余本金小于100，则需要打折处理
        if (end_money != null && end_money.compareTo(hundred) < 0) {
            fv = fv.multiply(end_money.divide(hundred, 16, BigDecimal.ROUND_UP));
        }

        //d债券结算日至到期兑付日的实际天数
        BigDecimal d = new BigDecimal(Double.valueOf(DateUtils.DateApartOneDay(settleDate, endDate)).toString());

        //ty当前计息年度的实际天数，算头不算尾,doTy方法暂时未定
        BigDecimal ty = new BigDecimal(DateUtils.setTY1(beginDate, endDate, settleDate).toString());

        //若计息基础为1：实际天数，为2且整一年则为360天
        if (bondInfo.getBond_baseday() == 2 && (ty.compareTo(new BigDecimal("365")) == 0 || ty.compareTo(new BigDecimal("366")) == 0)) {
            ty = new BigDecimal("360");
        }
        //(fv-fullDateUtils)/fullDateUtils/(d/ty)*100
        return ((fv.subtract(fullPrice)).divide(fullPrice, 16, RoundingMode.HALF_UP)).divide(d.divide(ty, 16, RoundingMode.HALF_UP), 16, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    /**
     * 对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算
     * 公式：PV=FV/[(1+y)^{(d/365)+m}] 或 PV=FV/[(1+y)^{(d/TY)+m}]
     * 变量：
     * d ：债券结算日至到期兑付日的实际天数
     * y ：到期收益率
     * m ：结算日至到期兑付日的整年数
     * FV ：到期兑付日债券本息和，固定利率债券
     * PV ：债券全价
     * TY :当前计息年度的实际天数，算头不算尾
     *
     * @param interestType 息票品种
     * @param interestSort 附息利率品种
     * @param beginDate    起息日期
     * @param endDate      到期兑付日
     * @param date         结算日期
     * @param payFreq      付息频率
     * @param fullPrice    全价
     * @param map          资金流信息
     * @param bondInfo     债券基本信息
     * @return BigDecimal  收益率
     * @throws ParseException 日期格式错误
     */
    private static BigDecimal calcYTMByFullPrice_2(int interestType, int interestSort, String beginDate, String endDate, String date,
                                                   int payFreq, BigDecimal fullPrice, Map<String, Object> map, BondInfo bondInfo) throws ParseException,CALCException {
        //每百元利息
        BigDecimal couponRate = (BigDecimal) map.get("coupon_rate");
        //本息和
        BigDecimal fv = doFv(interestType, interestSort, couponRate, payFreq, beginDate, endDate);
        fv = fv.setScale(16, RoundingMode.HALF_UP);

        //d债券结算日至到下一理论付息日的实际天数,此处的方法应该借助资金流的信息去计算
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(Double.valueOf(DateUtils.DateApartOneDay(date, nextDate)).toString());

        //m：结算日至到期兑付日的整年数；
        BigDecimal m = new BigDecimal(countYear(date, endDate));

        //ty当前计息年度的实际天数，算头不算尾
        BigDecimal ty = new BigDecimal(DateUtils.setTY(beginDate, date).toString());
        //若bond_baseday类型为1则为实际天数，类型为2且整一年则为360天
        if (bondInfo.getBond_baseday() == 2 && (ty.compareTo(new BigDecimal("365")) == 0 || ty.compareTo(new BigDecimal("366")) == 0)) {
            ty = new BigDecimal("360");
        }

        //返回的是百分比
       // return getYTMByDichotomy2(fv, d, m, ty, fullPrice).multiply(new BigDecimal(100));
        return countFullPriceNR(fv, d, m, ty, fullPrice).multiply(new BigDecimal(100));
    }

    /**
     * 对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 公式：PV=C/f/(1+y/f)^(d/ts)+C/f/(1+y/f)^(d/ts+1)……+C/f/(1+y/f)^[(d/ts)+n-1]+M/(1+y/f)^[(d/ts)+n-1]
     * @param interestType 债券品种
     * @param endDate      到期日期
     * @param settleDate   结算日期
     * @param payFreq      付息频率
     * @param fullPrice    全价
     * @param bondInfo     债券信息
     * @return 返回收益率的百分比
     * @throws ParseException 日期格式错误
     */
    private static BigDecimal calcYTMByFullPrice_3(int interestType, String endDate, String settleDate, int payFreq,
                                                   BigDecimal fullPrice, List<Cashflow> cashFlows, BondInfo bondInfo) throws ParseException,CALCException {
        //付息频率
        BigDecimal f = new BigDecimal(DateUtils.getYearFreq(payFreq));
        //计算结算区间的付息时间的Map
        Map<String, Object> map = countPrepAndNextDate(settleDate, cashFlows, interestType);
        //结算日到下一最近付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(settleDate, nextDate));
        //上一个付息日与下一个付息日的实际天数
        String prepDate = map.get("start_date").toString();
        BigDecimal ts = new BigDecimal(DateUtils.DateApartOneDay(prepDate, nextDate));
        //1:实际天数，2且整一年:360天
        if (bondInfo.getBond_baseday() == 2 && (ts.compareTo(new BigDecimal("365")) == 0 || ts.compareTo(new BigDecimal("366")) == 0)) {
            ts = new BigDecimal("360");
        }
        //年利率,付息次数存入一个Map中,结算日开始后的付息次数
        int n = DateUtils.countPayTimes(nextDate, endDate, payFreq);
        //计算从结算开始后每次付息的现金流，若interest_mode=1为分段计息，2为逐日计息
        //暂时不区分段计息和逐日计息interestMode
        Map<Integer, BigDecimal> cashMoney ;
        switch (1) {
            case 1:
                cashMoney = countCashMoney_1(cashFlows, nextDate, payFreq, n);
                break;
            default:
                cashMoney = countCashMoney_2(cashFlows, nextDate, n);
                break;
        }
        return getYTMByDichotomy3(cashMoney, f, d, ts,fullPrice).multiply(new BigDecimal(100));
    }

    /**
     * 对不处于最后付息周期的附息式浮动利率债券计算
     *  公式：PV=C/f/(1+y/f)^(d/ts)+C/f/(1+y/f)^(d/ts+1)……+C/f/(1+y/f)^[(d/ts)+n-1]+M/(1+y/f)^[(d/ts)+n-1]
     *  此处C=R1+r R1:最近起息日的基准利率 r:发行时的基本利差
     *     y=R2+s R2:下一息期的基准利率是 s:期望的收益率点差
     * @param interestType 债券品种
     * @param endDate      到期日期
     * @param settleDate   结算日期
     * @param payFreq      付息频率
     * @param fullPrice    全价
     * @param bondInfo     债券信息
     * @return 返回收益率的百分比
     * @throws ParseException 日期格式错误
     */
    private static BigDecimal calcYTMByFullPrice_4(int interestType,  String endDate, String settleDate,int payFreq,
                                                   BigDecimal fullPrice, List<Cashflow> cashFlows, BondInfo bondInfo) throws ParseException,CALCException {
        //f:付息频率
        BigDecimal f = new BigDecimal(DateUtils.getYearFreq(payFreq));
        //计算结算区间的付息时间的Map
        Map<String, Object> map = countPrepAndNextDate(settleDate, cashFlows, interestType);
        //d:结算日到下一最近付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(settleDate, nextDate));
        //ts:上一个付息日与下一个付息日的实际天数
        String prepDate = map.get("start_date").toString();
        BigDecimal ts = new BigDecimal(DateUtils.DateApartOneDay(prepDate, nextDate));
        //年利率,付息次数存入一个Map中,结算日开始后的付息次数
        if (bondInfo.getBond_baseday() == 2 && (ts.compareTo(new BigDecimal("365")) == 0 || ts.compareTo(new BigDecimal("366")) == 0)) {
            ts = new BigDecimal("360");
        }
        //结算日开始后的付息次数
        int n = DateUtils.countPayTimes(nextDate, endDate, payFreq);

        //Map中key:第几次付息,value:年利率，若interest_mode=1为分段计息，2为逐日计息
        //TODO 暂时不区分段计息和逐日计息interestMode
        Map<Integer, BigDecimal> cashMoney;
        switch (1) {
            case 1:
                cashMoney = countCashMoney_1(cashFlows, nextDate, payFreq, n);
                break;
            default:
                cashMoney = countCashMoney_2(cashFlows, nextDate, n);
                break;
        }

        //TODO 暂时预设一个数值 当前市场的基础利率
        //BondBaseRateUnit.BaseRateSearch(bondInfo, settleDate);
        BigDecimal R = new BigDecimal(4.7470);
        //利差
        BigDecimal r = new BigDecimal(bondInfo.getBond_spread());
        BigDecimal Rr = R.add(r);
        //当期的票面利率
        BigDecimal R0 = new BigDecimal(cashFlows.get(0).getCoupon_rate());

        return getYTMByDichotomy4(cashMoney, f, d, ts, Rr, R0,fullPrice).multiply(new BigDecimal(100));
    }

    /**
     * 对不处于最后付息周期的提前还本的债券计算
     *
     * @param interestType    债券品种
     * @param endDate         到期日期
     * @param settleDate      结算日期
     * @param payFreq         付息频率
     * @param fullPrice       全价
     * @return BigDecimal     返回收益里的百分比
     * @throws ParseException 日期格式错误
     */
    private static BigDecimal calcYTMByFullPrice_5
    (int interestType,  String endDate, String settleDate,int payFreq,int bondBaseday, BigDecimal fullPrice, List<Cashflow> cashFlows) throws ParseException {
        //f:付息频率
        BigDecimal f = new BigDecimal(DateUtils.getYearFreq(payFreq));
        //计算结算区间的付息时间的Map
        Map<String, Object> map = countPrepAndNextDate(settleDate, cashFlows, interestType);
        //d:结算日到下一最近付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(settleDate, nextDate));
        //ts:上一个付息日与下一个付息日的实际天数
        String prepDate = map.get("start_date").toString();
        BigDecimal ts = new BigDecimal(DateUtils.DateApartOneDay(prepDate, nextDate));
        //年利率,付息次数存入一个Map中,结算日开始后的付息次数
        if (bondBaseday == 2 && (ts.compareTo(new BigDecimal("365")) == 0 || ts.compareTo(new BigDecimal("366")) == 0)) {
            ts = new BigDecimal("360");
        }
        //c:年利率,付息次数存入一个Map中
        //结算日开始后的付息次数
        int n = DateUtils.countPayTimes(nextDate, endDate, payFreq);
        //Map中key:第几次付息,value:年利率
        Map<Integer, BigDecimal> cashMoney = countCashMoney_3(cashFlows, nextDate, n);
        //收益率的最大值
        BigDecimal max = new BigDecimal("1.1");
        //收益率的最大允许范围
        BigDecimal maxV = new BigDecimal("1");
        //收益率的最小值
        BigDecimal min = new BigDecimal("-1.1");
        //收益率的最小允许范围
        BigDecimal minV = new BigDecimal("-1");
        //2
        BigDecimal two = new BigDecimal("2");
        //将要确定的收益率
        BigDecimal y = null;
        //将要计算的全价
        BigDecimal pv ;
        BigDecimal full_DateUtils = fullPrice.setScale(4, RoundingMode.HALF_UP);
        for (int i = 0; i < TIMES; i++) {
            y = (max.add(min)).divide(two, 16, RoundingMode.HALF_UP);
            pv = countAheadFull(cashMoney,f,y,d,ts);//推算的全价
            BigDecimal pv_DateUtils = pv.setScale(4, RoundingMode.HALF_UP);
            //将算出的全价和正确的全价进行对比，如果算出的太大，说明插入的数据太小
            if ((pv_DateUtils.subtract(full_DateUtils)).abs().compareTo(ACCURACY) < 0) {
                System.out.println("pv:" + pv);
                break;
            } else if (pv_DateUtils.subtract(full_DateUtils).doubleValue() > 0) {
                min = y;
            } else {
                max = y;
            }
            if (y.compareTo(minV) <= 0 || y.compareTo(maxV) >= 0) {
                System.out.println("超出收益率的计算范围");
                throw new CALCException("超出收益率的计算范围！");
            }
        }
        return y.multiply(new BigDecimal(100));
    }

    /**
     * 计算全价算法1：
     * 对处于最后付息周期的固定利率债券、待偿期在一年及以内的到期一次还本付息零息债和贴现债券，到期收益率按单利计算
     */
    private static BigDecimal calcFullPriceByYTM_1
    (int interestType, int interestSort, String beginDate, String endDate, String settleDate, int payFreq, BigDecimal ytm, Map<String, Object> map) throws ParseException {
        BigDecimal couponRate = (BigDecimal) map.get("coupon_rate");
        //fv本息和
        BigDecimal fv = doFv(interestType, interestSort, couponRate, payFreq, beginDate, endDate, settleDate);
        //对于有提前还本的数据，对fv要进行打折
        BigDecimal end_money = (BigDecimal) map.get("end_money");
        //100
        BigDecimal hundred = new BigDecimal("100");
        if (end_money != null && end_money.compareTo(hundred) < 0) {//如果剩余本金小于100，则需要打折处理
            fv = fv.multiply(end_money.divide(hundred, 16, BigDecimal.ROUND_UP));
        }
        //d结算日到到期日的实际天数
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(settleDate, endDate));
        //ty起息日当年的实际天数
        BigDecimal ty = new BigDecimal(DateUtils.setTY1(beginDate, endDate, settleDate).toString());
        BigDecimal divisor = d.multiply(ytm).divide(ty, 16, RoundingMode.HALF_UP).add(new BigDecimal(1));
        return fv.divide(divisor, 16, RoundingMode.HALF_UP);
    }

    /**
     * 计算全价算法2：
     * 对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算。
     */
    private static BigDecimal calcFullPriceByYTM_2
    (int interestType, int interestSort, String beginDate, String endDate, String date, int payFreq, BigDecimal ytm, Map<String, Object> map)
            throws ParseException {
        BigDecimal couponRate = (BigDecimal) map.get("coupon_rate");
        BigDecimal fv = doFv(interestType, interestSort, couponRate, payFreq, beginDate, endDate);
        //d债券结算日至到下一理论付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(date, nextDate));

        //m：结算日至到期兑付日的整年数；
        BigDecimal m = new BigDecimal(countYear(date, endDate));

        //ty当前计息年度的实际天数，算头不算尾
        BigDecimal ty = new BigDecimal(DateUtils.setTY(beginDate, date).toString());

        return countFullPrice(fv, d, m, ty, ytm);
    }

    /**
     * 计算全价算法3：
     * 对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     */
    private static BigDecimal calcFullPriceByYTM_3(int interestType, String endDate,
                              String date, int payFreq, BigDecimal ytm, List<Cashflow> cashFlows) throws ParseException {
        Map<String, Object> map = countPrepAndNextDate(date, cashFlows, interestType);
        //f:付息频率
        BigDecimal f = new BigDecimal(DateUtils.getYearFreq(payFreq));
        //d:结算日到下一最近付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(date, nextDate));
        //结算日开始后的付息次数
        int n = DateUtils.countPayTimes(nextDate, endDate, payFreq);
        //结算日开始后的付息现金流
        Map<Integer, BigDecimal> cMap = countCashMoney_1(cashFlows, nextDate, payFreq, n);
        //ts:上一个付息日与下一个付息日的实际天数
        String prepDate = map.get("start_date").toString();
        BigDecimal ts = new BigDecimal(DateUtils.DateApartOneDay(prepDate, nextDate));

        return countFullPrice(cMap, f, ytm, d, ts);
    }

    /**
     * 计算全价算法4：
     * 浮息债券的到期收益率推全价的方法
     */
    private static BigDecimal calcFullPriceByYTM_4
    (int interestType, String endDate, String date, int payFreq, BigDecimal ytm, List<Cashflow> cashFlows)
            throws ParseException {
        Map<String, Object> map = countPrepAndNextDate(date, cashFlows, interestType);
        //f:付息频率
        BigDecimal f = new BigDecimal(DateUtils.getYearFreq(payFreq));

        //d:结算日到下一最近付息日的实际天数
        String nextDate = map.get("pay_date").toString();
        BigDecimal d = new BigDecimal(DateUtils.DateApartOneDay(date, nextDate));
        //结算日开始后的付息次数
        int n = DateUtils.countPayTimes(nextDate, endDate, payFreq);
        //结算日开始后的付息现金流
        Map<Integer, BigDecimal> cMap = countCashMoney_1(cashFlows, nextDate, payFreq, n);
        //ts:上一个付息日与下一个付息日的实际天数
        String prepDate = map.get("start_date").toString();
        BigDecimal ts = new BigDecimal(DateUtils.DateApartOneDay(prepDate, nextDate));
        //当前市场的基础利率
//        BigDecimal Rr = BondBaseRateUnit.BaseRateSearch(bondInfo, date);
        BigDecimal Rr = new BigDecimal(4.7470);
        //当期的票面利率
        BigDecimal R0 = new BigDecimal(cashFlows.get(0).getCoupon_rate());
        return countFullPrice(cMap, f, ytm, d, ts, Rr, R0);
    }

    /**
     * 根据债券基础信息自动执行的子类计算方法：
     * 1.对处于最后付息周期的固定利率债券、待偿期在一年及以内的零息债和贴现债券，到期收益率按单利计算
     * 2.对待偿期在一年以上的零息债券和贴现债，到期收益率按复利计算。
     * 3.对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 4.对不处于最后付息周期的附息式浮动利率债券计算
     *
     * @return 算法类别
     * @throws ParseException 日期格式错误
     */
    private static int pand(int interestType, int interestSort, String endDate, String settleDate, List<Cashflow> cashflows) throws ParseException {
        //固定利率附息债券
        if (interestType == 1 && interestSort == 2) {
            //判断是否处于最后的付息周期内
            if (pDate(settleDate, cashflows, interestType)) {
                //判断是否提前还本
                if(isEarlyRepay(settleDate, cashflows, interestType)){
                    return 5;
                }
                    return 1;
            } else {
                return 3;
            }
        //零息或贴现债券
        } else if (interestType == 2 || interestType == 3) {
            //到期日的日期
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(SDF.parse(endDate));
            //到期日的前一年的日期
            calendar.add(Calendar.YEAR, -1);
            String prepDateStr = SDF.format(calendar.getTime());
            //待偿期在一年及以内的零息债券和贴现债券
            if (settleDate.compareTo(prepDateStr) >= 0) {
                return 1;
            } else {
                return 2;
            }
        //浮动利率附息债券
        } else if (interestType == 1 && interestSort == 1) {
            //判断是否处于最后的付息周期内,
            if (pDate(settleDate, cashflows, interestType)) {
                //最后付息周期浮息债票面利率不变,视作固息处理
                return 1;
            } else {
                //未到最后收益日的浮息债
                return 4;
            }
        }
        return 0;
    }

    //判断结算日是否处于最后的付息周期，是返回true，不是返回false,
    private static boolean pDate(String date, List<Cashflow> cashflows, int interest_type) throws ParseException {
        //处理逻辑：（1）找到date的下一理论付息日 ；（2）如果下一理论付息日大于或等于所有的付息日，则说明是最后周期
        String nextDate = countPrepAndNextDate(date, cashflows, interest_type).get("pay_date").toString();
        for (Cashflow c : cashflows) {
            String end_date = c.getPay_date();
            if (end_date.compareTo(nextDate) > 0) {
                return false;
            }
        }
        return true;
    }

    //判断结算日前付息债是否提前还本，是返回true，不是返回false,
    private static boolean isEarlyRepay(String date, List<Cashflow> cashflows, int interest_type) throws ParseException {
        BigDecimal end_money = (BigDecimal) countPrepAndNextDate(date, cashflows, interest_type).get("end_money");
        return end_money.compareTo(new BigDecimal("100")) < 0;
    }

    /**
     * 当含权债行权时，选择合适的结算时间
     * 1、只有一个行权日，就只取该日期；
     * 2、如果有多个行权日且有部分行权日大于当前日期，取大于当前系统日期，并且最接近当前日期的行权日做到期日；
     * 3、如果有多个行权日且全部行权日都小于当前日期，取最后一个行权日做到期日；
     *
     * @param  dates  行权时间列表
     * @return String 行权日
     */
    private static String changeEndDate(List<String> dates) {
        if (dates == null) {
            return null;
        }
        Collections.sort(dates);
        //当前系统时间
        String systemDate = SDF.format(new Date());
        //如果行权时间只有一个,返回该行权日
        if (dates.size() == 1) {
            return dates.get(0);
        }else{//如果行权时间有多个
            for (String date : dates) {
                //返回大于系统当前日期最小行权日
                if (date.compareTo(systemDate) >= 0) {
                    return date;
                }
            }
            //都小于时，返回最后行权日
            return dates.get(dates.size() - 1);
        }

    }



    /**
     * 对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 二分法计算YTM
     * @param fv 本息和
     * @param d  结算日至到下一理论付息日的实际天数
     * @param m  结算日至到期兑付日的整年数
     * @param ty 当前计息年度的实际天数，算头不算尾
     * @param fullPrice   全价
     * @return BigDecimal 实际收益率/100
     */
    private static BigDecimal getYTMByDichotomy2(BigDecimal fv,BigDecimal d ,BigDecimal m,BigDecimal ty,BigDecimal fullPrice){
        //收益率的最大值
        BigDecimal max = new BigDecimal("1.1");
        //收益率的最大允许范围
        BigDecimal maxV = new BigDecimal("1");
        //收益率的最小值
        BigDecimal min = new BigDecimal("-1.1");
        //收益率的最小允许范围
        BigDecimal minV = new BigDecimal("-1");
        //2
        BigDecimal two = new BigDecimal("2");
        //实际的收益率
        BigDecimal ytm = null;
        //推算出的全价
        BigDecimal pv;

        for (int i = 0; i < TIMES; i++) {
            //二分差值法的插入数据
            ytm = (max.add(min)).divide(two, 16, RoundingMode.HALF_UP);
            //插值后算出的全价
            pv = countFullPrice(fv, d, m, ty, ytm);
            pv = pv.setScale(6, RoundingMode.HALF_UP);
            if (pv.subtract(fullPrice).abs().compareTo(ACCURACY) < 0) {
                break;
            } else if (pv.subtract(fullPrice).doubleValue() > 0) {
                min = ytm;
            } else {
                max = ytm;
            }
            if (ytm.compareTo(minV) <= 0 || ytm.compareTo(maxV) >= 0) {
                throw new CALCException("超出收益率的计算范围！");
            }
        }
        return ytm;
    }

    /**
     * 对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 二分法计算YTM
     * @param f  付息频率
     * @param d  结算日到下一最近付息日的实际天数
     * @param ts 上一个付息日与下一个付息日的实际天数
     * @param fullPrice 全价
     * @param cashMoney 百元利息
     * @return BigDecimal 实际收益率/100
     */
    private static BigDecimal getYTMByDichotomy3(Map<Integer, BigDecimal> cashMoney,BigDecimal f ,BigDecimal d,BigDecimal ts,BigDecimal fullPrice){
        //收益率的最大值
        BigDecimal max = new BigDecimal("1.1");
        //收益率的最小值
        BigDecimal min = new BigDecimal("-1.1");
        //收益率的最大允许范围
        BigDecimal maxV = new BigDecimal("1");
        //收益率的最小允许范围
        BigDecimal minV = new BigDecimal("-1");
        //2
        BigDecimal two = new BigDecimal("2");
        //推算的收益率
        BigDecimal ytm = null;
        //推算的全价
        BigDecimal pv ;
        //实际的全价
        fullPrice = fullPrice.setScale(8, RoundingMode.HALF_UP);
        for (int i = 0; i < TIMES; i++) {
            ytm = (max.add(min)).divide(two, 16, RoundingMode.HALF_UP);
            pv = countFullPrice(cashMoney, f, ytm, d, ts);
            pv = pv.setScale(8, RoundingMode.HALF_UP);
            if ((pv.subtract(fullPrice)).abs().compareTo(ACCURACY) < 0) {
                break;
            } else if (pv.subtract(fullPrice).doubleValue() > 0) {
                min = ytm;
            } else {
                max = ytm;
            }
            if (ytm.compareTo(minV) <= 0 || ytm.compareTo(maxV) >= 0) {
                System.out.println("超出收益率的计算范围");
				throw new CALCException("超出收益率的计算范围！");
            }
        }
        return ytm;
    }

    /**
     * 对不处于最后付息周期的固定利率债券，到期收益率按复利计算
     * 二分法计算YTM
     * @param f  付息频率
     * @param d  结算日到下一最近付息日的实际天数
     * @param ts 上一个付息日与下一个付息日的实际天数
     * @param fullPrice 全价
     * @param cashMoney 百元利息
     * @return BigDecimal 实际收益率/100
     */
    private static BigDecimal getYTMByDichotomy4(Map<Integer, BigDecimal> cashMoney,BigDecimal f ,BigDecimal d,BigDecimal ts,
                                                 BigDecimal Rr,BigDecimal R0,BigDecimal fullPrice){
        //收益率的最大值
        BigDecimal max = new BigDecimal("1.1");
        //收益率的最大允许范围
        BigDecimal maxV = new BigDecimal("1");
        //收益率的最小值
        BigDecimal min = new BigDecimal("-1.1");
        //收益率的最小允许范围
        BigDecimal minV = new BigDecimal("-1");
        //2
        BigDecimal two = new BigDecimal("2");
        //将要确定的收益率
        BigDecimal ytm = null;
        //将要计算的全价
        BigDecimal pv ;
        //实际的全价
        fullPrice = fullPrice.setScale(4, RoundingMode.HALF_UP);
        for (int i = 0; i < TIMES; i++) {
            ytm= (max.add(min)).divide(two, 16, RoundingMode.HALF_UP);
            pv = countFullPrice(cashMoney, f, ytm, d, ts, Rr, R0);
            pv = pv.setScale(4, RoundingMode.HALF_UP);
            if ((pv.subtract(fullPrice)).abs().compareTo(ACCURACY) < 0) {
                System.out.println("pv:" + pv);
                break;
            } else if (pv.subtract(fullPrice).doubleValue() > 0) {
                min = ytm;
            } else {
                max = ytm;
            }
            if (ytm.compareTo(minV) <= 0 || ytm.compareTo(maxV) >= 0) {
                System.out.println("超出收益率的计算范围");
                throw new CALCException("超出收益率的计算范围！");
            }
        }
        return ytm;
    }

    /**
     * 分段计息类型的债券获取当前资金流的信息
     * @param nextDate 下次理论付息日
     * @return Map 每个付息周期的百元利息
     */
    private static Map<Integer, BigDecimal> countCashMoney_1(List<Cashflow> cashflows, String nextDate, int pay_freq, int n) throws ParseException {
        //返回的结果
        Map<Integer, BigDecimal> ret = new HashMap<>();
        //付息频率
        BigDecimal payTimes = new BigDecimal(Integer.valueOf(DateUtils.getYearFreq(pay_freq)).toString());
        //定位付息次数
        int i = 0;
        //本金
        BigDecimal money = new BigDecimal("100");
        //剩余本金
        BigDecimal end_money = new BigDecimal("100");
        //兑付本金
        BigDecimal ret_money ;
        for (Cashflow c : cashflows) {
            String pay_date = c.getPay_date();
            int change_id = c.getChange_id();
            if (pay_date.compareTo(nextDate) >= 0 && change_id == 10) {
                BigDecimal couponRate = new BigDecimal(Double.valueOf(c.getCoupon_rate()).toString());
                BigDecimal sureCouponRate = couponRate.multiply(end_money).divide(money, 16, BigDecimal.ROUND_UP).divide(payTimes, 16, BigDecimal.ROUND_UP);
                i++;
                if (i == n) {
                    ret.put(i - 1, sureCouponRate.add(end_money));
                    break;
                } else {
                    ret.put(i - 1, sureCouponRate.add(findNextPayMoney(cashflows, c.getPay_date())));
                }
            } else if (change_id == 11 || change_id == 12) {
                ret_money = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                end_money = end_money.subtract(ret_money);
            }
        }
        return ret;
    }

    /**
     * 逐日计息类型的债券
     * 获取当前资金流的信息
     */
    private static Map<Integer, BigDecimal> countCashMoney_2(List<Cashflow> cashflows, String nextDate, int n) throws ParseException {
        //返回的结果
        Map<Integer, BigDecimal> ret = new HashMap<>();
        //本金
        BigDecimal money = new BigDecimal("100");
        //剩余本金
        BigDecimal end_money = new BigDecimal("100");
        //还的本金
        BigDecimal ret_money;
        //计算次数最后一次时加end_money
        int i = 0;
        for (Cashflow c : cashflows) {
            String pay_date = c.getPay_date();
            int change_id = c.getChange_id();
            if (pay_date.compareTo(nextDate) >= 0 && change_id == 10) {
                BigDecimal couponRate = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                //若有提前还本，这里需要折算
                BigDecimal sureCouponRate = couponRate.multiply(end_money).divide(money, 16, BigDecimal.ROUND_UP);
                i++;
                if (i == n) {
                    ret.put(i - 1, sureCouponRate.add(end_money));
                    break;
                } else {
                    ret.put(i - 1, sureCouponRate.add(findNextPayMoney(cashflows, c.getPay_date())));
                }
            } else if (change_id == 11 || change_id == 12) {
                ret_money = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                end_money = end_money.subtract(ret_money);
            }
        }
        return ret;
    }

    /**
     * 提前还本类型的债券
     * 获取当前资金流的信息
     */
    private static Map<Integer, BigDecimal> countCashMoney_3(List<Cashflow> cashflows, String nextDate, int n) throws ParseException {
        //返回的结果
        Map<Integer, BigDecimal> ret = new HashMap<>();
        //本金
        BigDecimal money = new BigDecimal("100");
        //剩余本金
        BigDecimal end_money = new BigDecimal("100");
        //还的本金
        BigDecimal ret_money;
        //计算次数最后一次时加end_money
        int i = 0;
        for (Cashflow c : cashflows) {
            String pay_date = c.getPay_date();
            int change_id = c.getChange_id();
            if (pay_date.compareTo(nextDate) >= 0 && change_id == 10) {
                i++;
                BigDecimal couponRate = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                BigDecimal sureCouponRate = couponRate.multiply(end_money).divide(money, 16, BigDecimal.ROUND_UP);
                if (i == n) {
                    ret.put(i - 1, sureCouponRate.add(end_money));
                    break;
                } else {
                    ret.put(i - 1, sureCouponRate.add(findNextPayMoney(cashflows, c.getPay_date())));
                }
            } else if (change_id == 11 || change_id == 12) {
                ret_money = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                end_money = end_money.subtract(ret_money);
            }
        }
        return ret;
    }

    //查找下次付息日的还本金额
    private static BigDecimal findNextPayMoney(List<Cashflow> cashflows, String pay_date) {
        BigDecimal ret = new BigDecimal("0");
        for (Cashflow c : cashflows) {
            if (c.getChange_id() == 12 && c.getPay_date().equals(pay_date)) {
                ret = new BigDecimal(Double.valueOf(c.getAmount()).toString());
                break;
            }
        }
        return ret;
    }

    /**
     * 获取债券理论付息日、票面利率和剩余本金
     *
     * 注意点：贴现债券没有付息的记录，必须推出理论的日期，零息债券的付息记录只有起息和到期，也必须手动推出
     * 附息债券:
     *          若 提前还本      理论付息日期<=交易结算日期  则 剩余本金=100-交易金额
     *          若 付息     开始日期<=交易结算日期<付息日期  则 利率-交易利率
     * 零息/贴现债券(结算后到到期算利息):
     *          若 付息/到期还本  交易结算日期>=起息日
     *          则 min(起息日+整年数)>交易结算日期的日期为下一理论付息日 下一理论付息日>到期日 取到期日
     *
     * @param settleDate    结算日期
     * @param cashFlows     资金流信息
     * @param interestType  债券品种
     * @return map，长度为4，
     *      start_date  表示上一理论付息日，
     *      pay_date    表示下一理论付息日,
     *      coupon_rate 表示当前票面利率，
     *      end_money   表示当前的剩余本金,只有付息债才有提前还本
     *
     * @throws RuntimeException 找不到合适的理论付息日时抛出
     * @throws ParseException   日期格式错误
     */
    private static Map<String, Object> countPrepAndNextDate(String settleDate, List<Cashflow> cashFlows, int interestType)
            throws CALCException, ParseException {
        Map<String, Object> map = new HashMap<>();
        BigDecimal end_money = new BigDecimal("100");
        map.put("end_money", end_money);

        switch (interestType) {
            //附息债券
            case 1:
                for (Cashflow c : cashFlows) {
                    //发生类型  1——起息、10——付息、12——ff提前还本、11——到期还本
                    int change_id = c.getChange_id();
                    //如果提前还本, 剩余本金=100-结算日以前的累计交易金额
                    if (change_id == 12 && c.getPay_date().compareTo(settleDate) <= 0) {
                        end_money = end_money.subtract(new BigDecimal(c.getAmount()), MathContext.DECIMAL64);
                    }
                    //如果不是付息的数据，取下一条
                    if (change_id != 10) {
                        continue;
                    }
                    String start_date = c.getStart_date();
                    String end_date = c.getPay_date();
                    //如果结算日期在开始和付息日期之间
                    if (settleDate.compareTo(start_date) >= 0 && settleDate.compareTo(end_date) < 0) {
                        BigDecimal coupon_rate = new BigDecimal(Double.valueOf(c.getCoupon_rate()).toString());
                        map.put("end_money", end_money);
                        map.put("coupon_rate", coupon_rate);
                        map.put("start_date", start_date);
                        map.put("pay_date", end_date);
                        return map;
                    }
                }
                break;
            //零息债券,贴现债券
            default:
                for (Cashflow c : cashFlows) {
                    //发生类型
                    int change_id = c.getChange_id();
                    //付息和到期还本
                    if (change_id == 10 || change_id == 11) {
                        BigDecimal coupon_rate = new BigDecimal(Double.valueOf(c.getCoupon_rate()).toString());
                        String begin_date = c.getStart_date();
                        String end_date = c.getPay_date();
                        Date begin = SDF.parse(begin_date);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(begin);
                        //当结算日期大于起息日，将起息日一直向后加一年，比结算日大的就是下一理论付息日
                        while (settleDate.compareTo(SDF.format(calendar.getTime())) >= 0) {
                            calendar.add(Calendar.YEAR, 1);
                        }
                        //下一理论付息日
                        String pay_date = SDF.format(calendar.getTime());
                        calendar.add(Calendar.YEAR, -1);
                        //上一理论付息日
                        String start_date = SDF.format(calendar.getTime());
                        //如果下一理论付息日大于到期日，则下一理论付息日就是到期日
                        if (pay_date.compareTo(end_date) > 0) {
                            pay_date = end_date;
                        }
                        map.put("coupon_rate", coupon_rate);
                        map.put("start_date", start_date);
                        map.put("pay_date", pay_date);
                        return map;
                    }
                }
                break;
        }
        throw new CALCException("未找到合适的理论付息日！");
    }

    /**
     * 推算全价算法
     * 对待偿期在一年以上的零息债券和贴现债
     * 公式： PV=FV/[(1+y)^{(d/TY)+m}]
     * @param fv   本息和
     * @param d    结算日至到下一理论付息日的实际天数
     * @param m    结算日至到期兑付日的整年数
     * @param ty   当前计息年度的实际天数，算头不算尾
     * @param ytm  到期收益率
     * @return BigDecimal 全价
     */
    private static BigDecimal countFullPrice(BigDecimal fv, BigDecimal d, BigDecimal m, BigDecimal ty,BigDecimal ytm) {
        //d/ty+m 幂数
        BigDecimal power = d.divide(ty, 16, RoundingMode.HALF_UP).add(m);
        //(1+y)^power 除数
        double divisor = Math.pow((1 + ytm.doubleValue()), power.doubleValue());
        //fv/divisor 全价
        return fv.divide(new BigDecimal(divisor, MathContext.DECIMAL64), 16, RoundingMode.HALF_UP);
    }

    //(1+y)^((d/TY)+m)=FV/PV
    private static BigDecimal countFullPriceNR(BigDecimal fv, BigDecimal d, BigDecimal m, BigDecimal ty,BigDecimal fullPrice) {
        //d/ty+m 幂数
        BigDecimal power = d.divide(ty, 16, RoundingMode.HALF_UP).add(m);
        //fv/pv
        BigDecimal squares = fv.divide(fullPrice, 16, RoundingMode.HALF_UP);
        //fv/divisor 全价
        return caclRootByNR(squares,power,16);
    }

    //牛顿拉夫逊算法 num是被开方数，n是开方次数,precision设置保留几位小数
    public static BigDecimal  caclRootByNR(BigDecimal squares,BigDecimal power,int precision)
    {
        BigDecimal x=squares.divide(power, 16, RoundingMode.HALF_UP);
        BigDecimal x0=BigDecimal.ZERO;

        BigDecimal e=new BigDecimal("0.0000000000000001");
        for(int i=1;i<precision;++i)
            e=e.divide(BigDecimal.TEN,i+1,BigDecimal.ROUND_HALF_EVEN);

        BigDecimal K=squares;
        BigDecimal m=power;

        long i=0;
        while(x.subtract(x0).abs().compareTo(e)>0)
        {
            x0=x;
            BigDecimal n= BigDecimal.valueOf(Math.pow(x.doubleValue(),power.doubleValue()));
            BigDecimal n1=BigDecimal.valueOf((Math.pow(x.doubleValue(),power.subtract(new BigDecimal("1")).doubleValue())));
            x=x.add(K.subtract(n).divide(m.multiply(n1),precision,BigDecimal.ROUND_HALF_EVEN));
            ++i;
        }
        return x;
    }


    /**
     * 推算全价算法
     * 对不处于最后付息周期的固定利率债券
     * 公式：PV=C/f/(1+y/f)^(d/ts)+C/f/(1+y/f)^(d/ts+1)……+C/f/(1+y/f)^[(d/ts)+n-1]+M/(1+y/f)^[(d/ts)+n-1]
     * @param map Map中key:第几次付息,value:年利率
     * @param f   付息频率
     * @param ytm 到期收益率
     * @param d   结算日到下一最近付息日的实际天数
     * @param ts  上一个付息日与下一个付息日的实际天数
     * @return BigDecimal 全价
     */
    private static BigDecimal countFullPrice(Map<Integer, BigDecimal> map, BigDecimal f, BigDecimal ytm, BigDecimal d, BigDecimal ts) {
        //全价，初始为0
        BigDecimal pv = new BigDecimal("0");
        //结算日至到期兑付日的付息次数
        int n = map.size();
        //1+y/f
        BigDecimal yf = ytm.divide(f, 16, RoundingMode.HALF_UP).add(new BigDecimal(1));
        //d/ts
        BigDecimal dts = d.divide(ts, 16, RoundingMode.HALF_UP);
        for (int i = 0; i < n; i++) {
            //c/f 每百元票面利息
            BigDecimal cf = map.get(i);
            //d/ts+i 幂数
            double power = dts.add(new BigDecimal(i)).doubleValue();
            //(1+y/f)^power 除数
            double divisor = Math.pow(yf.doubleValue(), power);
            //每次付息计算的全价
            BigDecimal temp = cf.divide(new BigDecimal(divisor), 8, RoundingMode.HALF_UP);
            //累计全价
            pv = pv.add(temp);
        }
        return pv;
    }

    /**
     * 推算全价算法
     * 对不处于最后付息周期的浮动利率债券
     *  公式：PV=C/f/(1+y/f)^(d/ts)+C/f/(1+y/f)^(d/ts+1)……+C/f/(1+y/f)^[(d/ts)+n-1]+M/(1+y/f)^[(d/ts)+n-1]
     *  此处C=R1+r R1:最近起息日的基准利率 r:发行时的基本利差
     *     y=R2+s R2:下一息期的基准利率是 s:期望的收益率点差
     *
     * @param map Map中key:第几次付息,value:年利率
     * @param f   付息频率
     * @param ytm 到期收益率
     * @param d   结算日到下一最近付息日的实际天数
     * @param ts  上一个付息日与下一个付息日的实际天数
     * @param R   当前市场的基础利率
     * @param R0  每百元票息
     * @return BigDecimal 全价
     */
    private static BigDecimal countFullPrice(Map<Integer, BigDecimal> map, BigDecimal f, BigDecimal ytm, BigDecimal d, BigDecimal ts, BigDecimal R, BigDecimal R0) {
        // 全价，初始为0
        BigDecimal pv = new BigDecimal("0");
        //结算日至到期兑付日的付息次数
        int n = map.size();
        //1+y的值 1+R+S
        BigDecimal Rs = ytm.divide(f,16, RoundingMode.HALF_UP).add(new BigDecimal(1));
        //d/ts的值
        BigDecimal dts = d.divide(ts, 16, RoundingMode.HALF_UP);
        for (int i = 0; i < n; i++) {
            //每次付息计算的全价
            BigDecimal temp ;
            //dts+i
            double power = dts.add(new BigDecimal(i)).doubleValue();
            if (i == 0) {
                //1+R+S的指数乘积
                double divisor = Math.pow(Rs.doubleValue(), power);
                //(R0+r)/(1+R+S)w：w为分母的幂
                temp = R0.divide(f,16, RoundingMode.HALF_UP).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
            } else if (i == (n - 1)) {
                // cf:付息现金流的值M
                BigDecimal cf = new BigDecimal(100);
                //1+R+S的指数乘积
                double divisor = Math.pow(Rs.doubleValue(), power);
                //((R+r)+M)/(1+R+S)w：w为分母的幂
                temp = (R.divide(f,16, RoundingMode.HALF_UP).add(cf)).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
            } else {
                //yf^fang
                double divisor = Math.pow(Rs.doubleValue(), power);
                //(R+r)/(1+R+S)w：w为分母的幂
                temp = R.divide(f,16, RoundingMode.HALF_UP).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
            }
            temp = temp.setScale(4, RoundingMode.HALF_UP);
            pv = pv.add(temp);
        }

        return pv;
    }

    /**
     * 推算全价算法
     * 对不处于最后付息周期的提前还本的债券计算
     */
    private static BigDecimal countAheadFull(Map<Integer, BigDecimal> map, BigDecimal f, BigDecimal y, BigDecimal d, BigDecimal ts) {
        //浮息债券当前票面利率
        BigDecimal R0r = new BigDecimal(0.0265).divide(f,16, RoundingMode.HALF_UP);
        //浮息债券利差
        BigDecimal r = new BigDecimal(0.0115);//1.15
        //当前市场的基础利率
        BigDecimal R = new BigDecimal(0.0125);
        // 返回值，初始为0
        BigDecimal pv = new BigDecimal("0");
        //map的数据条数
        int n = map.size();
        //R+r
        BigDecimal Rr = R.add(r);
        // 1+y的值 1+R+S
        BigDecimal Rs = y.divide(f,16, RoundingMode.HALF_UP).add(new BigDecimal(1));
        //d/ts=w的值
        BigDecimal dts = d.divide(ts, 16, RoundingMode.HALF_UP);
        for (int i = 0; i < n; i++) {
            //每次的计算结果
            BigDecimal temp ;
            //dts+i
            double fang = dts.add(new BigDecimal(i)).doubleValue();
            if (i == 0) {
                // cf:付息现金流的值M
                BigDecimal cf = new BigDecimal(100);
                //1+R+S的指数乘积
                double divisor = Math.pow(Rs.doubleValue(), fang);
                //(R0+r)*M/(1+R+S)w：w为分母的幂
                temp = R0r.multiply(cf).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
//			        System.out.println("第"+i+"个分数式，分子为："+R0r.multiply(cf).setScale(4, RoundingMode.HALF_UP)+"分母为："+divisor);
            } else if (i == (n - 1)) {
                // cf:付息现金流的值M
                BigDecimal cf = new BigDecimal(100);
                //1+R+S的指数乘积
                double divisor = Math.pow(Rs.doubleValue(), fang);
                //((R+r)*M+M)/(1+R+S)w：w为分母的幂
                temp = (Rr.divide(f,16, RoundingMode.HALF_UP).multiply(cf).add(cf)).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
            } else {
                // cf:付息现金流的值M
                BigDecimal cf = new BigDecimal(100);
                //yf^fang
                double divisor = Math.pow(Rs.doubleValue(), fang);
                //(R+r)*M/(1+R+S)w：w为分母的幂
                temp = Rr.divide(f,16, RoundingMode.HALF_UP).multiply(cf).divide(new BigDecimal(divisor), 16, RoundingMode.HALF_UP);
            }
            temp = temp.setScale(4, RoundingMode.HALF_UP);
            pv = pv.add(temp);
        }

        return pv;
    }

    /**
     * 计算到期兑付日债券本息和FV
     * 公式：
     *      零息债券:FV=M
     *      固定利率债券: FV=M+C/F
     *      到期一次还本付息债券:FV=M+N×C
     * 变量：
     *      M ：债券面值
     *      C ：债券票面年利息
     *      F ：年付息频率
     *      N ：债券期限（年），即从起息日至到期兑付日的整年数
     */
    private static BigDecimal doFv(int interestType, int interestSort, BigDecimal couponRate, int payFreq,
                                   String beginDate, String endDate) throws ParseException {
        //price:表示本金100
        BigDecimal price = new BigDecimal("100", MathContext.DECIMAL64);
        if (interestType == 1 && interestSort == 2) {//固定利率债券
            //每年的付息次数
            BigDecimal times = new BigDecimal(DateUtils.getYearFreq(payFreq));
            //price+couponRate/times
            return price.add(couponRate.divide(times, 16, BigDecimal.ROUND_HALF_UP));
        } else if (interestType == 2) {//利随本清债
            //years:起息到到息的年数，可以包含小数
            BigDecimal years = new BigDecimal(Double.valueOf(countDoubleYear(beginDate, endDate)).toString());
            //price+years*couponRate
            return price.add(years.multiply(couponRate), MathContext.DECIMAL64);
        } else if (interestType == 3) {//贴现债
            return price;
        }
        return price;
    }

    /**
     * 重载计算本息和
     */
    private static BigDecimal doFv(int interestType, int interestSort, BigDecimal couponRate, int payFreq,
                                   String beginDate, String endDate, String settleDate) throws ParseException {
        //price:表示本金100
        BigDecimal price = new BigDecimal("100", MathContext.DECIMAL64);
        //固定利率债券
        if (interestType == 1 && interestSort == 2) {
            //每年的付息次数
            BigDecimal times = new BigDecimal(DateUtils.getYearFreq(payFreq));
            //price+couponRate/times
            return price.add(couponRate.divide(times, 16, RoundingMode.HALF_UP));
        }
        //利随本清债
        if (interestType == 2) {
            //years:起息到到息的年数，可以包含小数
            BigDecimal years = new BigDecimal(Double.valueOf(countDoubleYear(beginDate, endDate, settleDate)).toString());
            //price+years*couponRate
            return price.add(years.multiply(couponRate), MathContext.DECIMAL64);
        }
        //贴现债
        if (interestType == 3) {
            return price;
        }
        return price;
    }

    /**
     * 计算起息到到期的整年数
     */
    private static double countDoubleYear(String beginDate, String endDate) throws ParseException {
        //起息日的年份
        int beginYear = Integer.parseInt(beginDate.substring(0, 4));
        //到期的年份
        int endYear = Integer.parseInt(endDate.substring(0, 4));
        //起息到到期的年数，不考虑月份的
        int sub = endYear - beginYear;
        Date date = SDF.parse(beginDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //将起息日加上年数
        calendar.add(Calendar.YEAR, sub);
        //起息加年数的日期
        Date lastDate = calendar.getTime();
        //到期的日期
        Date end_Date = SDF.parse(endDate);
        if (lastDate.getTime() > end_Date.getTime()) {//如果起息加年数的日期大于到期的日期，说明年份加大了，应该减去一年
            calendar.add(Calendar.YEAR, -1);
            sub -= 1;
        }
        //和起息日相差整年数且下于到期日的最大日期的字符串
        String lastDateString = SDF.format(calendar.getTime());
        //最后日期和到期日的天数
        int days = DateUtils.DateApartOneDay(lastDateString, endDate);
        return sub + days / 365.0;
    }

    /**
     * 重载计算起息到到期的整年数
     */
    private static int countYear(String beginDate, String endDate) throws ParseException {
        return (int) countDoubleYear(beginDate, endDate);
    }

    /**
     * 重载计算起息到到期的整年数
     */
    private static double countDoubleYear(String beginDate, String endDate, String settleDate) throws ParseException {
        //起息日的年份
        int beginYear = Integer.parseInt(beginDate.substring(0, 4));
        //到期的年份
        int endYear = Integer.parseInt(endDate.substring(0, 4));
        //起息到到期的年数，不考虑月份的
        int sub = endYear - beginYear;
        //计息基数
        double yearday = DateUtils.setTY1(beginDate, endDate, settleDate).intValue();
        Date date = SDF.parse(beginDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //将起息日加上年数
        calendar.add(Calendar.YEAR, sub);
        //起息加年数的日期
        Date lastDate = calendar.getTime();
        //到期的日期
        Date end_Date = SDF.parse(endDate);
        if (lastDate.getTime() > end_Date.getTime()) {//如果起息加年数的日期大于到期的日期，说明年份加大了，应该减去一年
            calendar.add(Calendar.YEAR, -1);
            sub -= 1;
        }
        //和起息日相差整年数且下于到期日的最大日期的字符串
        String lastDateString = SDF.format(calendar.getTime());
        //最后日期和到期日的天数
        int days = DateUtils.DateApartOneDay(lastDateString, endDate);
        return sub + days / yearday;
    }
}
