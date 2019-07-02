package com.kayak.algorithmtest.service;

import com.kayak.algorithmtest.entity.*;
import com.kayak.algorithmtest.mapper.BondInfoMapper;
import com.kayak.algorithmtest.mapper.CashFlowMapper;
import com.kayak.algorithmtest.mapper.ExerciseMapper;
import com.kayak.algorithmtest.mapper.RedempVOMapper;
import com.kayak.cloud.algorithm.comm.CALCResponse;
import com.kayak.cloud.algorithm.comm.Foreign;
import com.kayak.common.exception.BusinessException;
import com.kayak.common.untils.BeanUtil;
import com.kayak.common.untils.MapUtil;
import com.kayak.common.untils.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mxl
 * @title: AssetsCaclServiceImp
 * @projectName algorithm_test1.0
 * @description: TODO
 * @date 2019/6/29 13:57
 */
@Service
public class AssetsCaclServiceImp implements IAssetsCalcService {


    @Autowired
    private BondInfoMapper bondInfoMapper;
    @Autowired
    private CashFlowMapper cashFlowMapper;
    @Autowired
    private ExerciseMapper exerciseMapper;
    @Autowired
    private RedempVOMapper redempVOMapper;

    @Override
    public BondInfo doSelect(String bondId) throws Exception {
        if (StringUtil.isEmpty(bondId)) {
            throw new BusinessException("bondId为空,请检查！");
        }
        return bondInfoMapper.getBondInfoByBondId(bondId);
    }

    @Override
    public Map<String, Object> getInterestAndInterestRate(String bondId, String settleDate) throws Exception {
        if (StringUtil.isEmpty(bondId)) {
            throw new BusinessException("bondId为空！");
        }
        if (StringUtil.isEmpty(settleDate)) {
            throw new BusinessException("结算日期为空!");
        }
        settleDate=settleDate.replaceAll("-","");
        BondInfo bondInfo = getBondInfoWithCashFlow(bondId);
        Map<String, Object> result = new HashMap<>(MapUtil.getInitialCapacityForMap(2));

        com.kayak.cloud.algorithm.model.BondInfo bondInfo1 =convertBondInfo(bondInfo);

        CALCResponse res1 = Foreign.GetAI(bondInfo1, settleDate);
        if(res1.isERROR()){
            throw new BusinessException("计算应计利息时，算法库返回异常；异常："+res1.results.getCalcLog());
        }
        result.put("interest", res1.toBigDecimal());
        return result;
    }

    @Override
    public BondInfo getBondInfoWithCashFlow(String bondId) throws Exception {
        if (StringUtil.isEmpty(bondId)) {
            throw new BusinessException("查询资产信息时,bondId为空！");
        }
        BondInfo bondInfo = bondInfoMapper.getBondInfoByBondId(bondId);
        if (BeanUtil.isEmpty(bondInfo)) {
            throw new BusinessException("未查询到资产信息！");
        }
        //这里不判断是否查到cashFlow
        List<Cashflow> cashflowList = cashFlowMapper.getCashFlowListByBondId(bondId);

        bondInfo.setCashflowList(cashflowList);
        return bondInfo;
    }

    @Override
    public Map<String, String> doCalculate(BondInfo bondInfo,String settleDate,String netPrice,String fullPrice,String ytm) throws Exception {
//        if (StringUtil.isEmpty(bondId)) {
//            throw new BusinessException("计算时,bondId为空！");
//        }
        Integer isExercise=bondInfo.getIs_exercise();
        if (StringUtil.isEmpty(settleDate)) {
            throw new BusinessException("计算时，结算日期为空!");
        }
        if (isExercise==null) {
            throw new BusinessException("计算时，是否行权参数有误！");
        }
        settleDate=settleDate.replaceAll("-","");
        Map<String,String> result=new HashMap<>(16);

        BondInfo bondInfoWithAllList = getBondInfoWithAllList(bondInfo);

        com.kayak.cloud.algorithm.model.BondInfo bondInfo1 = convertBondInfo(bondInfoWithAllList);

        Map<String,String> tempMap= netPriceAndFullPriceAndYtm(bondInfo1,settleDate,netPrice,fullPrice,ytm);
        result.putAll(tempMap);


        String duration="";
        String modDuration="";
        String convexity="";

        String resultNetPrice=result.get("netPrice");
        //资产久期
        CALCResponse durationRes = Foreign.GetDuration(settleDate, isExercise, new BigDecimal(resultNetPrice), bondInfo1);
        if(durationRes.isERROR()){
            throw new BusinessException("推算资产久期时出错，异常："+durationRes.results.getCalcLog());
        }
        duration=durationRes.toBigDecimal().toString();
        //修正久期
        CALCResponse modDurationRes = Foreign.GetModDuration(new BigDecimal(resultNetPrice), settleDate, isExercise, bondInfo1);

        if(modDurationRes.isERROR()){
            throw new BusinessException("推算修正久期时出错，异常："+modDurationRes.results.getCalcLog());
        }
        modDuration=modDurationRes.toBigDecimal().toString();
        //资产凸性
        CALCResponse convexityRes = Foreign.GetConvexity(new BigDecimal(resultNetPrice), settleDate, isExercise, bondInfo1);
        if(convexityRes.isERROR()){
            throw new BusinessException("推算资产凸性时出错，异常："+convexityRes.results.getCalcLog());
        }
        convexity=convexityRes.toBigDecimal().toString();

        //净价推收益率
//        CALCResponse ytmRes = Foreign.GetYTMByRealPrice(bondInfo1,new BigDecimal(resultNetPrice), settleDate);
//        if(ytmRes.isOK()){
//            ytm=ytmRes.toBigDecimal().toString();
//        }
        result.put("duration",duration);
        result.put("modDuration",modDuration);
        result.put("convexity",convexity);
//        result.put("ytm",ytm);

        return result;
    }

