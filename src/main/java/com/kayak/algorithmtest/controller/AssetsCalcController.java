package com.kayak.algorithmtest.controller;

import com.kayak.algorithmtest.entity.BondInfo;
import com.kayak.algorithmtest.service.IAssetsCalcService;
import com.kayak.common.exception.BusinessException;
import com.kayak.common.untils.BeanUtil;
import com.kayak.common.untils.file.FileUploadUtils;
import com.kayak.common.untils.file.FileUtils;
import com.kayak.core.web.entity.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author mxl
 * @title: AssetsCalcController
 * @projectName algorithm_test1.0
 * @description: 资产试算
 * @date 2019/6/29 13:54
 */
@Controller
@RequestMapping("/M8A01")
public class AssetsCalcController {


    @Autowired
    private IAssetsCalcService assetsCalcService;



    @ResponseBody
    @RequestMapping(value = "/doSelect", method = RequestMethod.POST)
    public AjaxResult doSelect(String bondId){
        AjaxResult ajaxResult=null;
        try {
            BondInfo bondInfo=assetsCalcService.doSelect(bondId);
            if(BeanUtil.isEmpty(bondInfo)){
                ajaxResult=AjaxResult.error("未查询到资产信息!");
                return ajaxResult;
            }
            ajaxResult=AjaxResult.success();
            ajaxResult.put("data",bondInfo);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult=AjaxResult.error(e.getMessage());
        }
        return ajaxResult;
    }
    @ResponseBody
    @RequestMapping(value = "/getInterestAndInterestRate", method = RequestMethod.POST)
    public AjaxResult getInterestAndInterestRate(String bondId,String settleDate){
        AjaxResult ajaxResult=null;
        try {
            Map<String,Object> result=assetsCalcService.getInterestAndInterestRate(bondId,settleDate);
            ajaxResult=AjaxResult.success();
            ajaxResult.put("data",result);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult=AjaxResult.error(e.getMessage());
        }
        return ajaxResult;
    }
    @ResponseBody
    @RequestMapping(value = "/doCalculate", method = RequestMethod.POST)
    public AjaxResult doCalculate(HttpServletRequest request){
        AjaxResult ajaxResult=null;
        try {
//            BondInfo bondInfo= request.getParameter("bondInfo");
            request.getParameter("settleDate");
            Map<String,String> data=assetsCalcService.doCalculate( bondInfo, settleDate, nerPrice, fullPrice, ytm);
            ajaxResult=AjaxResult.success();
//            ajaxResult.put("data",data);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult=AjaxResult.error(e.getMessage());
        }
        return ajaxResult;
    }

    @ResponseBody
    @RequestMapping(value = "/netPriceAndFullPriceAndYtm", method = RequestMethod.POST)
    public AjaxResult netPriceAndFullPriceAndYtm(BondInfo bondInfo, String settleDate,String netPrice,String fullPrice,String ytm){
        AjaxResult ajaxResult=null;
        try {
            Map<String,String> data=assetsCalcService.netPriceAndFullPriceAndYtm(bondInfo,settleDate,netPrice,fullPrice,ytm);
            ajaxResult=AjaxResult.success();
            ajaxResult.put("data",data);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult=AjaxResult.error(e.getMessage());
        }
        return ajaxResult;
    }

    @ResponseBody
    @RequestMapping(value = "/otherCalculate", method = RequestMethod.POST)
    public AjaxResult otherCalculate(@RequestParam("bondInfo") BondInfo bondInfo,String in_date,String out_date,String trans_amtc){
        AjaxResult ajaxResult=null;
        try {
            Map<String,String> data=assetsCalcService.otherCalculate(bondInfo,in_date,out_date,trans_amtc);
            ajaxResult=AjaxResult.success();
            ajaxResult.put("data",data);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult=AjaxResult.error(e.getMessage());
        }
        return ajaxResult;
    }
}
