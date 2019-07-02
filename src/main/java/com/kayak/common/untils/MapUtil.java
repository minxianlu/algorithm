package com.kayak.common.untils;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mxl
 * @title: MapUtil
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/27 17:39
 */
public class MapUtil {


    /**
     * 用于把List<Object>转换成Map<String,Object>形式，便于存入缓存或取数据
     *
     * @author xiongfayun
     * @param keyName 主键属性
     * @param list 集合
     * @return 返回对象
     */
    public static <T> Map<String, T> listToMap(String keyName, List<T> list) {
        Map<String, T> m = new HashMap<String, T>();
        if ((null != list) &&StringUtil.isNotEmpty(keyName)) {
            try {
                for (T t : list) {
                    PropertyDescriptor pd = new PropertyDescriptor(keyName, t.getClass());
                    // 获得get方法
                    Method getMethod = pd.getReadMethod();
                    Object o = getMethod.invoke(t);// 执行get方法返回一个Object
                    if(BeanUtil.isNotEmpty(o)){
                        m.put(o.toString(), t);
                    }
                }
                return m;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return m;
    }

    /**
     　* @description: 为Map获取初始大小
     * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:40
     　*/
    public static int getInitialCapacityForMap(int k){
        if(k<=0){
            return 16;
        }
        return (int)Math.ceil( k/0.75+1.0);
    }

}
