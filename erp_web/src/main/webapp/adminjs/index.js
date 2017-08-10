
window.onload = function(){
	$('#loading-mask').fadeOut();
}
var onlyOpenTitle="欢迎使用";//不允许关闭的标签的标题

var _menus={
		"icon":"icon-sys",
		"menuid":"0",
		"menuname":"系统菜单",
		"menus":
			[
			 	{
			 		"icon":"icon-sys","menuid":"100","menuname":"基础数据","menus":
					[
						{"icon":"icon-sys","menuid":"101","menuname":"商品类型","url":"goodstype.html"},
						{"icon":"icon-sys","menuid":"102","menuname":"商品","url":"goods.html"}						
					]
			 	}
			 ]
		};




$(function(){	
	
	showName();
	//发请求，取出菜单数据
	$.ajax({
		url:'menu_getMenuTree',
		dataType: 'json',
		type:'post',
		success:function(menu){
			//从数据库得到的菜单
			_menus = menu;
			InitLeftMenu();
		}
	});
	tabClose();
	tabCloseEven();
	
	$('#loginOut').bind("click",function(){
		$.ajax({
			url: 'login_loginOut',
			type: 'post',
			success:function(rtn){
				location.href="login.html";
			}
		});
	});
	//获得上次登录时间
	$.ajax({
		url:'login_lastDate',
		dataType: 'json',
		type:'post',
		success:function(rtn){
			if(rtn.success){
				$("#time").html(rtn.message);
			}else{
				$("#time").html(rtn.message);
			}
		}
	});
	
	startTime();
});
/**
 * 首页时间
 */
function startTime()     
{     
    var today=new Date();//定义日期对象     
    var yyyy = today.getFullYear();//通过日期对象的getFullYear()方法返回年      
    var MM = today.getMonth()+1;//通过日期对象的getMonth()方法返回月     
    var dd = today.getDate();//通过日期对象的getDate()方法返回日    
    var hh=today.getHours();//通过日期对象的getHours方法返回小时     
    var mm=today.getMinutes();//通过日期对象的getMinutes方法返回分钟     
    var ss=today.getSeconds();//通过日期对象的getSeconds方法返回秒     
    // 如果分钟或小时的值小于10，则在其值前加0，比如如果时间是下午3点20分9秒的话，则显示15：20：09     
    MM=checkTime(MM);  
    dd=checkTime(dd);  
    mm=checkTime(mm);     
    ss=checkTime(ss);      
    var day; //用于保存星期（getDay()方法得到星期编号）  
    if(today.getDay()==0)   day   =   "星期日 "   
    if(today.getDay()==1)   day   =   "星期一 "   
    if(today.getDay()==2)   day   =   "星期二 "   
    if(today.getDay()==3)   day   =   "星期三 "   
    if(today.getDay()==4)   day   =   "星期四 "   
    if(today.getDay()==5)   day   =   "星期五 "   
    if(today.getDay()==6)   day   =   "星期六 "   
    document.getElementById('nowDateTimeSpan').innerHTML=yyyy+"-"+MM +"-"+ dd +" " + hh+":"+mm+":"+ss+"   " + day;     
    setTimeout('startTime()',1000);//每一秒中重新加载startTime()方法   
}     
  
function checkTime(i)     
{     
    if (i<10){  
        i="0" + i;  
    }     
      return i;  
}  


/**
 * 显示登陆名
 */
function showName(){
	$.ajax({
		url: 'login_showName',
		dataType:'json',
		type: 'post',
		success:function(rtn){
			if(rtn.success){
				//如果登陆过了，就可以显示当前登陆用户的名称
				$('#username').html(rtn.message);
			}else{
				//没果没有登陆，则提示并跳转到登陆页面
				$.messager.alert("提示",rtn.message,'info',function(){
					location.href="login.html";
				})
			}
		}
	});
}




