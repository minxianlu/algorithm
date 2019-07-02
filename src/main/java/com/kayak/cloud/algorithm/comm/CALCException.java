/**
 * Copyright (C), 2019-2019, 北京开科唯识技术有限公司
 * FileName: CALCException
 * Author:   Administrator
 * Date:     2019/6/24 14:46
 * Description: 计算异常
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kayak.cloud.algorithm.comm;

/**
 * 〈一句话功能简述〉<br> 
 * 〈计算异常〉
 *
 * @author Administrator
 * @create 2019/6/24
 * @since 1.0.0
 */
public class CALCException extends ArithmeticException{
    private static final long serialVersioenUID = 3730672311250820080L;

    public CALCException() {
        super();
    }

    public CALCException(String msg) {
        super(msg);
    }
}