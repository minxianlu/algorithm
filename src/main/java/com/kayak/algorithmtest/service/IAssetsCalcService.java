package com.kayak.algorithmtest.service;


import com.kayak.algorithmtest.entity.BondInfo;

import java.util.Map;

/**
 * @author mxl
 * @title: IAssetsCalcService
 * @projectName algorithm_test1.0
 * @description: TODO
 * @date 2019/6/29 13:57
 */

public interface IAssetsCalcService  {


     /**
     　* @description: 通过bondId查询BondInfo信息;
       * @ author mxl
     　* @params:
     　* @return: 
     　* @date 2019/6/30 14:56 
     　*/
    BondInfo doSelect(String bondId)throws Exception;

     /**
     　* @description: 获取应计利息和票面利率
       * @ author mxl
     　* @params:
     　* @return: map中有两个结果，key:interest;value:应计利息；key:couponRate;value:票面利率
     　* @date 2019/6/30 14:56
     　*/
    Map<String,Object> getInterestAndInterestRate(String bondId,String settleDate)throws Exception;
    

    Map<String,String> doCalculate(BondInfo bondInfo,String settleDate,String netPrice,String fullPrice,String ytm)throws Exception;


    BondInfo getBondInfoWithCashFlow(String bondId)throws Exception;


    Map<String,String> netPriceAndFullPriceAndYtm(BondInfo bondInfo,String settleDate,String netPrice,String fullPrice,String ytm)throws Exception;


    Map<String,String> otherCalculate(BondInfo bondInfo,String in_date,String out_date,String trans_amtc)throws Exception;

//    BondInfo getBondInfoWithAllList(String bondId)throws Exception;



}
