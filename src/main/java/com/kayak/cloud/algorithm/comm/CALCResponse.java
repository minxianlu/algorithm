/**
 * Copyright (C), 2019-2019, 北京开科唯识技术有限公司
 * FileName: CALCResponse
 * Date:     2019/6/24 14:20
 * Description: 计算响应类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kayak.cloud.algorithm.comm;


import java.math.BigDecimal;
import java.util.List;

/**
 * 〈计算响应类〉
 *
 * @since 1.0.0
 */
public class CALCResponse {
    //响应状态
    public CALCResultStatus status;
    //算法名称
    public String algorithmName = "";
    //计算类型
    public CALCResultTypes types;
    //计算结果
    public CALCResult results = new CALCResult();

    /**
     * 计算结果类型
     */
    public enum CALCResultTypes {
        resInt, resDouble, resBigDecimal, resString, resBool, resError, resObject, resList
    }

    public enum CALCResultStatus {
        OK, ERROR
    }

    /**
     * 响应错误结果
     */
    public CALCResponse calcResponseError(String msg, String algorithmName) {
        this.status = CALCResultStatus.ERROR;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resError;
        if (msg == null) {
            this.results.setCalcLog(CALCLogMsg.ExpNull.getMsg());
        } else {
            this.results.setCalcLog(msg);
        }
        return this;
    }

    /**
     * String类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(String strResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resString;
        this.results.setStrResult(strResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * double类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(double dblResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resDouble;
        this.results.setDblResult(dblResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * boolean类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(boolean booResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resBool;
        this.results.setBooResult(booResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * int类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(int intResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resInt;
        this.results.setIntResult(intResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * BigDecimal类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(BigDecimal bdlResult, String algorithmName) {
        CALCResponse response = new CALCResponse();
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resBigDecimal;
        this.results.setBdlResult(bdlResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * List类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(List listResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resList;
        this.results.setListResult(listResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * Object类型计算响应成功
     */
    public CALCResponse calcResponseSuccess(Object objResult, String algorithmName) {
        this.status = CALCResultStatus.OK;
        this.algorithmName = algorithmName;
        this.types = CALCResultTypes.resObject;
        this.results.setObjResult(objResult);
        this.results.setCalcLog(CALCLogMsg.CalcSuccess.getMsg());
        return this;
    }

    /**
     * 计算结果类转boolean类型
     */
    public boolean toBoolean() throws CALCException {
        boolean result ;
        if (this.types == CALCResultTypes.resBool) {
            result = this.results.isBooResult();
        } else if (this.types == CALCResultTypes.resString) {
            if (CALCLogMsg.True.getMsg().equalsIgnoreCase(this.results.getStrResult())) {
                result = true;
            } else if (CALCLogMsg.False.getMsg().equalsIgnoreCase(this.results.getStrResult())) {
                result = false;
            } else {
                throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                        CALCLogMsg.ExpBoolean));
            }
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpBoolean));
        }
        return result;
    }

    /**
     * 计算结果类转BigDecimal类型
     */
    public BigDecimal toBigDecimal() throws CALCException {
        BigDecimal result;
        if (this.types == CALCResultTypes.resInt) {
            result = BigDecimal.valueOf(this.results.getIntResult());
        } else if (this.types == CALCResultTypes.resDouble) {
            result = BigDecimal.valueOf(this.results.getDblResult());
        } else if (this.types == CALCResultTypes.resBigDecimal) {
            result = this.results.getBdlResult();
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpBigDecimal));
        }
        return result;
    }

    /**
     * 计算结果类转double类型
     */
    public double toDouble() throws CALCException {
        double result;
        if (this.types == CALCResultTypes.resInt) {
            result = this.results.getIntResult();
        } else if (this.types == CALCResultTypes.resDouble) {
            result = this.results.getDblResult();
        } else if (this.types == CALCResultTypes.resBigDecimal) {
            result = this.results.getBdlResult().doubleValue();
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpDouble));
        }
        return result;
    }

    /**
     * 计算结果类转int类型
     */
    public int toInt() throws CALCException {
        int result;
        if (this.types == CALCResultTypes.resInt) {
            result = this.results.getIntResult();
        } else if (this.types == CALCResultTypes.resDouble) {
            result = (int) this.results.getDblResult();
        } else if (this.types == CALCResultTypes.resBigDecimal) {
            result = this.results.getBdlResult().intValue();
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpInt));
        }
        return result;
    }

    /**
     * 计算结果类转list类型
     */
    public List toList() throws CALCException {
        List result;
        if (this.types == CALCResultTypes.resList) {
            result = this.results.getListResult();
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpList));
        }
        return result;
    }

    /**
     * 计算结果类转string类型
     */
    public String toString() {
        String result;
        if (this.types == CALCResultTypes.resString) {
            result = this.results.getStrResult();
        } else if (this.types == CALCResultTypes.resInt) {
            result = Integer.toString(this.results.getIntResult());
        } else if (this.types == CALCResultTypes.resDouble) {
            result = Double.toString(this.results.getDblResult());
        } else if (this.types == CALCResultTypes.resBool) {
            if (this.results.isBooResult()) result = CALCLogMsg.True.getMsg();
            else result = CALCLogMsg.False.getMsg();
        } else if (this.types == CALCResultTypes.resBigDecimal) {
            result = this.results.getBdlResult().toString();
        } else {
            throw new CALCException(String.format(CALCLogMsg.NotValue.getMsg(),
                    CALCLogMsg.ExpString));
        }
        return result;
    }

    /**
     * 是否计算成功
     *
     * @return boolean
     */
    public boolean isOK() {
        boolean result = false;
        switch (this.status) {
            case OK:
                result = true;
                break;
            case ERROR:
                result = false;
                break;
        }
        return result;
    }

    /**
     * 是否计算失败
     *
     * @return boolean
     */
    public boolean isERROR() {
        boolean result = false;
        switch (this.status) {
            case ERROR:
                result = true;
                break;
            case OK:
                result = false;
                break;
        }
        return result;
    }
}