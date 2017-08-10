//记录当前编辑的行的下标
var existEditIndex=-1;
$(function(){
	var columns = [];
	$('#ordersgrid').datagrid({
		columns:columns,
		singleSelect:true,
		showFooter:true,//显示行脚
		fitColumns:true,
		toolbar: [{
			text:'新增',
			iconCls: 'icon-add',
			handler: function(){
				//如果存在编辑的行
				if(existEditIndex > -1){
					$('#ordersgrid').datagrid('endEdit',existEditIndex);
				}
				
				//新增加一行
				$('#ordersgrid').datagrid('appendRow',{
					num: 0,
					money: 0
				});
				
				//获取所有行
				var rows = $('#ordersgrid').datagrid('getRows');
				existEditIndex = rows.length - 1;
				
				//如何取出最后一行的下标
				$('#ordersgrid').datagrid('beginEdit',existEditIndex);
				
				//绑定keyup事件
		    	bindGridEvent();
			}
		},'-',{
			text:'提交',
			iconCls: 'icon-save',
			handler: function(){
				//如果存在编辑的行
				if(existEditIndex > -1){
					$('#ordersgrid').datagrid('endEdit',existEditIndex);
				}
				var submitData = $('#orderForm').serializeJSON();
				//alert(JSON.stringify(submitData));
				//{"t.supplieruuid":1}
//				if(submitData['t.supplieruuid'] == ""){
//					$.messager.alert('提示',"请选择供应商",'info');
//					return;
//				}
				
				var rows = $('#ordersgrid').datagrid('getRows');
				//获取所有数据
                for(var i=0;i<rows.length;i++){
                	//相同商品不能提交
                    for(var j=i+1;j<rows.length;j++)
                    if(rows[i].goodsuuid == rows[j].goodsuuid){
                        $.messager.alert('提示','商品相同','info');
                        return;
                    }
                    //数量为0不能提交
                    if (rows[i].num <=0) {
                    	 $.messager.alert('提示','数量不能为0','info');
                         return;
					}
                }
				if(submitData['t.orderuuid'] == ""){
					$.messager.alert('提示',"请选择原订单编号",'info');
					return;
				}
				var rows = $('#ordersgrid').datagrid('getRows');
				//alert(JSON.stringify(rows));
				submitData.json=JSON.stringify(rows);
				//alert(JSON.stringify($('#ordersgrid').datagrid("getData")));
				$.ajax({
					url: 'returnorders_add?t.type=' + type,
					data:submitData,
					dataType:'json',
					type:'post',
					success:function(rtn){
						$.messager.alert('提示',rtn.message,'info',function(){
							if(rtn.success){
								//清空供应商
								$('#supplieruuidName').val('');
								//清空原订单
								$('#orderuuid').combogrid('clear');
								//清空表格
								$('#ordersgrid').datagrid("loadData",{total:0,rows:[],footer:[{num:'合计',money:0}]});
								
								//关闭采购申请窗口
								$('#addOrdersDlg').dialog('close');
								$('#grid').datagrid('reload');
							}
						});
					}
				});
			}
		}],
		onClickRow:function (rowIndex, rowData){
			/*
			在用户点击一行的时候触发，参数包括：
			rowIndex：点击的行的索引值，该索引值从0开始。
			rowData：对应于点击行的记录。
			*/
			//如果存在编辑的行
			if(existEditIndex > -1){
				$('#ordersgrid').datagrid('endEdit',existEditIndex);
			}
			//重新设定当前编辑的行
			existEditIndex = rowIndex;
			//开启编辑
			$('#ordersgrid').datagrid('beginEdit',existEditIndex);
			
			//绑定keyup事件
	    	bindGridEvent();
		} 

	});
	
	//加载行脚数据
	$('#ordersgrid').datagrid('reloadFooter',[{
		num: '合计',
		money: 0
	}]);
	
	//供应商下拉表格
//	$('#supplier').combogrid({    
//	    panelWidth:750,  
//	    idField:'uuid', //要提交的供应商编号   
//	    textField:'name',//显示的供应商名称
//	    url:'supplier_list?t1.type=' + type,
//	    fitColumns:true,
//	    mode:'remote',
//	    columns:[[    
//			{field:'name',title:'名称',width:100},
//			{field:'address',title:'联系地址',width:100},
//			{field:'contact',title:'联系人',width:100},
//			{field:'tele',title:'联系电话',width:100},
//			{field:'email',title:'邮件地址',width:100}  
//	    ]]    
//	}); 
	var orderuuidUrl = '';
	if (type == 1) {
		orderuuidUrl = 'orders_list?t1.type=1';
	}
	if (type == 2) {
		orderuuidUrl = 'orders_list?t1.type=2';
	}
	//原订单下拉表格
	$('#orderuuid').combogrid({    
	    panelWidth:400,
	    idField:'uuid', //要提交的原订单编号   
	    textField:'uuid',//显示原订单编号
	    url: orderuuidUrl,
	    fitColumns:true,
	    mode:'remote',
	    columns:[[    
			{field:'uuid',title:'编号',width:50},
			{field:'supplierName',title:'供应商或客户名称',width:150},
			{field:'createtime',title:'下单日期',width:100,formatter:formatDate},
			{field:'createrName',title:'下单员名称',width:100}
	    ]],
	    onSelect:function(rowIndex, rowData) {
			//清空表格
			$('#ordersgrid').datagrid("loadData",{total:0,rows:[],footer:[{num:'合计',money:0}]});
	    	//设置供应商或客户
	    	$('#supplieruuid').val(rowData.supplieruuid);
	    	$('#supplieruuidName').val(rowData.supplierName);
	    	//重新加载columns
	    	$('#ordersgrid').datagrid({
	    		columns:[[
	      				{field:'goodsuuid',title:'商品编号',width:100,editor:{type:'numberbox',options:{
	      					 disabled:true
	      				}}},
	      				{field:'goodsname',title:'商品名称',width:100,editor:{type:'combobox',options:{
	      					url:'goods_listByOrder?ordersuuid=' + rowData.uuid,
	      				    valueField:'name',
	      				    textField:'name',
	      				    onSelect:function(record){
	      				    	//采购价格
	      				    	var price = record.inprice;
	      				    	if(type == 2){
	      				    		//销售价格
	      				    		price = record.outprice;
	      				    	}
	      				    	var uuid = record.uuid;
	      				    	
	      				    	//获取编辑器，商品编号
	      				    	var goodsuuidEditor =getEditor('goodsuuid');
	      				    	//设置商品编号
	      				    	//$(goodsuuidEditor.target).numberbox('setValue', uuid);
	      				    	$(goodsuuidEditor.target).val(uuid);
	      				    	//获取价格编辑器
	      				    	var priceEditor = getEditor('price');
	      				    	//设置商品编号
	      				    	//$(priceEditor.target).numberbox('setValue', price);
	      				    	$(priceEditor.target).val(price);
	      				    	
	      				    	var numEditor = getEditor('num');
	      				    	//定位光标到输入框中
	      				    	$(numEditor.target).select();

	      				    	//绑定keyup事件
	      				    	bindGridEvent();
	      				    	
	      				    	cal();
	      						sum();

	      				    }
	      				}}},
	      				{field:'price',title:'价格',width:100,editor:{type:'numberbox',options:{
	      					 min:0,    
	      					 precision:2
	      				}}},
	      				{field:'num',title:'数量',width:100,editor:'numberbox'},
	      				{field:'money',title:'金额',width:100,editor:{type:'numberbox',options:{
	      					 min:0,    
	      					 precision:2,
	      					 disabled:true
	      				}}},			
	      				{field:'-',title:'操作',width:200,formatter: function(value,row,index){
	      					if(row.num == '合计'){
	      						return '';
	      					}
	      					return '<a href="javascript:void(0)" onclick="deleteRow(' + index + ')">删除</a>';
	      				}}
	      			]]
	    	});
	    }
	}); 
});

