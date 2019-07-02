package com.kayak.common.untils;

import com.kayak.common.untils.spring.SpringUtil;
import org.springframework.context.MessageSource;

/**
 * @author mxl
 * @title: MessageUtil
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/2511:17
 */
public class MessageUtil {
    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return
     */
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, null);
    }
}
