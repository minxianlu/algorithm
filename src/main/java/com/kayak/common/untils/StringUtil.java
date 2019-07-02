package com.kayak.common.untils;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author mxl
 * @title: StringUtil
 * @projectName algorithm_test
 * @description: 字符串工具类
 * @date 2019/6/2414:55
 */
public class StringUtil {


     /**
     　* @description: 判断字符串是否包函内容
       * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:35
     　*/
    public static boolean isExist(String content, String begin, String end) {
        String tmp = content.toLowerCase();
        begin = begin.toLowerCase();
        end = end.toLowerCase();
        int beginIndex = tmp.indexOf(begin);
        int endIndex = tmp.indexOf(end);
        if ((beginIndex != -1) && (endIndex != -1) && (beginIndex < endIndex)){
            return true;
        }
        return false;
    }

     /**
     　* @description: 去掉前面的指定字符
       * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:34
     　*/
    public static String trimPrefix(String toTrim, String trimStr) {
        while (toTrim.startsWith(trimStr)) {
            toTrim = toTrim.substring(trimStr.length());
        }
        return toTrim;
    }


    /**
    　* @description: 删除后面指定的字符
      * @ author mxl
    　* @params:
    　* @return:
    　* @date 2019/6/25 15:34
    　*/
    public static String trimSufffix(String toTrim, String trimStr) {
        while (toTrim.endsWith(trimStr)) {
            toTrim = toTrim.substring(0, toTrim.length() - trimStr.length());
        }
        return toTrim;
    }

     /**
     　* @description: 删除指定的字符
       * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:34
     　*/
    public static String trim(String toTrim, String trimStr) {
        return trimSufffix(trimPrefix(toTrim, trimStr), trimStr);
    }


    /**
    　* @description: 判断字符串是否为空
      * @ author mxl
    　* @params:
    　* @return:
    　* @date 2019/6/25 15:33
    　*/
    public static boolean isEmpty(String str) {
        if(null==str||"".equals(str.trim())){
            return true;
        }
        return false;
    }

    /**
    　* @description: 判断字符串非空
      * @ author mxl
    　* @params: str
    　* @return: boolean
    　* @date 2019/6/25 15:33
    　*/
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }



     /**
     　* @description: 首字母大写
       * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:30
     　*/
    public static String toFirstLetterUpperCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1, str.length());
    }

}
