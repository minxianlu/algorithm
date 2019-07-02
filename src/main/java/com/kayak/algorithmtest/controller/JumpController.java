package com.kayak.algorithmtest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author mxl
 * @title: JumpController
 * @projectName kafl_test
 * @description: 用于页面跳转
 * @date 2019/6/2115:36
 */
@Controller
public class JumpController {
    // 跳转首页
    @RequestMapping("/index")
    public String index()
    {
        return "index";
    }

    //资产试算跳转
    @RequestMapping("/M8A01")
    public String M8A01()
    {
        return "algorithmtest/M8A01";
    }

    //资产实际利率试算跳转
    @RequestMapping("/M8A02")
    public String M8A02()
    {
        return "algorithmtest/M8A02";
    }

    //买断式回购试算跳转
    @RequestMapping("/M8A03")
    public String M8A03() { return "algorithmtest/M8A03"; }

    //批量导入验证跳转
    @RequestMapping("/M8A04")
    public String M8A04() { return "algorithmtest/M8A04"; }




}
