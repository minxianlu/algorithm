var layForm=null;


var bondInfo={
    formId:$("#bondInfo"),
    getJqObjByName: function (name) {
        if ($.common.isEmpty(name)) {
            throw new Error("获取Jq对象时参数为空");
        }
        return this.formId.find("*[name=" + name + "]");
    },
    dateFormat:function(str){
        if($.common.isEmpty(str)){
            return str;
        }
        var pattern = /(\d{4})(\d{2})(\d{2})/;
        return str.replace(pattern, '$1-$2-$3');
    },
    oldBondInfo:null,

    getBondInfo:function(){
        var bondInfo=this.oldBondInfo;
        var data=$.common.getValue(this.formId);
        //is_exercise;选中获取到的值时0；未选中没有is_exercise这个属性
        data.is_exercise=$.common.isEmpty(data.is_exercise)?0:1;
        for(var key in data){
            bondInfo[key]=data[key];
        }
        return bondInfo;
    },
    //资产试算的查询
    doSelect: function (params) {
        if (params.assets_type != "1") {
            $.modal.msg("不支持的资产类型");
            return false;
        }
        var bondId = params.market + params.assets_code;
        $.request.post("/M8A01/doSelect", false,{bondId: bondId}, function (result) {
            if (result.code != web_status.SUCCESS) {
                $.modal.msgError(result.msg);
                bondInfo.formId[0].reset();
                return false;
            }
            bondInfo.oldBondInfo=result.data;
            var data = result.data;
            for (var key in data) {
                var obj = bondInfo.getJqObjByName(key);
                if (obj.length <= 0) {
                    if (key == 'fi_name') {
                        obj = bondInfo.getJqObjByName("assets_name");
                        // assetsNameObj.val(data[key]);
                    }
                    //持有期试算中的首次付息日(隐藏input)框
                    if(key=="first_pay_date"){
                        obj=holdingPeriodIncome.getJqObjByName("first_pay_date");
                    }
                }
                if ($.common.endWith(key, "date")) {
                    data[key] = bondInfo.dateFormat(data[key])
                }
                if (key == 'is_exercise') {
                    obj.prop('checked', data[key] == '1');
                    assetsCalc.exerciseChange(data[key]=='1');
                }
                obj.val(data[key]);
            }

        })
    },
}


