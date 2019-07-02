package com.kayak.common.untils;

import java.util.Collection;

/**
 * @author mxl
 * @title: BeanUtil
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/2415:39
 */
public class BeanUtil {
    /**
     * 判断-是否不为空
     * @param o
     * @return
     */
    public static boolean isNotEmpty(Object o)
    {
        return !isEmpty(o);
    }

    /**
     * 判断-是否为空
     * @param o java.lang.Object.
     * @return boolean.
     */
    public static boolean isEmpty(Object o) {
        if(null==o || "".equals(o)){
            return true;
        }
        if (o instanceof Collection){
            if (((Collection<?>) o).isEmpty()){
                return true;
            }
        }
        return false;
    }

}
