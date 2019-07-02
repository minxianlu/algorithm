package com.kayak.common.exception;

/**
 * @author mxl
 * @title: BusinessException
 * @projectName algorithm_test
 * @description: 业务异常类型
 * @date 2019/6/25 11:27
 */
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    protected final String message;

    public BusinessException(String message)
    {
        this.message = message;
    }

    public BusinessException(String message, Throwable e)
    {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

}
