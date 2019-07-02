package com.kayak.algorithmtest.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author mxl
 * @title: IBatchTestService
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/27 17:25
 */
public interface IBatchTestService  {

    public MultipartFile batchTestValuation(MultipartFile file)throws Exception;
}