//初始化左侧
function InitLeftMenu() {
	$("#nav").accordion({animate:false,fit:true,border:false});
	var selectedPanelname = '';
	
	    $.each(_menus.menus, function(i, n) {
			var menulist ='';
			menulist +='<ul class="navlist">';
	        $.each(n.menus, function(j, o) {
				menulist += '<li><div ><a ref="'+o.menuid+'" href="#" rel="' + o.url + '" ><span class="icon '+o.icon+'" >&nbsp;</span><span class="nav">' + o.menuname + '</span></a></div> ';
				/* 三级菜单
				if(o.child && o.child.length>0)
				{
					//li.find('div').addClass('icon-arrow');
	
					menulist += '<ul class="third_ul">';
					$.each(o.child,function(k,p){
						menulist += '<li><div><a ref="'+p.menuid+'" href="#" rel="' + p.url + '" ><span class="icon '+p.icon+'" >&nbsp;</span><span class="nav">' + p.menuname + '</span></a></div> </li>'
					});
					menulist += '</ul>';
				}
				*/
				menulist+='</li>';
	        })
			menulist += '</ul>';
	
			$('#nav').accordion('add', {
	            title: n.menuname,
	            content: menulist,
					border:false,
	            iconCls: 'icon ' + n.icon
	        });
	
			if(i==0)
				selectedPanelname =n.menuname;
	
	    });
	
	$('#nav').accordion('select',selectedPanelname);


	$('.imgs').click(function(){
		var tabTitle = $(this).attr("nav");

		var url = $(this).attr("rel");
		var menuid = $(this).attr("ref");
		var icon = $(this).attr("icon");

		var third = find(menuid);
		if(third && third.child && third.child.length>0)
		{
			$('.third_ul').slideUp();

			var ul =$(this).parent().next();
			if(ul.is(":hidden"))
				ul.slideDown();
			else
				ul.slideUp();
		}
		else{
			addTab(tabTitle,url,icon);
			$('.navlist li div').removeClass("selected");
			$(this).parent().addClass("selected");
		}
	}).hover(function(){
		$(this).parent().addClass("hover");
	},function(){
		$(this).parent().removeClass("hover");
	});
	
	
	$('.navlist li a').click(function(){
		var tabTitle = $(this).children('.nav').text();

		var url = $(this).attr("rel");
		var menuid = $(this).attr("ref");
		var icon = $(this).find('.icon').attr('class');

		var third = find(menuid);
		if(third && third.child && third.child.length>0)
		{
			$('.third_ul').slideUp();

			var ul =$(this).parent().next();
			if(ul.is(":hidden"))
				ul.slideDown();
			else
				ul.slideUp();



		}
		else{
			addTab(tabTitle,url,icon);
			$('.navlist li div').removeClass("selected");
			$(this).parent().addClass("selected");
		}
	}).hover(function(){
		$(this).parent().addClass("hover");
	},function(){
		$(this).parent().removeClass("hover");
	});





	//选中第一个
	//var panels = $('#nav').accordion('panels');
	//var t = panels[0].panel('options').title;
    //$('#nav').accordion('select', t);
}
//获取左侧导航的图标
function getIcon(menuid){
	var icon = 'icon ';
	$.each(_menus.menus, function(i, n) {
		 $.each(n.menus, function(j, o) {
		 	if(o.menuid==menuid){
				icon += o.icon;
			}
		 })
	})

	return icon;
}

function find(menuid){
	var obj=null;
	$.each(_menus.menus, function(i, n) {
		 $.each(n.menus, function(j, o) {
		 	if(o.menuid==menuid){
				obj = o;
			}
		 });
	});

	return obj;
}

function addTab(subtitle,url,icon){
	if(!$('#tabs').tabs('exists',subtitle)){
		$('#tabs').tabs('add',{
			title:subtitle,
			content:createFrame(url),
			closable:true,
			icon:icon
		});
	}else{
		$('#tabs').tabs('select',subtitle);
		$('#mm-tabupdate').click();
	}
	tabClose();
}

function createFrame(url)
{
	var s = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:100%;height:100%;"></iframe>';
	return s;
}

function tabClose()
{
	/*双击关闭TAB选项卡*/
	$(".tabs-inner").dblclick(function(){
		var subtitle = $(this).children(".tabs-closable").text();
		$('#tabs').tabs('close',subtitle);
	})
	/*为选项卡绑定右键*/
	$(".tabs-inner").bind('contextmenu',function(e){
		$('#mm').menu('show', {
			left: e.pageX,
			top: e.pageY
		});

		var subtitle =$(this).children(".tabs-closable").text();

		$('#mm').data("currtab",subtitle);
		$('#tabs').tabs('select',subtitle);
		return false;
	});
}


//绑定右键菜单事件
function tabCloseEven() {

    $('#mm').menu({
        onClick: function (item) {
            closeTab(item.id);
        }
    });

    return false;
}

