package com.kayak.cloud.algorithm.comm;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	/**
	 * 常量SCALE，设置BigDecimal 类型的精度
	 */
	public static final int SCALE = 16;

	/**
	 * 常量MONEY-100
	 */
	public static final BigDecimal HUNDRED = new BigDecimal("100");
	/**
	 * 常量ZERO-0
	 */
	public static final BigDecimal ZERO = new BigDecimal("0");
	/**
	 * 年月日
	 */
	public static final SimpleDateFormat sft = new SimpleDateFormat("yyyyMMdd");
	/** 首次付息日 */
	public static int fist_paydays = 0;
	/** 0.0000 4位 */
	public static DecimalFormat dft = new DecimalFormat("0.0000");
	/** 0.00000000 8位 */
	public static DecimalFormat dft_8 = new DecimalFormat("0.00000000");
	/**
	 * 常量天数-365
	 */
	public static final BigDecimal day_365 = new BigDecimal("365");

	/**
	 * 常量天数-366
	 */
	public static final BigDecimal day_366 = new BigDecimal("366");

	/**
	 * 常量天数-360
	 */
	public static final BigDecimal day_360 = new BigDecimal("360");

	/**
	 * 得到起息日至到期日之间的年整数
	 *
	 * @param end
	 *            到期日
	 * @param begin
	 *            起息日
	 * @return 年整数
	 * @throws ParseException
	 */
	public static int getYears(String end, String begin) throws ParseException {
		int k = 0;
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();

		try {
			calendar1.setTime(sft.parse(begin));
			calendar2.setTime(sft.parse(end));
			// 起始
			int end_year1 = calendar1.get(Calendar.YEAR);
			int end_month1 = calendar1.get(Calendar.MONTH);
			int end_days1 = calendar1.get(Calendar.DAY_OF_MONTH);
			// 结束
			int end_year2 = calendar2.get(Calendar.YEAR);
			int end_month2 = calendar2.get(Calendar.MONTH);
			int end_days2 = calendar2.get(Calendar.DAY_OF_MONTH);

			if (end_month2 > end_month1) {
				k = end_year2 - end_year1;
			} else if (end_month2 == end_month1) {
				if (end_days2 >= end_days1) {
					k = end_year2 - end_year1;
				} else {
					k = end_year2 - end_year1 - 1;
				}
			} else if (end_month2 < end_month1) {
				k = end_year2 - end_year1 - 1;
			}
			return k;
		} catch (ParseException e) {
			e.printStackTrace();
			return k;
		}
	}

	/**
	 * 日期加月
	 *
	 * @param date
	 *            日期
	 * @param months
	 *            月数
	 * @return （日期 + 月数）后的日期
	 * @throws ParseException
	 */
	public static String DateAddMonth(String date, int months) throws ParseException {
		Date now = sft.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.MONTH, months);
		return sft.format(calendar.getTime());
	}

	/**
	 * 日期加天数
	 *
	 * @param date
	 *            日期
	 * @param days
	 *            天数
	 * @return （日期 + 天数）后的日期
	 * @throws ParseException
	 */
	public static String DateAddDay(String date, int days) throws ParseException {
		Date now = sft.parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return sft.format(calendar.getTime());
	}

	/**
	 * 日期相减
	 *
	 * @param date1
	 *            起始日期
	 * @param date2
	 *            结算日期
	 * @return 相隔天数
	 * @throws ParseException
	 */
	public static int DateApart(String date1, String date2) throws ParseException {
		long days = (sft.parse(date2).getTime() - sft.parse(date1).getTime()) / (1000L * 3600 * 24);
		return (int) days;
	}

	/**
	 * 日期相减
	 *
	 * @param date1
	 *            起始日期
	 * @param date2
	 *            结算日期
	 * @return 相隔天数
	 * @throws ParseException
	 */
	public static int DateApartOneDay(String date1, String date2) throws ParseException {
		// date2比date1刚好小一天的情况忽略不计
		SimpleDateFormat sft = new SimpleDateFormat("yyyyMMdd");
		long days = (sft.parse(date2).getTime() - sft.parse(date1).getTime() + 1000L * 3600 * 12) / (1000L * 3600 * 24);
		return (int) days;
	}

	/**
	 * 日期相减,获取两个日期间的年数，主要用于利随本清的债，如：20141012-20161011=1.99726776（考虑了闰年）
	 *
	 * @param date1
	 *            起始日期
	 * @param date2
	 *            结算日期
	 * @return 相隔年数，带小数位，作为计算现金流的
	 * @throws ParseException
	 */
	public static BigDecimal DateApartPra(String date1, String date2) throws ParseException {
		BigDecimal resYes = ZERO;
		// 1、两个日期之间的年数
		int years1 = getYears(date2, date1);
		resYes = resYes.add(new BigDecimal(years1));
		// 获取截止日的年+起始日的月份、日，判断如果该日期大于截止日，那么2中获取的年份应该是截止日年份-1；等于就不用再计算了；小于，那么2中获取的应该是截止日年份
		String newDate1 = date2.substring(0, 4) + date1.substring(4, 8);// 截止日当年交易日
		// 2、起始日到截至到最后一年的日期，得根据到期日判断，可能是同一年、也可能不是同一年
		int praDays = 0;// 实际参与计算的天数
		if (newDate1.compareTo(date2) > 0) {// 截止日当年实际日期没有一年
			if (date2.substring(0, 4) != null && !"".equals(date2.substring(0, 4))) {
				newDate1 = (Integer.parseInt(date2.substring(0, 4)) - 1) + date1.substring(4, 8);// 截止日前一年交易日
				praDays = DateApart((Integer.parseInt(date2.substring(0, 4)) - 1) + date2.substring(4, 8), date2);// 截止日当前交易年限全年时长
			}
		} else if (newDate1.compareTo(date2) == 0) {
			return resYes;
		} else if (newDate1.compareTo(date2) < 0) {// 截止日当年实际日期超过一年
			praDays = DateApart(date2, (Integer.parseInt(date2.substring(0, 4)) + 1) + date2.substring(4, 8));// 截止日下一交易年限全年时长
		}

		// 3、计算不是整数年数部分天数的年数
		// 获取newDate1到到日期之间的实际天数，再根据到日期年份是否闰年去计算
		int lastDays = DateApart(newDate1, date2);
		/*
		 * Date date2Date = sft.parse(date2); Calendar calendar =
		 * Calendar.getInstance(); calendar.setTime(date2Date);
		 */
		BigDecimal b1 = BigDecimal.valueOf(praDays);
		BigDecimal b2 = BigDecimal.valueOf(lastDays);
		BigDecimal a1 = b2.divide(b1, SCALE, BigDecimal.ROUND_HALF_UP);// b2/b1
		// 保留8位
		// 4舍5入
		resYes = resYes.add(a1);

		return resYes;
	}

	/**
	 * 得到两个时间之间有多少个月
	 *
	 * @param start
	 * @param end
	 * @return
	 * @throws ParseException
	 */
	public static int getMonths(String start, String end) throws ParseException {
		int startYear = Integer.parseInt(start.substring(0, 4));
		int startMonth = Integer.parseInt(start.substring(4, 6));
		int endYear = Integer.parseInt(end.substring(0, 4));
		int endMonth = Integer.parseInt(end.substring(4, 6));

		int ret = endYear * 12 + endMonth - startMonth - startYear * 12;
		if (Integer.parseInt(end.substring(6)) > Integer.parseInt(start.substring(6))) {
			ret++;
		}
		return ret;
	}

	/**
	 * 获取下次还息时间
	 *
	 * @param date
	 * @param m
	 * @param end_date
	 * @return
	 */
	public static String addMonths(String date, int m, String end_date) {// 返回在date的基础上，下次还本的时间
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(4, 6));
		int day = Integer.parseInt(date.substring(6, 8));
		month += m;
		if (month > 12) {
			month = month - 12;
			year += 1;
		}
		if (fist_paydays > 28) {// 首次付息日为29,30,31
			if (month == 2) {
				if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
					day = 29;
				} else {
					day = 28;
				}
			} else if (month == 4 || month == 6 || month == 9 || month == 11) {
				if (fist_paydays == 29) {
					day = 29;
				} else {
					day = 30;
				}
			} else {
				day = fist_paydays;
			}
		}
		String ret = "";
		if (month < 10) {
			ret = year + "0" + month;
		} else {
			ret = year + "" + month;
		}
		if (day < 10) {
			ret = ret + "0" + day;
		} else {
			ret = ret + "" + day;
		}
		if (ret.compareTo(end_date) > 0) {
			return end_date;
		} else {
			return ret;
		}
	}

	/**
	 * 获取当前年度实际天数
	 *
	 * @param s
	 * @return
	 */
	public static int getYearDays(String s) {
		Calendar c = getByString(s);
		return c.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 获取两个日期间隔整年数
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public static int getYear(String start, String end) {
		Calendar s = getByString(start);
		Calendar e = getByString(end);
		int ret = 0;
		while (s.compareTo(e) < 0) {
			s.add(Calendar.YEAR, 1);
			if (s.compareTo(e) <= 0) {
				ret++;
			} else {
				break;
			}
		}
		return ret;
	}

	public static Calendar getByString(String s) {
		Calendar c = Calendar.getInstance();
		c.set(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)) - 1,
				Integer.parseInt(s.substring(6)));
		return c;
	}

	/**
	 * 计算一个日期在months个月后的天数
	 *
	 * @param date
	 *            ：开始日期
	 * @param months
	 *            ：月份
	 * @return 天数
	 */
	public static int getDaysByMonth(String date, int months) {
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(4, 6));
		int day = Integer.parseInt(date.substring(6, 8));
		Calendar c = Calendar.getInstance();
		c.set(year, month - 1, day);
		long st = c.getTime().getTime();
		c.add(Calendar.MONTH, months);
		long et = c.getTime().getTime();
		return (int) ((et - st) / 1000 / 60 / 60 / 24);
	}

	/**
	 * 获取月份 两位
	 *
	 * @param a
	 * @return
	 */
	public static String parse2(int a) {
		if (a > 9) {
			return String.valueOf(a);
		} else {
			return "0" + a;
		}
	}

	/**
	 * 获取算法需要计息周期天数
	 *
	 * @throws ParseException
	 */
	public static int getdays_use(int bond_baseday, String begin_date, String end_date) throws ParseException {
		if (bond_baseday == 1) {
			return DateUtils.getDaysByMonth(begin_date, 12);
		} else if (bond_baseday == 2) {
			return 360;
		} else if (bond_baseday == 3) {
			return 365;
		} else if (bond_baseday == 4) {
			return 366;
		} else {
			throw new ParseException("计息基础值有误", bond_baseday);
		}
	}

	public static BigDecimal setTY1(String begin_date, String end_date, String date) throws ParseException {
		BigDecimal ty = ZERO;
		SimpleDateFormat sft = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		Calendar calendar3 = Calendar.getInstance();
		calendar1.setTime(sft.parse(begin_date));
		calendar2.setTime(sft.parse(end_date));
		calendar3.setTime(sft.parse(date));
		calendar1.add(Calendar.YEAR, 1);
		int end_year = calendar2.get(Calendar.YEAR);
		int begin_year = calendar1.get(Calendar.YEAR);
		String year = "";
		String mon = "";
		String day = "";
		String dateString = "";
		while (begin_year <= (end_year + 1)) {
			if (!calendar1.before(calendar3)) {
				year = (calendar1.get(Calendar.YEAR) - 1) + "";
				mon = (calendar1.get(Calendar.MONTH) + 1) + "";
				if ((calendar1.get(Calendar.MONTH) + 1) < 10) {
					mon = "0" + mon;
				}
				day = (calendar1.get(Calendar.DAY_OF_MONTH)) + "";
				if (calendar1.get(Calendar.DAY_OF_MONTH) < 10) {
					day = "0" + day;
				}
				dateString = year + mon + day;
				break;
			} else {
				calendar1.add(Calendar.YEAR, 1);
				begin_year = calendar1.get(Calendar.YEAR);
				continue;
			}
		}
		calendar2.setTime(sft.parse(dateString));
		Long val = (calendar1.getTimeInMillis() - calendar2.getTimeInMillis()) / (1000 * 3600 * 24);
		Integer days = Integer.parseInt(val.toString());
		ty = new BigDecimal(Integer.toString(days));
		return ty;
	}

	/**
	 * 债券息票品种为零息债券
	 *
	 * @return
	 * @throws java.text.ParseException
	 */
	public static BigDecimal setTY(String begin_date, String settle_date) throws ParseException {
		BigDecimal ty = ZERO;
		SimpleDateFormat sft = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		String begin_date1 = begin_date;
		System.err.println("起息日期：" + begin_date);
		while (begin_date.compareTo(settle_date) <= 0) {
			calendar.setTime(sft.parse(begin_date));
			calendar.add(Calendar.YEAR, 1);
			begin_date = sft.format(calendar.getTime());
			if (begin_date.compareTo(settle_date) > 0)
				break;
			else
				begin_date1 = begin_date;
		}
		long TY = getDateDays(begin_date1, begin_date);
		System.err.println("起息日期：" + begin_date + "天数:" + TY + "结算日期：" + settle_date);
		ty = new BigDecimal(Long.toString(TY));
		return ty;
	}

	public static long getDateDays(String begin_date, String end_date) throws ParseException {
		SimpleDateFormat sft = new SimpleDateFormat("yyyyMMdd");
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(sft.parse(begin_date));
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);
		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(sft.parse(end_date));
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);
		return (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24);
	}

	/**
	 * 计算日期间隔内存在2月29日次数
	 *
	 * @param date1
	 * @param date2
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal DateFeb29(String date1, String date2) throws ParseException {
		int sum = 0;
		int date1_year = Integer.parseInt(date1.substring(0, 4));
		int date2_year = Integer.parseInt(date2.substring(0, 4));
		for (int i = date1_year; i <= date2_year; i++) {
			if ((i % 4 == 0 && i % 100 != 0) || i % 400 == 0) {
				if (sft.parse(date1).before(sft.parse(i + "0229")) && sft.parse(date2).after(sft.parse(i + "0229"))) {
					sum++;
				}
			}
		}

		return BigDecimal.valueOf(sum);

	}

	/**
	 * 根据付息频率得到付息次数
	 *
	 * @param pay_freq
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal getTimes(Integer pay_freq) throws ParseException {
		int times = 0;
		if (pay_freq == 1) {// 月
			times = 12;
		} else if (pay_freq == 2) {// 季
			times = 12 / 3;
		} else if (pay_freq == 3) {// 半年
			times = 12 / 6;
		} else if (pay_freq == 4) {// 一年
			times = 12 / 12;
		}
		return BigDecimal.valueOf(times);
	}

	/**
	 * @param payFreq 1—— 按月付息 2—— 按季付息 3——半年付息 4—— 按年付息 5——到期付息
	 * @return int 付息频率年付次数
	 */
	public static int getYearFreq(int payFreq) {
		int freq = 1;
		if (payFreq == 1) {
			freq = 12;
		} else if (payFreq == 2) {
			freq = 4;
		} else if (payFreq == 3) {
			freq = 2;
		}
		return freq;
	}

	/**
	 * @param payFreq 1—— 按月付息 2—— 按季付息 3——半年付息 4—— 按年付息 5——到期付息
	 * @return int 付息频率间隔月数
	 */
	public static int getMonthFreq(int payFreq) {
		//每次付息的月份间隔
		int month = 0;
		if (payFreq == 1) {
			month = 1;
		} else if (payFreq == 2) {
			month = 3;
		} else if (payFreq == 3) {
			month = 6;
		} else if (payFreq == 4 || payFreq == 5) {
			month = 12;
		}
		return month;

	}


	/**
	 * 计算结算日的下一理论付息日开始到到期付息日的付息总次数
	 *
	 * @param nextDate 下一理论付息日
	 * @param endDate  到期付息日
	 * @param payFreq  付息频率
	 * @return
	 */
	public static int countPayTimes(String nextDate, String endDate, int payFreq) {
		//到期日的月份
		int eMonth = Integer.parseInt(endDate.substring(4, 6));
		//到期日的年份
		int eYear = Integer.parseInt(endDate.substring(0, 4));
		//到期日的日期
		int eDay = Integer.parseInt(endDate.substring(6, 8));

		//下次理论付息日的月份
		int nMonth = Integer.parseInt(nextDate.substring(4, 6));
		//下次理论付息日的年份
		int nYear = Integer.parseInt(nextDate.substring(0, 4));
		//下次理论付息日的日期
		int nDay = Integer.parseInt(nextDate.substring(6, 8));

		int s = 0;
		if (eMonth / nMonth == 1 && eYear / nYear == 1 && eDay - nDay > 0) {
			s = 1;
		} else if (eMonth / nMonth == 1 && eMonth - nMonth > 0) {
			s = 1;
		}
		return ((eYear - nYear) * 12 + eMonth - nMonth) / getMonthFreq(payFreq) + s + 1;
	}

	/**
	 * 获取付息周期起始日期（付息频率为月，季，半年，年）
	 * @param settle_date 结算日期
	 * @param begin_date 起息日
	 * @param pay_freq 付息频率
	 * @return
	 * @throws ParseException
	 */
    public static String getstartdate(String settle_date, String begin_date, int pay_freq, String first_date, String end_date) throws ParseException{
        String start_date = null;
        int months = DateUtils.getMonths(first_date, end_date)+1;
        int times = 0;// 多少次还完?
        int month = 0;// 多少个月还一次?
        if (pay_freq == 5) { // 到期一次付息还本
        } else if (pay_freq == 6) {// 自定义
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
        int year = Integer.parseInt(begin_date.substring(0, 4));
        int m = Integer.parseInt(begin_date.substring(4, 6));
        int d = Integer.parseInt(begin_date.substring(6));
        Calendar c = Calendar.getInstance();
        c.set(year, m-1, d);
        c.add(Calendar.MONTH, times*month);
        //理论算出的到期时间
        String lun = ""+c.get(Calendar.YEAR)+DateUtils.parse2(c.get(Calendar.MONTH)+1)+DateUtils.parse2(c.get(Calendar.DATE));
        if(lun.compareTo(end_date)<0){
            times++;
        }
        String pay_date1 = null;
        for (int i = 0; i < times; i++) {
            if (i == 0) {// 起息的时间，第一条付息
                start_date = begin_date;
                if (first_date != "" && !first_date.equals(begin_date)) {
                    pay_date1 = first_date;
                }else{
                    pay_date1 =DateUtils.addMonths(start_date, month,end_date);
                }
            } else if (i == 1) {//第二条付息记录
                if (first_date != "" && !first_date.equals(begin_date)) {
                    start_date = first_date;//开始日期是首次付息日
                } else {//首次付息日不明确
                    start_date = pay_date1;//开始日期是上次的发生日期
                }
                pay_date1 = DateUtils.addMonths(start_date,
                        month,end_date);
            }else {//最后一条付息
                start_date = pay_date1;
                pay_date1 = DateUtils.addMonths(start_date,
                        month,end_date);

            }
            if(settle_date.compareTo(pay_date1)<=0 && settle_date.compareTo(start_date)>=0){
                break;
            }
        }
        return start_date;
    }
}
