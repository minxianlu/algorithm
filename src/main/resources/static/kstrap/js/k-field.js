

/**
 * 初始化kstrap输入控件
 */
K.init.field = function(selector){
	if(selector==null){
		selector='body';
	}
	
	var $selector;
	if(selector){
		$selector = $(selector);
	}else{
		$selector = $(document.body);
	}
	
	var $field_panel = $selector.find('.field-not-init');
	if($field_panel.length > 0){
		return;
	}
	
			
	var k, v;
	for(k in K.init.field){
		v  = K.init.field[k];
		if(typeof v=='function' && k != 'bind'){
			var $fields = v(selector);
			if($fields){
				$fields.each(function(){
					var $this = $(this);
					if($this.attr('data-k-fieldtype')){
						return;
					}
					$this.attr('data-k-fieldtype', k);
					if($this.attr('data-value')!=='' && $this.attr('data-value')!==undefined){
						$this.data('k-field-ori-value',$this.attr('data-value'));
					}else if($this.attr('value')!=='' && $this.attr('value')!==undefined){
						$this.data('k-field-ori-value',$this.attr('value'));
					}
					if($this.attr('data-allowblank')=='false'){
						K.field.allowblank($this, false);
					}
					
					//是否禁止粘贴
					if($this.attr('data-onpaste-disable') == "true"){
						$this.attr('onpaste','return false');
					}
				});
			}
		}
	}
};

/**
 * 保存各个类型的表单字段field的选择器
 */
K.field.SELECTOR = {};

/**
 * 添加一个类型的field字段选择器
 */
K.field.pushSelector = function(fieldtype, selector){
	K.field.SELECTOR[fieldtype] = selector;
}
/**
 * 获取所有字段类型的选择器
 
 */
K.field.selectorAll = function(name){
	var arr = [];
	for(var k in K.field.SELECTOR){
		arr.push(K.field.selector(k, name));
	}
	return arr.join(',');
};
/**
 * 获取某类型的字段选择器
 */
K.field.selector = function(fieldtype, name){
	var selector = K.field.SELECTOR[fieldtype];
	if(name==null){//不添加name作为选择器条件
		return selector;
	}
	//添加name作为选择器条件
	if(selector.match(/^(?:input|select|textarea|button)/)){
		//如果是标准html表单字段对象，则取name属性
		selector = selector + '[name="'+name+'"]';
	}else{
		//非标准的html表单字段对象，则取data-name属性
		selector = selector + '[data-name="'+name+'"]';
	}
	return selector;
};

/**
 * 获取k-field-xx字段的jQuery对象
 */
K.field.get = function(field, fieldname){
	var $field;
	if(fieldname==null){//直接传$field或selector
		$field = $(field);
	}else{//使用formname, fieldname的方式
		if(typeof field=='string'){//尝试使用表单名称获取
			$field = $('form[name="'+field+'"]');
		}
		//再尝试从表单里查找fieldname
		$field = $field.find(K.field.selectorAll(fieldname));
	}
	//如果找到$field则返回，找不到必须返回null
	if($field.length>0 && $field.is(K.field.selectorAll())){
		return $field;
	}
	//找不到$field必须返回null
	return null;
	
};

/**
 * 返回jQuery对象是否k-field-xx字段对象
 */
K.field.is = function($field){
	if($field==null){
		return false;
	}
	if($field.length==0){
		return false;
	}
	if(!$field.is(K.field.selectorAll())){
		K.logerror('K.field.is[1]:非法的字段对象');
		return false;
	}
	var fieldtype = $field.attr('data-k-fieldtype');
	if(fieldtype==null || fieldtype==''){
		K.logerror('K.field.is[2]:未初始化的字段对象');
		return false;
	}
	if(K.field[fieldtype]==null){
		K.logerror('K.field.is[3]:非法的字段对象');
		return false;
	}
	return fieldtype;
};


K.field.value = function(field, VALUE){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return undefined;
	}
	if(K.field[fieldtype].value==null){
		K.logerror('K.field.value[1]:组件不支持value方法');
		return undefined;
	}
	return K.field[fieldtype].value(field, VALUE);
};


K.field.text = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return undefined;
	}
	if(K.field[fieldtype].text==null){
		//K.logerror('K.field.text[1]:组件不支持text方法');
		//return undefined;
		return K.field.value(field);
	}
	return K.field[fieldtype].text(field);
};


