/**
 * 通用js方法封装处理
 * Copyright (c) 2019 mxl
 */
(function ($) {
    $.extend({
        common:{
            //判断对象是否为空、undefined、'';如果为空，返回true
            isEmpty:function(obj){
                if(obj==null||typeof (obj)=='undefined'||this.trim(obj)==''){
                    return true;
                }
                return false;
            },
            // 判断对象是否非空；isEmpty取反
            isNotEmpty: function (value) {
                return !$.common.isEmpty(value);
            },
            // 空对象转字符串
            nullToStr: function(value) {
                if ($.common.isEmpty(value)) {
                    return "-";
                }
                return value;
            },
            // 是否显示数据 为空默认为显示
            visible: function (value) {
                if ($.common.isEmpty(value) || value == true) {
                    return true;
                }
                return false;
            },
            // 空格截取
            trim: function (value) {
                if (value == null) {
                    return "";
                }
                return value.toString().replace(/(^\s*)|(\s*$)|\r|\n/g, "");
            },
            // 指定随机数返回
            random: function (min, max) {
                return Math.floor((Math.random() * max) + min);
            },
            // 判断字符串是否是以start开头
            startWith: function(value, start) {
                var reg = new RegExp("^" + start);
                return reg.test(value)
            },
            // 判断字符串是否是以end结尾
            endWith: function(value, end) {
                var reg = new RegExp(end + "$");
                return reg.test(value)
            },
            //判断一个对象是否为数组
            isArray:function(obj){
                if($.common.isEmpty(obj)){
                    return false;
                }
                return obj != null && typeof obj == "object" &&
                    obj.splice!=null && obj.join!=null && obj.length!=null;
            },
            //通过id获取jq对象;
            getJQObj:function(obj){
                if($.common.isEmpty(obj)){
                    throw new Error("获取Jq对象时参数为空");
                }
                if(obj instanceof jQuery&&obj.length>0 ){
                    return obj;
                }
                return obj.indexOf("#")==0?$(obj):$("#"+obj);
            },
            //获取obj下的所有input的值;返回一个对象
            getValue:function(obj){
                var params={};
                if($.common.isEmpty(obj)){
                    return params;
                }
                var $obj=$.common.getJQObj(obj);
                $.each($obj.serializeArray(),function(i,o){
                    var paramsVal=params[this.name]
                    if($.common.isNotEmpty(paramsVal)){
                       if(!$.common.isArray(paramsVal)){
                           params[this.name]=[paramsVal];
                       }
                       params[this.name].push(this.value);
                       return true;
                    }
                    params[this.name]=this.value;
                })
                return params;
            }
        },

        //向后台的请求，默认的回调操作；如需自己操作回调信息，请使用JQ的$.ajax();
        request:{
            // 提交数据
            // submit: function(url, type, dataType, data) {
            //     var config = {
            //         url: url,
            //         type: type,
            //         dataType: dataType,
            //         data: data,
            //         beforeSend: function () {
            //             $.modal.loading("正在处理中，请稍后...");
            //         },
            //         success: function(result) {
            //             $.request.ajaxSuccess(result);
            //         }
            //     };
            //     $.ajax(config)
            // },
            // // post请求传输
            // post: function(url, data) {
            //     $.request.submit(url, "post", "json", data);
            // },
            // // get请求传输
            // get: function(url) {
            //     $.request.submit(url, "get", "json", "");
            // },
            // //异步请求的成功回调
            // ajaxSuccess:function(result){
            //     if (result.code == web_status.SUCCESS ) {
            //         $.modal.msgSuccess(result.msg);
            //     } else {
            //         $.modal.alertError(result.msg);
            //     }
            //     $.modal.closeLoading();
            // },
            //post请求查询
            post:function(url,async,data,successFun){
                var config={
                    url:url,
                    type:"post",
                    async:$.common.isEmpty(async)?true:async,
                    data:data,
                    dataType:"json",
                    beforeSend: function () {$.modal.loading("正在处理中，请稍后...");},
                    success:function(result){
                        if(successFun!=null){
                            successFun(result);
                        }
                        $.modal.closeLoading();
                    }
                };
                $.ajax(config)
            },
            postQuery:function(url,async,data,successFun){
                var config={
                    url:url,
                    type:"post",
                    async:$.common.isEmpty(async)?true:async,
                    data:data,
                    contentType:"application/json",
                    dataType:"json",
                    beforeSend: function () {$.modal.loading("正在处理中，请稍后...");},
                    success:function(result){
                        if(successFun!=null){
                            successFun(result);
                        }
                        $.modal.closeLoading();
                    }
                };
                $.ajax(config)
            },

        },
        // 弹出层封装处理
        modal: {
            // 显示图标
            icon: function (type) {
                var icon = "";
                if (type == modal_status.WARNING) {
                    icon = 0;
                } else if (type == modal_status.SUCCESS) {
                    icon = 1;
                } else if (type == modal_status.FAIL) {
                    icon = 2;
                } else {
                    icon = 3;
                }
                return icon;
            },
            // 消息提示
            msg: function (content, type) {
                if (type != undefined) {
                    layer.msg(content, {icon: $.modal.icon(type), time: 1500, shift: 5});
                } else {
                    layer.msg(content);
                }
            },
            // 错误消息
            msgError: function (content) {
                $.modal.msg(content, modal_status.FAIL);
            },
            // 成功消息
            msgSuccess: function (content) {
                $.modal.msg(content, modal_status.SUCCESS);
            },
            // 警告消息
            msgWarning: function (content) {
                $.modal.msg(content, modal_status.WARNING);
            },
            // 弹出提示
            alert: function (content, type) {
                layer.alert(content, {
                    icon: $.modal.icon(type),
                    title: "系统提示",
                    btn: ['确认'],
                    btnclass: ['btn btn-primary'],
                });
            },
            // 消息提示并刷新父窗体
            msgReload: function (msg, type) {
                layer.msg(msg, {
                        icon: $.modal.icon(type),
                        time: 500,
                        shade: [0.1, '#8F8F8F']
                    },
                    function () {
                        $.modal.reload();
                    });
            },
            // 错误提示
            alertError: function (content) {
                $.modal.alert(content, modal_status.FAIL);
            },
            // 成功提示
            alertSuccess: function (content) {
                $.modal.alert(content, modal_status.SUCCESS);
            },
            // 警告提示
            alertWarning: function (content) {
                $.modal.alert(content, modal_status.WARNING);
            },
            // 打开遮罩层
            loading: function (message) {
                $.blockUI({ message: '<div class="loaderbox"><div class="loading-activity"></div> ' + message + '</div>' });
            },
            // 关闭遮罩层
            closeLoading: function () {
                setTimeout(function(){
                    $.unblockUI();
                }, 50);
            },
            // 关闭窗体
            close: function () {
                var index = parent.layer.getFrameIndex(window.name);
                parent.layer.close(index);
            },
            // 关闭全部窗体
            closeAll: function () {
                layer.closeAll();
            },
            // 确认窗体
            confirm: function (content, callBack) {
                layer.confirm(content, {
                    icon: 3,
                    title: "系统提示",
                    btn: ['确认', '取消'],
                    btnclass: ['btn btn-primary', 'btn btn-danger'],
                }, function (index) {
                    layer.close(index);
                    callBack(true);
                });
            },
            // 弹出层指定宽度
            open: function (title, url, width, height, callback) {
                //如果是移动端，就使用自适应大小弹窗
                if (navigator.userAgent.match(/(iPhone|iPod|Android|ios)/i)) {
                    width = 'auto';
                    height = 'auto';
                }
                if ($.common.isEmpty(title)) {
                    title = false;
                }
                ;
                if ($.common.isEmpty(url)) {
                    url = "/404.html";
                }
                ;
                if ($.common.isEmpty(width)) {
                    width = 800;
                }
                ;
                if ($.common.isEmpty(height)) {
                    height = ($(window).height() - 50);
                }
                ;
                if ($.common.isEmpty(callback)) {
                    callback = function (index, layero) {
                        var iframeWin = layero.find('iframe')[0];
                        iframeWin.contentWindow.submitHandler();
                    }
                }
                layer.open({
                    type: 2,
                    area: [width + 'px', height + 'px'],
                    fix: false,
                    //不固定
                    maxmin: true,
                    shade: 0.3,
                    title: title,
                    content: url,
                    btn: ['确定', '关闭'],
                    // 弹层外区域关闭
                    shadeClose: true,
                    yes: callback,
                    cancel: function (index) {
                        return true;
                    }
                });
            },
        },
    })
// laydate 时间控件绑定
    if ($(".select-time").length > 0) {
        layui.use('laydate', function() {
            var laydate = layui.laydate;
            var startDate = laydate.render({
                elem: '#startTime',
                max: $('#endTime').val(),
                theme: 'molv',
                trigger: 'click',
                done: function(value, date) {
                    // 结束时间大于开始时间
                    if (value !== '') {
                        endDate.config.min.year = date.year;
                        endDate.config.min.month = date.month - 1;
                        endDate.config.min.date = date.date;
                    } else {
                        endDate.config.min.year = '';
                        endDate.config.min.month = '';
                        endDate.config.min.date = '';
                    }
                }
            });
            var endDate = laydate.render({
                elem: '#endTime',
                min: $('#startTime').val(),
                theme: 'molv',
                trigger: 'click',
                done: function(value, date) {
                    // 开始时间小于结束时间
                    if (value !== '') {
                        startDate.config.max.year = date.year;
                        startDate.config.max.month = date.month - 1;
                        startDate.config.max.date = date.date;
                    } else {
                        startDate.config.max.year = '';
                        startDate.config.max.month = '';
                        startDate.config.max.date = '';
                    }
                }
            });
        });
    }
    // laydate time-input 时间控件绑定
    if ($(".time-input").length > 0) {
        layui.use('laydate', function() {
            var laydate = layui.laydate;
            var times = $(".time-input");
            // 控制控件外观
            var type = times.attr("data-type") || 'date';
            // 控制回显格式
            var format = times.attr("data-format") || 'yyyy-MM-dd';
            for (var i = 0; i < times.length; i++) {
                var time = times[i];
                laydate.render({
                    elem: time,
                    theme: 'molv',
                    trigger: 'click',
                    type: type,
                    format: format,
                    done: function(value, date) {}
                });
            }
        });
    }
})(jQuery);

/** 消息状态码 */
web_status = {
    SUCCESS: 0,
    FAIL: 1,
    ERROR:500
};

/** 弹窗状态码 */
modal_status = {
    SUCCESS: "success",
    FAIL: "error",
    WARNING: "warning"
};