function closeTab(action)
{
    var alltabs = $('#tabs').tabs('tabs');
    var currentTab =$('#tabs').tabs('getSelected');
	var allTabtitle = [];
	$.each(alltabs,function(i,n){
		allTabtitle.push($(n).panel('options').title);
	})


    switch (action) {
        case "refresh":
            var iframe = $(currentTab.panel('options').content);
            var src = iframe.attr('src');
            $('#tabs').tabs('update', {
                tab: currentTab,
                options: {
                    content: createFrame(src)
                }
            })
            break;
        case "close":
            var currtab_title = currentTab.panel('options').title;
            $('#tabs').tabs('close', currtab_title);
            break;
        case "closeall":
            $.each(allTabtitle, function (i, n) {
                if (n != onlyOpenTitle){
                    $('#tabs').tabs('close', n);
				}
            });
            break;
        case "closeother":
            var currtab_title = currentTab.panel('options').title;
            $.each(allTabtitle, function (i, n) {
                if (n != currtab_title && n != onlyOpenTitle)
				{
                    $('#tabs').tabs('close', n);
				}
            });
            break;
        case "closeright":
            var tabIndex = $('#tabs').tabs('getTabIndex', currentTab);

            if (tabIndex == alltabs.length - 1){
                alert('亲，后边没有啦 ^@^!!');
                return false;
            }
            $.each(allTabtitle, function (i, n) {
                if (i > tabIndex) {
                    if (n != onlyOpenTitle){
                        $('#tabs').tabs('close', n);
					}
                }
            });

            break;
        case "closeleft":
            var tabIndex = $('#tabs').tabs('getTabIndex', currentTab);
            if (tabIndex == 1) {
                alert('亲，前边那个上头有人，咱惹不起哦。 ^@^!!');
                return false;
            }
            $.each(allTabtitle, function (i, n) {
                if (i < tabIndex) {
                    if (n != onlyOpenTitle){
                        $('#tabs').tabs('close', n);
					}
                }
            });

            break;
        case "exit":
            $('#closeMenu').menu('hide');
            break;
    }
}


//弹出信息窗口 title:标题 msgString:提示信息 msgType:信息类型 [error,info,question,warning]
function msgShow(title, msgString, msgType) {
	$.messager.alert(title, msgString, msgType);
}




//设置登录窗口
function openPwd() {
    $('#w').dialog({
        title: '修改密码',
        width: 300,
        modal: true,
        shadow: true,
        closed: true,
        height: 180,
        buttons:[
			{
				text:'保存',
				iconCls: 'icon-save',
				handler:function(){
					var txtOldPass = $('#txtOldPass');
					var $newpass = $('#txtNewPass');
				    var $rePass = $('#txtRePass');

				    if (txtOldPass.val() == '') {
				        msgShow('系统提示', '请输入原密码！', 'warning');
				        return false;
				    }
				    if ($newpass.val() == '') {
				        msgShow('系统提示', '请输入密码！', 'warning');
				        return false;
				    }
				    if ($rePass.val() == '') {
				        msgShow('系统提示', '请再一次输入密码！', 'warning');
				        return false;
				    }

				    if ($newpass.val() != $rePass.val()) {
				        msgShow('系统提示', '两次密码不一致！请重新输入', 'warning');
				        return false;
				    }
					//用记输入的部门信息
					var submitData={newPwd:$newpass.val(),oldPwd:txtOldPass.val()};
					$.ajax({
						url: 'emp_updatePwd',
						data: submitData,
						dataType: 'json',
						type: 'post',
						success:function(rtn){
							//{success:true, message: 操作失败}
							$.messager.alert('提示',rtn.message, 'info',function(){
								if(rtn.success){
									//关闭弹出的窗口
									$('#w').dialog('close');
									txtOldPass.val('');
									$newpass.val('');
									$rePass.val('');
								}
							});
						}
					});
				}
			},{
				text:'取消',
				iconCls:'icon-cancel',
				handler:function(){
					//关闭弹出的窗口
					$('#w').dialog('close');
				}
			}
         ]
    });
}
//关闭登录窗口
function closePwd() {
    $('#w').window('close');
}



//修改密码
function serverLogin() {
    var $newpass = $('#txtNewPass');
    var $rePass = $('#txtRePass');

    if ($newpass.val() == '') {
        msgShow('系统提示', '请输入密码！', 'warning');
        return false;
    }
    if ($rePass.val() == '') {
        msgShow('系统提示', '请在一次输入密码！', 'warning');
        return false;
    }

    if ($newpass.val() != $rePass.val()) {
        msgShow('系统提示', '两次密码不一至！请重新输入', 'warning');
        return false;
    }

    $.post('/ajax/editpassword.ashx?newpass=' + $newpass.val(), function(msg) {
        msgShow('系统提示', '恭喜，密码修改成功！<br>您的新密码为：' + msg, 'info');
        $newpass.val('');
        $rePass.val('');
        close();
    })
    
}

$(function() {

    openPwd();

    $('#editpass').click(function() {
        $('#w').window('open');
    });

    $('#btnEp').click(function() {
        serverLogin();
    })

	$('#btnCancel').click(function(){closePwd();})

   
});

function isContains(str, substr) {
    return new RegExp(substr).test(str);
}

//菜单查询
function doSearch(value){
	var items = _menus.menus;
	var subItems;
	var flag = true;
	var ifElseContain = false;
	$.each(items,function(i,row){
		subItems = row.menus;
		$.each(subItems, function(j, row2) {
			if(isContains(row2.menuname,value) && value!=""){
				addTab(row2.menuname,row2.url,row2.icon)
				$('#nav').accordion('select',row.menuname);
				$('.navlist li a').addClass("selected");
				
				flag = false;
			}
			if(flag==false){
				return false;
			}
		});
		if(flag==false){
			return false;
		}
	});
	$('#HomeFuncSearch').searchbox('setValue','');
}