K.field.allowblank = function(field, allowblank){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	if(allowblank){//设置允许为空
		$field.attr('data-allowblank', 'true');
		$field.removeClass('k-field-not-allowblank');
	}else{
		$field.attr('data-allowblank', 'false');
		$field.addClass('k-field-not-allowblank');
	}
	K.field.clearerr($field);
};


K.field.disable = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	if(K.field[fieldtype].disable==null){
		//没有自定义的disable方法，尝试使用默认的disabled属性
		$field.attr('disabled', 'disabled');
		//K.logerror('K.field.disable[1]:组件不支持disable方法');
		return
	}
	return K.field[fieldtype].disable(field);
};


K.field.enable = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	if(K.field[fieldtype].enable==null){
		//没有自定义的disable方法，尝试使用默认的disabled属性
		$field.removeAttr('disabled');
		//K.logerror('K.field.enable[1]:组件不支持enable方法');
		return
	}
	return K.field[fieldtype].enable(field);
};


K.field.reset = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	var ori_value = $field.data('k-field-ori-value');
	var reset_value = null;
	if(ori_value!==undefined && ori_value!==''){
		reset_value = ori_value;
	}
	K.field[fieldtype].value($field, reset_value);
	if(fieldtype == 'select' || fieldtype == 'date'){
		$field.attr("data-last-value","");//解决select在重置后再选择原先的值不会触发change事件
		
	}
	if(fieldtype == "mselect"){//解决mselect控件在重置时候，值无法清空的bug
		$field.attr("data-value","");
	}
	K.field.clearerr($field);
};


K.field.show = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	$field.parents('.k-field').show();
};


K.field.hide = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	$field.parents('.k-field').hide();
};


/**
 * 支持使用{fieldname}方式指定表单字段名称，在任意字符串中注入表单字段值
 */
K.field.computeValue = function($field, valuestr){
	if(valuestr==null){
		return valuestr;
	}
	var vals = valuestr.match(/[{]([^}]*)[}]/g);
	if(vals==null){
		return valuestr;
	}else{
		var value = valuestr;
		//尝试向上找到form表单
		var $form = $field.parents('form');
		var vals, name, val, $f;
		while(vals = value.match(/[{]([^}]*)[}]/)){
			name = vals[1];
			//尝试从表单里查找name字段
			$f = $form.find(K.field.selectorAll(name));
			if($f.length==0){
				$f = $(name);
			}
			val = K.field.value($f);
			value = value.replace(vals[0], val);
		}
		return value;
	}
};

K.field.validate = function(field){
	var $field = $(field);
//	if($field.attr("data-disabled")=="true"){
//		return true;
//	}
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	K.field.clearerr($field);
	//隐藏字段不效验
	if($field.parents('.k-field').css('display')=='none'){
		return true;
	}
	
	var value = K.field.value($field);
	var errors = [];
	
	//data-allowblank：true/false，是否允许为空
    if($field.attr("data-allowblank")=='false' && (value==null || value=='' || Tools.trim(value)=='')){
    	errors.push("该输入项不允许为空");
    }
	
	//data-validate：指定一个JS函数，用于输入验证
	var msg = K.doEvent($field[0], $field.attr("data-validate"), value);
	if(msg && msg!==true){
    	errors.push(msg);
	}

	var valueLen = Tools.gblen(value);
	//data-min-length：最小长度，提示未达到长度
	var minLength = $field.attr("data-min-length");
    if(minLength && parseInt(minLength)>valueLen && (value!=null && value!='') ){//为空时不判断位数
    	errors.push("该输入项的长度最少为"+minLength);
    }
    

	//data-max-length：最大长度，限制不能输入超长的字符
    var maxLength = Number($field.attr("data-max-length"));
    if(!isNaN(maxLength) && maxLength>0 && maxLength<valueLen && (value!=null && value!='')){//为空时不判断位数
    	errors.push("该输入项的长度最大为"+maxLength);
    	var i=Number(maxLength);
    	var subValue=value.substr(0,i);
    	K.field.value($field,subValue);
    	
    }

  	//data-regx：可指定用于验证输入的正则表达式
	var regx = $field.attr("data-regx");
	if(regx){
		if(!(new RegExp(regx)).test(value)){
			//data-regx-text：可指定正则验证失败的提示信息
			var regxtext = $field.attr("data-regx-text");
			var msg = regxtext || ('正则验证不通过' + ":" + regx);
			errors.push(msg);
		}
	}
	
	if(K.field[fieldtype].validate){
		var ret = K.field[fieldtype].validate($field);
		if(ret!==true){
			errors = errors.concat(ret);
		}
	}
	
	
	if(errors.length>0){
		if(errors.length == 1){
			K.field.errormsg($field, errors);
			return errors;		
		}else{
			errors = errors[0];
			K.field.errormsg($field, errors);
			return errors;
		}

	}
	return true;
};


