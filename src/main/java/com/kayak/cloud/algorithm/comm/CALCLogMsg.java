/**
 * Copyright (C), 2019-2019, 北京开科唯识技术有限公司
 * FileName: CALCLogMsg
 * Author:   Administrator
 * Date:     2019/6/24 15:23
 * Description: 计算日志枚举类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kayak.cloud.algorithm.comm;

/**
 * 〈一句话功能简述〉<br> 
 * 〈计算日志枚举类〉
 *
 * @author Administrator
 * @create 2019/6/24
 * @since 1.0.0
 */
public enum CALCLogMsg {
    DateNotInRange("DtaeNotInRange", "结算日期不在计算期间内!"),
    DateError("DateError", "传入日期有误！"),
    FullPriceLessZero("FullPriceLessZero", "全价不能小于或等于0！"),
    NetPriceIllegal("NetPriceIllegal", "净价不能小于0或大于200！"),
    BondTypeMatchFail("BondTypeMatchFail", "债券的类型匹配失败！"),
    CalcSuccess("CalcSuccess", "计算成功！"),
    True("True", "true"),
    False("False", "false"),
    NotValue("NotValue", "不是一个 %s 值！"),
    ExpInt("ExpInt", "int"),
    ExpDouble("ExpDouble", "double"),
    ExpString("ExpString", "String"),
    ExpBoolean("ExpBoolean", "boolean"),
    ExpBigDecimal("ExpBigDecimal", "BigDecimal"),
    ExpNull("ExpNull","空指针异常!"),
    ExpList("ExpString", "List");



    private String code;
    private String msg;

    CALCLogMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}