var assetsCalc = {
    //资产的form表单Id
    formId: $("#assetsCalc"),
    //通过name获取该formId对象的jq对象
    getJqObjByName: function (name) {
        if ($.common.isEmpty(name)) {
            throw new Error("获取Jq对象时参数为空");
        }
        return this.formId.find("*[name=" + name + "]");
    },
    //资产试算的计算
    assetsCalculate: function (params) {
        var info=bondInfo.getBondInfo();

        if ($.common.isEmpty(params.settle_date)) {
            $.modal.msg("计算时，结算日期不允许为空!");
            return false;
        }
        if (params.settle_date == info.end_date) {
            $.modal.msg("计算时，结算日不能等于到期日");
            return false;
        }
        //是否含权的checkbox，未选中则params中没有is_exercise参数，选中is_exercise参数为0

        var bondId=info.market+info.assets_code;
        var interestData = getInterestAndInterestRate(bondId, params.settle_date);
        if ($.common.isEmpty(interestData.interest)) {
            // $.modal.msg("未查询到应计利息");
            return false;
        }
        this.getJqObjByName("accrued_interest").val(interestData.interest.toFixed(5))

        var flag = $.common.isEmpty(params.net_price) && $.common.isEmpty(params.full_price) && $.common.isEmpty(params.exercise_rate) && $.common.isEmpty(params.end_rate);
        if (flag) {
            $.modal.alertWarning("已计算应计利息，要计算其他值，请输入百元净价、百元全价、到期收益率、行权收益率中的一个");
            return false;
        }
        params.bondId = bondId;
        params.ytm=info.is_exercise==0?params.end_rate:params.exercise_rate;

        var calcData=doCalculate(info,params);
        if($.common.isEmpty(calcData)){
            // $.modal.msg("计算结果为空！");
            return false;
        }
        // params.is_exercise = $.common.isEmpty(info.is_exercise)? '0' : '1';

        this.getJqObjByName("full_price").val(calcData.fullPrice.toFixed(4));
        this.getJqObjByName("net_price").val(calcData.netPrice.toFixed(4));
        this.getJqObjByName("duration").val(calcData.duration.toFixed(4));
        this.getJqObjByName("mod_duration").val(calcData.modDuration.toFixed(4));
        this.getJqObjByName("convexity").val(calcData.convexity.toFixed(4));
        this.getJqObjByName(info.is_exercise==0?"end_rate":"exercise_rate").val(calcData.ytm.toFixed(4));

        if ($.common.isNotEmpty(params.trans_amt)) {
            var totalInterest = params.trans_amt * interestData.interest / 100 * 10000;
            var settleAmt = calcData.fullPrice * params.trans_amt * 100;
            this.getJqObjByName("total_interest").val(totalInterest.toFixed(2));
            this.getJqObjByName("settle_amt").val(settleAmt.toFixed(2));//结算金额
        }
        var D = parseFloat(calcData.modDuration);
        var P0 = parseFloat(calcData.fullPrice);
        var PVBP = D * P0 * 0.0001;
        this.getJqObjByName("PVBP").val(PVBP.toFixed(4));

        var years = getDaysFromTowDate(params.settle_date,params.end_date);
        this.getJqObjByName("remain_fixed").val(years.toFixed(2));

    },
    exerciseChange:function(check){
        this.getJqObjByName(check?"end_rate":"exercise_rate").prop("disabled",true);
        this.getJqObjByName(check?"exercise_rate":"end_rate").prop("disabled",false);

        this.getJqObjByName(check?"end_rate":"exercise_rate").addClass("layui-disabled");
        this.getJqObjByName(check?"exercise_rate":"end_rate").removeClass("layui-disabled");
    }

}
var holdingPeriodIncome={
    formId:$("#holdingPeriodIncome"),
    //通过name获取该formId对象的jq对象
    getJqObjByName: function (name) {
        if ($.common.isEmpty(name)) {
            throw new Error("获取Jq对象时参数为空");
        }
        return this.formId.find("*[name=" + name + "]");
    },
    //买入试算
    buyCalculate:function(params){
        var info=bondInfo.getBondInfo();
        if($.common.isEmpty(params.in_date)){
            $.modal.msg("买入计算时，买入日期不能为空！");
            return false;
        }
        var flag=$.common.isEmpty(params.in_net_price)&&$.common.isEmpty(params.in_full_price)&&$.common.isEmpty(params.in_rate);
        if(flag){
            $.modal.msg("请输入买入净价、全价或者到期收益率中的一个");
            return false;
        }

        params.settle_date=params.in_date;

        var bondId=info.market+info.assets_code;

        params.bondId=bondId;
        // var is_exercise=assetsCalc.getJqObjByName("is_exercise").val();
        params.net_price=params.in_net_price;
        params.full_price=params.in_full_price;
        params.ytm=params.in_rate;
        var data=netPriceAndFullPriceAndYtm(info,params);

        if($.common.isEmpty(data)){
            return false;
        }

        this.getJqObjByName("in_net_price").val(data.netPrice.toFixed(4));
        this.getJqObjByName("in_full_price").val(data.fullPrice.toFixed(4));

        this.getJqObjByName("in_rate").val(data.ytm.toFixed(4));
        this.getJqObjByName("in_net_cost").val((params.trans_amtc*data.netPrice*10000/100).toFixed(4));
        this.getJqObjByName("in_full_cost").val((params.trans_amtc*data.fullPrice*10000/100).toFixed(4));
    },
    //卖出试算
    sellCalculate:function(params){
        var info=bondInfo.getBondInfo();
        if($.common.isEmpty(params.out_date)){
            $.modal.msg("买入计算时，买入日期不能为空！");
            return false;
        }
        var flag=$.common.isEmpty(params.out_net_price)&&$.common.isEmpty(params.out_full_price)&&$.common.isEmpty(params.out_rate);
        if(flag){
            $.modal.msg("请输入卖出净价、全价或者到期收益率中的一个");
            return false;
        }
        params.settle_date=params.out_date;

        var bondId=info.market+info.assets_code;
        params.bondId=bondId;
        params.net_price=params.out_net_price;
        params.full_price=params.out_full_price;
        params.ytm=params.out_rate;
        var data=netPriceAndFullPriceAndYtm(info,params);


        // var data=doCalculate(params);
        if($.common.isEmpty(data)){
            return false;
        }

        this.getJqObjByName("out_net_price").val(data.netPrice.toFixed(4));
        this.getJqObjByName("out_full_price").val(data.fullPrice.toFixed(4));

        this.getJqObjByName("out_rate").val(data.ytm.toFixed(4));
        this.getJqObjByName("out_net_cost").val((params.trans_amtc*data.netPrice*10000/100).toFixed(4));
        this.getJqObjByName("out_full_cost").val((params.trans_amtc*data.fullPrice*10000/100).toFixed(4));
    },
    otherCalculate:function(params){
        var info=bondInfo.getBondInfo();

        var v={'bondInfo':info,'in_date':params.in_date,'out_date':params.out_date,'trans_amtc':params.trans_amtc};
        $.request.post("/M8A01/otherCalculate",true,v,function(result){
            if (result.code != web_status.SUCCESS) {
                $.modal.msgError(result.msg);
                return false;
            }
            var cIncome=result.data.cIncome;
            var out_full_cost=holdingPeriodIncome.getJqObjByName("out_full_cost").val()+cIncome;
            holdingPeriodIncome.getJqObjByName("center_income").val(cIncome);//中间收息
            holdingPeriodIncome.getJqObjByName("out_full_cost").val(out_full_cost);//卖出全价金额
        })


        //回显最后一行数据
        var total_income=0,interest_income=0,holding_rate=0;
        var in_full_cost=this.getJqObjByName("in_full_cost").val();//买入全价金额
        var out_full_cost=this.getJqObjByName("out_full_cost").val();//卖出全价金额
        // var years=getDateDays($('input[name=in_date]').val(),$('input[name=out_date]').val());
        var years=getDaysFromTowDate(params.in_date,params.out_date);
        params.in_cost=$.common.isEmpty(params.in_cost)?0:params.in_cost;
        params.out_cost=$.common.isEmpty(params.out_cost)?0:params.out_cost;


        total_income=out_full_cost-in_full_cost-params.in_cost-params.out_cost;//总收益
        interest_income=params.trans_amtc*info.coupon_rate*years*10000/100;//利息收入
        holding_rate=total_income/in_full_cost/years*100;//持有期收益率

        this.getJqObjByName("holding_rate").val(holding_rate);//持有期收益率
        this.getJqObjByName("interest_income").val(interest_income);//利息收入
        this.getJqObjByName("margin_income").val(total_income-interest_income);//价差收入
        this.getJqObjByName("total_income").val(total_income);//总收益
    }


}