K.field.dict = function(field, dict){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return undefined;
	}
	if(K.field[fieldtype].dict==null){
		K.logerror('K.field.dict[1]:组件不支持dict方法');
		return undefined;
	}
	return K.field[fieldtype].dict(field, dict);
};


K.field.reloadData = function(field, params){
	var $fields = $(field);
	$fields.each(function(){
		var $field = $(this);
		var fieldtype = K.field.is($field);
		if(!fieldtype){
			return undefined;
		}
		if(K.field[fieldtype].reloadData==null){
			K.logerror('K.field.reloadData[1]:组件不支持reloadData方法');
			return undefined;
		}
		K.field[fieldtype].reloadData(field, params);
	});
	
};


K.field.greenmsg = function(field, msgs){
	var $field = $(field);
	if(msgs==null || msgs=='' || msgs.length==0){
		$field.data('k-field-messages', null);
	}else{
		$field.data('k-field-messages', [].concat(msgs));
	}
	K.field.initPopover($field);
};


K.field.clearerr = function(field){
	var $field = $(field);
	var fieldtype = K.field.is($field);
	if(!fieldtype){
		return;
	}
	$field.removeClass('k-field-error');
	$field.data('k-field-errors', null);
	K.field.initPopover($field);
};


//添加错误信息  -->
K.field.errormsg = function(field,errors){
	var $field = $(field);
	if(errors==null || errors=='' || errors.length==0){
		$field.removeClass('k-field-error');
		$field.data('k-field-errors', null);
	}else{
		$field.addClass('k-field-error');
		$field.data('k-field-errors', [].concat(errors));
	}
	K.field.initPopover($field);
};

K.field.hideover = function($field, force, onred){
	if(onred){//去除红色框
		$field.removeClass("k-field-error");
	}
	
	if(force!==true &&
		$field.attr('data-k-i-am-focus')=='true'//有焦点
		&& //并且有提示信息，则不隐藏
		($field.data('k-field-errors') || $field.data('k-field-messages'))
	){
		
		return;
	}
	$field.popover('hide');
};

K.field.showover = function($field){
	if($field.hasClass('k-field-error') || $field.data('k-field-messages')){
		$field.popover("show");
	}
};

K.field.initPopover = function($el){
	if($el.data('k-field-popover-inited')!==true){
		$el.data('k-field-popover-inited', true)
		$el.popover({
			placement:'bottom',
			trigger:'manual',
			html: true,
			content:function(){
				var $this = $(this),
					errs = $this.data('k-field-errors'),
					msgs = $this.data('k-field-messages');
				var html = '';
				if(msgs && msgs.length>0){
					var s = '<li class="k-field-message">', e = '</li>';
					html = html + s +msgs.join(e + s) + e;
				}
				if(errs && errs.length>0){					
					var s = '<li class="k-field-error"><span class="glyphicon glyphicon-exclamation-sign"></span>',
						e = '</li>';
					html = html + s + errs.join(e + s) + e;
				}
				if(html==''){
					return false;
				}else{
					return '<ul class="k-field-popover-ul" style="width:170px;">' + html + '</ul>';
				}
			}
		}).focus(function () {
			var $this = $(this);
			if($this.data('bs.popover') && $this.data('bs.popover').tip().hasClass('in')){
				return;
			}
			
			//因为提示文字导致一些输入框无法选择数据，所以注释掉
			//K.field.showover($this);
		}).mouseenter(function () {
			var $this = $(this);
			if($this.data('bs.popover') && $this.data('bs.popover').tip().hasClass('in')){
				return;
			}
			//因为提示文字导致一些输入框无法选择数据，所以注释掉
			//K.field.showover($this);
		}).mouseleave(function(){
			K.field.hideover($(this));
		}).blur(function(){
			//K.field.hideover($(this));
		});
	}
	K.field.showover($el);
	window.setTimeout(function(){
		K.field.hideover($el,true, true);
	}, 2000);
};

