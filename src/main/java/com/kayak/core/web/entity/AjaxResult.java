package com.kayak.core.web.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mxl
 * @title: AjaxResult
 * @projectName algorithm_test1.0
 * @description: 统一定义返回的结果类
 * @date 2019/6/29 14:08
 */
public class AjaxResult extends HashMap<String,Object> {

    private static final long serialVersionUID = 1L;

    //成功码
    private static final int SUCCESS_CODE=0;
    //失败码
    private static final int FAIL_CODE=1;
    //错误码
    private static final int ERROR_CODE=500;

    /**
     * 初始化一个新创建的 Message 对象
     */
    public AjaxResult()
    {
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static AjaxResult error()
    {
        return error(FAIL_CODE, "操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 内容
     * @return 错误消息
     */
    public static AjaxResult error(String msg)
    {
        return error(ERROR_CODE, msg);
    }

    /**
     * 返回错误消息,如果msg为null,则msg提示空指针异常
     *
     * @param code 错误码
     * @param msg 内容
     * @return 错误消息
     */
    public static AjaxResult error(int code, String msg)
    {
        if(null==msg){
            msg="空指针异常";
        }
        AjaxResult json = new AjaxResult();
        json.put("code", code);
        json.put("msg", msg);
        return json;
    }

    /**
     * 返回成功消息
     *
     * @param msg 内容
     * @return 成功消息
     */
    public static AjaxResult success(String msg)
    {
        AjaxResult json = new AjaxResult();
        json.put("msg", msg);
        json.put("code", SUCCESS_CODE);
        return json;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success()
    {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功消息
     *
     * @param key 键值
     * @param value 内容
     * @return 成功消息
     */
    @Override
    public AjaxResult put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
