/**
 * kstrap JavaScript命名空间说明
 */
window.K = {
    init: {
        form: {}, field: {}
    },
    form: {},
    field: {
        text: {}, display: {}, date: {}, time: {}, textarea: {},
        checkbox: {}, radio: {},
        select: {}, select2: {}, mselect: {}, tree: {}, mtree: {}, btndate: {},
        btnselect: {}, btnmselect: {}, btntree: {},
        search: {}, mInput: {}, bswitch: {}, mselecttree: {}
    }
};


window['K'] = K;

K.argsCall = Tools.argsCall;


K.logerror = Tools.logerror;

K.logdebug = Tools.logdebug;

/**
 * 执行事件属性定义的名称
 * K.doEvent(scope, fn, ...)
 */
K.doEvent = Tools.doEvent;


/**
 * 触发K.onchange绑字的事件
 */
K.firechange = function (field, callback) {
    var $field = $(field);
    if ($field.length == 0) {
        return;
    }
    $field.data('k-firechange-callback', callback);
    if (navigator.userAgent.indexOf("MSIE") > 0) {//IE
        //$field[0].fireEvent('keyup');
        $field.trigger('keyup');
    } else {//非IE
        $field.trigger('input');
    }
};


//用于input type="text"的绑定输入字段的change事件
K.onchange = function (field, fn) {
    var $field = $(field);
    if ($field.length == 0) {
        return;
    }
    //获取change事件执行函数队列
    var fns = $field.data('k-on-change-functions');
    if (fns == null) {//取不到则创建
        fns = [];
    }
    fns.push(fn);//添加新注册的函数入队列
    //重新设置队列记录
    $field.data('k-on-change-functions', fns);

    var f = function (e) {
        //保存最后按键时间，以便在加载数据的函数里判断延时时间
        $field.data('k-field-last-keyup', (new Date()).getTime());
        //延时调用加载查找结果的函数
        window.setTimeout(function () {
            K.onchange.change.call($field[0], e);
        }, K.onchange.KEYDOWN_DELAY + 1);
    };

    if (navigator.userAgent.indexOf("MSIE") > 0) {//IE
        //Tools.attchEvent($field[0], 'propertychange', f);
        $field.on('keyup', f);
        $field.on('blur', function (e) {
            K.onchange.change.call(this, e);
        });
    } else {//非IE
        $field.on('input', f);
    }
};


/**
 * change在KEYDOWN后被触发的延时时间
 */
K.onchange.KEYDOWN_DELAY = 400;

K.onchange.change = function (e) {
    var $this = $(this);
    var lastkeydown = $this.data('k-field-last-keyup');
    if (lastkeydown == null) {
        return;
    }
    //判断输入停顿时间是否达到延时时间
    if (lastkeydown + K.onchange.KEYDOWN_DELAY > (new Date()).getTime()) {
        //未达到延时时间，直接退出
        return;
    }
    var val = $this.val();
    if (val == $this.attr('data-k-onchange-last-value')) {
        return;
    }
    //获取change事件执行函数队列
    var fns = $this.data('k-on-change-functions');
    if (fns != null) {
        //执行函数队列里的函数
        for (var i = 0; i < fns.length; i++) {
            fns[i].call(this, e);
        }
    }
    $this.attr('data-k-onchange-last-value', val);
    //是否有绑定callback函数
    var callback = $this.data('k-firechange-callback');
    if (callback != null && typeof callback == 'function') {
        callback.call(this, e);
    }
    //callback函数只回调一次，调用完成后清除掉
    $this.data('k-firechange-callback', null);
};


/**
 * 初始化所有kstrap控件
 */
K.init = function (selector) {
    if (selector == null) {
        selector = 'body';
    }
    var k, v;
    for (k in K.init) {
        v = K.init[k];
        if (typeof v == 'function' && k != 'bind') {
            v(selector);
        }
    }
    var $obj;
    if (typeof selector == 'string') {
        $obj = $(selector);
    } else {
        $obj = selector;
    }
    if ($obj.length == 0) {
        return;
    }
    //为面板标题里的折叠按钮添加处理
    $obj.find('.panel-heading > .k-btn-collapse').each(function () {
        var $this = $(this);
        if ($this.attr('data-k-inited') == 'true') {
            return;
        }
        $this.attr('data-k-inited', 'true');
        $this.addClass('close glyphicon glyphicon-chevron-down')
        //$this.addClass('searchicon-down')
            .click(function () {
                var $ths = $(this),
                    $panel = $ths.parent(".panel-heading").parent('.panel');
                //	$panel.find('.panel-body').collapse('toggle');

                if ($ths.hasClass('glyphicon-chevron-up')) {
                    //if($ths.hasClass('searchicon-up')){
                    $ths.attr("title", "隐藏");
                    $panel.find('.panel-body').show();
                    $panel.find('.panel-toolbar,.panel-footer').show();
                    $ths.removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
                    //	$ths.removeClass('searchicon-up').addClass('searchicon-down');
                } else {
                    $ths.attr("title", "显示");
                    $panel.find('.panel-body').hide();
                    $panel.find('.panel-toolbar,.panel-footer').hide();
                    $ths.removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
                    //	$ths.removeClass('searchicon-down').addClass('searchicon-up');
                }
            });
        $this.parents('.panel').find('.panel-body').addClass('collapse in');
    });
};


K.documentClickHide = function (elClass, listClass, fn) {
    var $document = $(document);
//	var $input = $document.find("input."+elClass);
    var len = K.documentClickHide.classes.length;
    for (var i = 0; i < len; i++) {
        var o = K.documentClickHide.classes[i];
        if (o.el == elClass) {
            return;
        }
    }
    K.documentClickHide.classes.push({el: elClass, list: listClass, fn: fn});
};