    @Override
    public Map<String, String> otherCalculate(BondInfo bondInfo, String in_date, String out_date, String trans_amtc) throws Exception {
        if(StringUtil.isEmpty(in_date)){
            throw new BusinessException("其他计算时，买入日期为空！");
        }
        if(StringUtil.isEmpty(out_date)){
            throw new BusinessException("其他计算时，卖出日期为空！");
        }
        if(StringUtil.isEmpty(trans_amtc)){
            throw new BusinessException("其他计算时，面额日期为空！");
        }
        in_date=in_date.replaceAll("-","");
        out_date=out_date.replaceAll("-","");

        BigDecimal trandAmt=new BigDecimal(trans_amtc);

        BondInfo bondInfoWithAllList=getBondInfoWithAllList(bondInfo);

        CALCResponse  calcResponse=Foreign.GetBondCashflow(convertBondInfo(bondInfoWithAllList));

        if(calcResponse.isERROR()){
            throw new BusinessException("在获取现金流时出错，异常："+calcResponse.results.getCalcLog());
        }
        List<com.kayak.cloud.algorithm.model.Cashflow> cashflowList=calcResponse.toList();
        BigDecimal cIncome=new BigDecimal(0);

        BigDecimal divisor=new BigDecimal(100);
        for(int i=0;i<cashflowList.size();i++){
            int payDate=Integer.parseInt(cashflowList.get(i).getPay_date());
            if(payDate>Integer.parseInt(in_date) &&payDate<Integer.parseInt(out_date) ){

               String couponRate=cashflowList.get(i).getCoupon_rate();
               cIncome=trandAmt.multiply(new BigDecimal(couponRate)).add(cIncome);

//                cIncome += trans_amtc*cashFlow.get(i).getCoupon_rate()/100;
            }
        }

        Map<String, String> result = new HashMap<String, String>(MapUtil.getInitialCapacityForMap(1));
        result.put("cIncome", cIncome.divide(divisor).toString());
        return result;
    }

    @Override
    public Map<String, String> netPriceAndFullPriceAndYtm(BondInfo bondInfo, String settleDate, String netPrice, String fullPrice, String ytm) throws Exception {
        BondInfo bondInfoWithAllList=getBondInfoWithAllList(bondInfo);
        Map<String,String> result= netPriceAndFullPriceAndYtm(convertBondInfo(bondInfoWithAllList),settleDate,netPrice,fullPrice,ytm);
        return result;
    }

    public Map<String, String> netPriceAndFullPriceAndYtm(com.kayak.cloud.algorithm.model.BondInfo bondInfo1, String settleDate, String netPrice, String fullPrice, String ytm) throws Exception {
        Map<String,String> result=new HashMap<>(MapUtil.getInitialCapacityForMap(3));
        if (StringUtil.isEmpty(settleDate)) {
            throw new BusinessException("计算时，结算日期为空!");
        }
        if(BeanUtil.isEmpty(bondInfo1)){
            throw new BusinessException("计算时,bondInfo为空！");
        }

        if(StringUtil.isEmpty(netPrice)&&StringUtil.isEmpty(fullPrice)&&StringUtil.isEmpty(ytm)){
            throw new BusinessException("计算时,净价、全价、收益率都为空!");
        }

        //净价不为空，则净价推全价
        if (StringUtil.isNotEmpty(netPrice)) {
            CALCResponse fullRes = Foreign.GetFullPriceByRealRate(new BigDecimal(netPrice), settleDate, bondInfo1);
            if(fullRes.isERROR()){
                throw new BusinessException("调用净价推全价的时出错，异常："+fullRes.results.getCalcLog());
            }
            fullPrice = fullRes.toBigDecimal().toString();
        } else if (StringUtil.isNotEmpty(fullPrice)) {
            CALCResponse netRes = Foreign.GetRealRateByFullPrice(new BigDecimal(fullPrice), settleDate, bondInfo1);
            if(netRes.isERROR()){
                throw new BusinessException("调用全价推净价时出错，异常："+netRes.results.getCalcLog());
            }
            netPrice = netRes.toBigDecimal().toString();
        } else {
            CALCResponse netRes = Foreign.GetRealPriceByYTM(bondInfo1,new BigDecimal(ytm), settleDate);
            if(netRes.isERROR()){
                throw new BusinessException("调用收益率推净价时出错，异常："+netRes.results.getCalcLog());
            }
            netPrice = netRes.toBigDecimal().toString();

            CALCResponse fullRes=Foreign.GetFullPriceByYTM(bondInfo1,new BigDecimal(ytm),settleDate);
            if(fullRes.isERROR()){
                throw new BusinessException("调用收益率推全价时出错，异常："+fullRes.results.getCalcLog());
            }
            fullPrice=fullRes.toBigDecimal().toString();
        }

        if(StringUtil.isEmpty(ytm)){
            CALCResponse ytmRes=Foreign.GetYTMByRealPrice(bondInfo1,new BigDecimal(netPrice),settleDate);
            if(ytmRes.isERROR()){
                throw new BusinessException("调用净价推收益率时出错，异常："+ytmRes.results.getCalcLog());
            }
            ytm=ytmRes.toBigDecimal().toString();
        }
        result.put("netPrice",netPrice);
        result.put("fullPrice",fullPrice);
        result.put("ytm",ytm);
        return result;
    }