//获取应计利息和票面利率
var getInterestAndInterestRate = function (bondId, settle_date) {
    var data = {};
    if ($.common.isEmpty(bondId)) {
        $.modal.msg("bondId为空！");
        return data;
    }
    if ($.common.isEmpty(settle_date)) {
        $.modal.msg("结算日期为空！");
        return data;
    }

    $.request.post("/M8A01/getInterestAndInterestRate", false,{"bondId": bondId,"settleDate": settle_date}, function (result) {
        if (result.code != web_status.SUCCESS) {
            $.modal.msgError(result.msg);
            return false;
        }
        data = result.data;
        for(var key in data){
            // if($.common.isEmpty(data[key])){
            //     data[key]="未查询到结果！";
            //     continue;
            // }
            for(var key in result.data){
                data[key]=parseFloat(result.data[key]);
            }
        }


    })
    return data;
}

//计算；要自己处理结果集
var doCalculate=function(bondInfo,params){
    var data={};
    if($.common.isEmpty(params.bondId)){
        $.modal.msgError("计算时，bond_id为空！");
        return data;
    }
    if($.common.isEmpty(params.settle_date)){
        $.modal.msgError("计算时，settle_date为空！");
        return data;
    }
    var v={'bondInfo':bondInfo,'settleDate':params.settle_date,'netPrice':params.net_price,'fullPrice':params.full_price,'ytm':params.ytm};
    // bondInfo.settleDate=params.settle_date;
    // bondInfo.netPrice=params.net_price;
    // bondInfo.fullPrice=params.full_price;
    // bondInfo.ytm=params.ytm;
    $.request.postQuery("/M8A01/doCalculate",false,v , function (result) {
        if (result.code != web_status.SUCCESS) {
            $.modal.msgError(result.msg);
            return false;
        }
        for(var key in result.data){
            data[key]=parseFloat(result.data[key]);
        }
    })
    return data;
}
//计算；要自己处理结果集
var netPriceAndFullPriceAndYtm=function(bondInfo,params){
    var data={};
    if($.common.isEmpty(params.bondId)){
        $.modal.msgError("计算时，bond_id为空！");
        return data;
    }
    if($.common.isEmpty(params.settle_date)){
        $.modal.msgError("计算时，settle_date为空！");
        return data;
    }
    var v={'bondInfo':bondInfo,'settleDate':params.settle_date,'netPrice':params.net_price,'fullPrice':params.full_price,'ytm':params.ytm}
    $.request.post("/M8A01/netPriceAndFullPriceAndYtm",false, v, function (result) {
        if (result.code != web_status.SUCCESS) {
            $.modal.msgError(result.msg);
            return false;
        }
        for(var key in result.data){
            data[key]=parseFloat(result.data[key]);
        }
    })
    return data;
}




