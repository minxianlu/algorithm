package com.kayak.algorithmtest.controller;

import com.kayak.algorithmtest.service.IBatchTestService;
import com.kayak.common.exception.BusinessException;
import com.kayak.common.untils.file.FileUploadUtils;
import com.kayak.common.untils.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author mxl
 * @title: M8A04Controller
 * @projectName algorithm_test
 * @description: 批量验证
 * @date 2019/6/2414:31
 */
@Controller
@RequestMapping("/M8A04")
public class FileUploadController {


    @Autowired
    private IBatchTestService batchTestService;

    @ResponseBody
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public void excelUpload(HttpServletRequest request, MultipartFile file){
        if(null==file){
            throw new BusinessException("文件为空！");
        }
        try {
            file=batchTestService.batchTestValuation(file);
            FileUploadUtils.upload("E:/A/",file,FileUtils.getFileSuffixCarryPoint(file));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("走完了");
    }


}