    public BondInfo getBondInfoWithAllList(BondInfo bondInfo) throws Exception {
        if (BeanUtil.isEmpty(bondInfo)) {
            throw new BusinessException("未查询到资产信息！");
        }
        if(StringUtil.isEmpty(bondInfo.getBond_id())){
            throw new BusinessException("在获取bondInfo的所有List信息时,bondInfo中bondId为空！");
        }
        String bondId=bondInfo.getBond_id();
        List<Cashflow> cashflowList=cashFlowMapper.getCashFlowListByBondId(bondId);

        List<Exercise> exerciseList=exerciseMapper.getExerciseListByBondId(bondId);

        List<RedempVO> redempVOList=redempVOMapper.getRedempVOListByBondId(bondId);

        //还有一个浮动利率

        bondInfo.setCashflowList(cashflowList);
        bondInfo.setBondexerciseList(exerciseList);
        bondInfo.setBondredempvoList(redempVOList);

        return bondInfo;
    }

    public com.kayak.cloud.algorithm.model.BondInfo convertBondInfo(BondInfo bondInfo)throws Exception{
        if(BeanUtil.isEmpty(bondInfo)){
           throw new BusinessException("bondInfo为空！");
        }

        com.kayak.cloud.algorithm.model.BondInfo bondInfo1=new com.kayak.cloud.algorithm.model.BondInfo();
        BeanUtils.copyProperties(bondInfo,bondInfo1);

        List<com.kayak.cloud.algorithm.model.Cashflow> cashflowList1=new ArrayList<>();
        List<com.kayak.cloud.algorithm.model.Exercise> exerciseList1=new ArrayList<>();
        List<com.kayak.cloud.algorithm.model.RedempVO> redempVOList1=new ArrayList<>();
        List<com.kayak.cloud.algorithm.model.FloatRate> floatRateList1=new ArrayList<>();

        List<Cashflow> cashflowList=bondInfo.getCashflowList();
        List<Exercise> exerciseList=bondInfo.getBondexerciseList();
        List<RedempVO> redempVOList=bondInfo.getBondredempvoList();
        List<FloatRate> floatRateList=bondInfo.getBondfloatrateList();
        if(BeanUtil.isNotEmpty(cashflowList)){
            for (Cashflow cashflow : cashflowList) {
                com.kayak.cloud.algorithm.model.Cashflow cashflow1=new com.kayak.cloud.algorithm.model.Cashflow();
                BeanUtils.copyProperties(cashflow,cashflow1);
                cashflowList1.add(cashflow1);
            }
        }
        if(BeanUtil.isNotEmpty(exerciseList)){
            for (Exercise exercise : exerciseList) {
                com.kayak.cloud.algorithm.model.Exercise exercise1=new com.kayak.cloud.algorithm.model.Exercise();
                BeanUtils.copyProperties(exercise,exercise1);
                exerciseList1.add(exercise1);
            }
        }
        if(BeanUtil.isNotEmpty(redempVOList)){
            for (RedempVO redempVO : redempVOList) {
                com.kayak.cloud.algorithm.model.RedempVO redempVO1=new com.kayak.cloud.algorithm.model.RedempVO();
                BeanUtils.copyProperties(redempVO,redempVO1);
                redempVOList1.add(redempVO1);
            }
        }
        if(BeanUtil.isNotEmpty(floatRateList)){
            for (FloatRate floatRate : floatRateList) {
                com.kayak.cloud.algorithm.model.FloatRate floatRate1=new com.kayak.cloud.algorithm.model.FloatRate();
                BeanUtils.copyProperties(floatRate,floatRate1);
                floatRateList1.add(floatRate1);
            }
        }
        bondInfo1.setCashflowList(cashflowList1);
        bondInfo1.setBondexerciseList(exerciseList1);
        bondInfo1.setBondfloatrateList(floatRateList1);
        bondInfo1.setBondredempvoList(redempVOList1);
        return bondInfo1;
    }

}