/**
 * 获取编辑器
 * @param _field
 * @returns
 */
function getEditor(_field){
	return $('#ordersgrid').datagrid('getEditor', {index:existEditIndex,field:_field});
}

/**
 * 计算金额
 */
function cal(){
	//获取价格
	var priceEditor = getEditor("price");
	var price = $(priceEditor.target).val();
	//获取数量
	var numEditor = getEditor("num");
	var num = $(numEditor.target).val();
	//计算金额
	var money = (num * 1) * (price * 1);
	//设置金额
	var moneyEditor = getEditor("money");
	//保留后2位有效数字
	money = money.toFixed(2);
	$(moneyEditor.target).val(money);
	
	//把金额更新到grid里面的数据源里去
	$('#ordersgrid').datagrid('getRows')[existEditIndex].money = money;
}

/**
 * 合计金额
 */
function sum(){
	//获取所有的行
	var rows = $('#ordersgrid').datagrid('getRows');
	var totalMoney = 0;
	//累计金额
	$.each(rows,function(i, row){
		totalMoney += parseFloat(row.money);
	});
	
	totalMoney = totalMoney.toFixed(2);
	//更新行脚数据
	$('#ordersgrid').datagrid('reloadFooter',[{num:'合计',money:totalMoney}]);
}

/**
 * 绑定表格事件
 */
function bindGridEvent(){
	var numEditor = getEditor('num');
	$(numEditor.target).bind('keyup',function(){
		cal();
		sum();
	});
}

/**
 * 
 * @param index 行号
 */
function deleteRow(index){
	$('#ordersgrid').datagrid('deleteRow',index);
	//如果让表格刷新
	//getData none 返回加载完毕后的数据。 
	var data = $('#ordersgrid').datagrid('getData');
	//loadData data 加载本地数据，旧的行将被移除。 
	$('#ordersgrid').datagrid('loadData',data);
}

/**
 * 日期格式化器
 * @param value
 * @returns
 */
function formatDate(value){
	if(null == value){
		return null;
	}
	return new Date(value).Format('yyyy-MM-dd');
}