K.documentClickHide.classes = [];

K.findListField = function (srcElement, listClass) {
    return $(srcElement).parents('.' + listClass).length > 0;
};


$(function () {
    $(document).click(function (e) {
        //iframe页面中存在k.js的情况下。
        if (window.frameElement && window.parent && window.parent.K && window.parent.K.index) {
            window.parent.$(window.parent.document).click();
        }
        var ev = e.srcElement || e.target;
        var len = K.documentClickHide.classes.length;

        var flag_date = false;//判定日期控件的下拉框是否出现，默认为隐藏
        var $input = $('input.k-field-date');
        $input.each(function () {
            var $this = $(this);
            var datefocus = $this.attr('data-k-i-am-focus');
            if (datefocus == 'true') {
                flag_date = true;//表示日期下拉框出现
            }
        })
        for (var i = 0; i < len; i++) {
            var classes = K.documentClickHide.classes[i];
            //window.setTimeout((function(cls){
            // 点击select控件以外的元素时，隐藏控件

            if (!$(ev).hasClass(classes.el)
                && !K.findListField(ev, classes.list)) {// 点击没找到该div
                classes.fn.call(ev);
            }
            var classes_cur = null;
            var classes_next = null;
            if (i < len - 1 && i != 0) {//遍历当前页面有多少个下拉框的控件
                var j = i + 1;
                classes_cur = K.documentClickHide.classes[i];
                classes_next = K.documentClickHide.classes[j];
            }
            if (i == len - 1 && i != 0) {
                var j = i - 1
                classes_cur = K.documentClickHide.classes[i];
                classes_next = K.documentClickHide.classes[j];
            }
            if (i == 0) {
                classes_cur = K.documentClickHide.classes[i];
            }

            var flag = K.findListField(ev, classes_cur.list) || $(ev).hasClass(classes_cur.el);
            var flag_next = false;
            if (classes_next) {
                flag_next = K.findListField(ev, classes_next.list) || $(ev).hasClass(classes_next.el);
            }

            //报表的显示与隐藏
            if ((flag || flag_next) || flag_date) {//点击找到下拉框，如果存在下拉框显示出来，报表隐藏

                onFeildShow($(ev));//报表隐藏

            } else {
                onFeildHide($(ev));//报表显示
            }


            //})(classes),1);
        }
    });

    if (window.frameElement) {
        window.parent.$(window.frameElement).data('child-K', K);
    }
});


//主页面的相关函数
K.index = {};

K.index.module = function (moduleid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.module) {
        window.parent.K.index.module(moduleid);
    } else {
        K.logerror('找不到主窗体页面');
    }
};

K.index.home = function (moduleid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.home) {
        window.parent.K.index.home(moduleid);
    } else {
        K.logerror('找不到主窗体页面');
    }
};

K.index.openPage = function (conf, params) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.openPage) {
        window.parent.K.index.openPage(conf, params);
    } else {
        K.logerror('找不到主窗体页面');
    }
};

K.index.closePage = function (menuid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.closePage) {
        window.parent.setTimeout(function () {
            window.parent.K.index.closePage(menuid);
        }, 10);
    } else {
        K.logerror('找不到主窗体页面');
    }
};


K.index.getPage = function (menuid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.getPage) {
        return window.parent.K.index.getPage(menuid);
    } else {
        K.logerror('找不到主窗体页面');
    }
};

K.index.getPageK = function (menuid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.getPageK) {
        return window.parent.K.index.getPageK(menuid);
    } else {
        K.logerror('找不到主窗体页面');
    }
};

/**
 *判断当前页面是否在框架子窗体内
 */
K.index.isPage = function (menuid) {
    if (window.frameElement && window.parent.K && window.parent.K.index && window.parent.K.index.getPageK) {
        return true;
    } else {
        return false;
    }
};

/**
 * html的复制
 * 把class='k-donot-init-me'的input过滤掉
 */
K.clone = function (selector) {
    if (selector == null) {
        K.logerror('K.column[1]: 没有传需要 clone的对象');
        return null;
    }
    var $selector = $(selector);
    if ($selector.length == 0) {
        K.logerror('K.column[2]: 找不到需要 clone的对象');
        return null;
    }
    var $clone = $($selector[0].outerHTML);
    if (K.clone.hiddenDiv == null) {
        //创建一个隐藏的DIV去暂存clone新创建的DOM元素
        K.clone.hiddenDiv = $('<div class="k-clone-hidde-div"></div>').css('display', 'none').appendTo(document.body);
    }
    //将新创建的DOM元素添加到HTML上下文中，以便后面调用K.init()方法初始化K对象
    K.clone.hiddenDiv.append($clone);
    //移除不初始化样式
    $clone.find(".k-donot-init").removeClass('k-donot-init');
    //初始化新创建的DOM元素K对象
    K.init($clone);
    return $clone;
}

/**
 * 定义搜索事件
 */
var searchEvent = function (params) {
    var $form = $(".k-form.search")[0];
    for (var i = 0; i < $form.length; i++) {
        params[$form[i].name] = $form[i].value;
    }

    return params;
};

/**
 * 清除搜索框的值
 */
var resetSearch = function () {
    K.form.reset($(".k-form.search"));
};



//是否打印日志
K.showLogger = false;
/**
 * 系统日志打印
 */
K.log = function (str) {
    if (window.top.K.showLogger || K.showLogger){
        console.log(str);
    }
};