/**
 * Copyright (C), 2019-2019, 北京开科唯识技术有限公司
 * FileName: CALCResult
 * Author:   Administrator
 * Date:     2019/6/28 16:36
 * Description: 计算结果
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kayak.cloud.algorithm.comm;

import java.math.BigDecimal;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈计算结果〉
 *
 * @author Administrator
 * @create 2019/6/28
 * @since 1.0.0
 */
public class CALCResult {
    //计算日志
    private String calcLog;
    //各类计算值
    private int intResult;
    private double dblResult;
    private boolean booResult;
    private String strResult;
    private BigDecimal bdlResult;
    private Object objResult;
    private List listResult;

    public String getCalcLog() {
        return calcLog;
    }

    public void setCalcLog(String calcLog) {
        this.calcLog = calcLog;
    }

    public int getIntResult() {
        return intResult;
    }

    public void setIntResult(int intResult) {
        this.intResult = intResult;
    }

    public double getDblResult() {
        return dblResult;
    }

    public void setDblResult(double dblResult) {
        this.dblResult = dblResult;
    }

    public boolean isBooResult() {
        return booResult;
    }

    public void setBooResult(boolean booResult) {
        this.booResult = booResult;
    }

    public String getStrResult() {
        return strResult;
    }

    public void setStrResult(String strResult) {
        this.strResult = strResult;
    }

    public BigDecimal getBdlResult() {
        return bdlResult;
    }

    public void setBdlResult(BigDecimal bdlResult) {
        this.bdlResult = bdlResult;
    }

    public Object getObjResult() {
        return objResult;
    }

    public void setObjResult(Object objResult) {
        this.objResult = objResult;
    }

    public List getListResult() {
        return listResult;
    }

    public void setListResult(List listResult) {
        this.listResult = listResult;
    }

}