//计算两个日期之间的相差年数；入参：例：start:2019-06-27；end:2020-06-27
var getDaysFromTowDate = function (start, end) {
    if($.common.isEmpty(start)||$.common.isEmpty(end)){
        $.modal.msg("计算两个日期差值时，参数为空！");
        return false;
    }
    var date1 = getDate(start);
    var date2 = getDate(end);
    return parseFloat((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24 * 365));
}


//获得时间对象
var getDate = function (date) {
    var y1 = Number(date.substring(0, 4));
    var m1 = Number(date.substring(5, 7));
    var d1 = Number(date.substring(8));
    var result = new Date(y1, m1, d1);
    return result;
}


$(function () {
    layui.use('form', function () {
        layForm = layui.form;
        layForm.render();

        //资产信息的查询
        layForm.on('submit(bondInfoSelect)', function (data) {
            bondInfo.doSelect(data.field)
            layForm.render(null, "bondInfo");
        })
        //资产试算的计算
        layForm.on('submit(assetsCalculate)', function (data) {
            assetsCalc.assetsCalculate(data.field);
            layForm.render(null, "assetsCalc");
        })
        //资产试算，是否行权checkbox的监听
        layForm.on('checkbox(is_exercise)',function(data){
            assetsCalc.exerciseChange(data.elem.checked);
        })

        //资产试算，是否行权checkbox的监听
        layForm.on('submit(buyCalculate)',function(data){
            holdingPeriodIncome.buyCalculate(data.field);
        })
        //资产试算，是否行权checkbox的监听
        layForm.on('submit(sellCalculate)',function(data){
            holdingPeriodIncome.sellCalculate(data.field);
        })
        //资产试算，是否行权checkbox的监听
        layForm.on('submit(otherCalculate)',function(data){
            holdingPeriodIncome.otherCalculate(data.field);
        })
    });

})