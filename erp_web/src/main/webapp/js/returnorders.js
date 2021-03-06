var oper = Request['oper'];
var type = Request['type'] * 1;
$(function(){
	var url = 'returnorders_listByPage';
	
		//订单查询
		if(oper == 'orders'){
			url += "?t1.type=" + type;
		}
		//订单审核
		if(oper == 'doCheck'){
			url +='?t1.state=0&t1.type='+type;
		}
	
		//订单入库
		if(oper == 'doInStore'){
			url +='?t1.state=1&t1.type='+type;
		}
		//订单出库
		if(oper == 'doOutStore'){
				url +='?t1.state=1&t1.type='+type;
		}
		//我的订单
		if(oper == 'returnOrder'){
			url = 'returnorders_myOrderList?t1.type=' + type;
		}
	
	//加载表格数据
	$('#grid').datagrid({
		url:url,
		columns:getColumns(),
		singleSelect: true,
		pagination: true,
		fitColumns: true,
		onDblClickRow:function(rowIndex, rowData){
			/*
			 在用户双击一行的时候触发，参数包括：
			rowIndex：点击的行的索引值，该索引值从0开始。
			rowData：对应于点击行的记录。
			*/
			$('#ordersDlg').dialog('open');
			
			$('#uuid').html(rowData.uuid);
			$('#supplierName').html(rowData.supplierName);
			$('#state').html(formatState(rowData.state));
			$('#createrName').html(rowData.createrName);
			$('#checkerName').html(rowData.checkerName);
			$('#starterName').html(rowData.starterName);
			$('#enderName').html(rowData.enderName);
			$('#createtime').html(formatDate(rowData.createtime));
			$('#checktime').html(formatDate(rowData.checktime));
			$('#starttime').html(formatDate(rowData.starttime));
			$('#endtime').html(formatDate(rowData.endtime));
			$('#waybillsn').html(rowData.waybillsn);
			$('#orderuuidTD').html(rowData.orderuuid);
			//加载明细的数据
			$('#itemgrid').datagrid('loadData',rowData.returnorderdetails);
		}
	});
	
	//明细表格
	$('#itemgrid').datagrid({
		columns:[[
			{field:'uuid',title:'编号',width:100},
			{field:'goodsuuid',title:'商品编号',width:100},
			{field:'goodsname',title:'商品名称',width:100},
			{field:'price',title:'价格',width:100},
			{field:'num',title:'数量',width:100},
			{field:'money',title:'金额',width:100},
			{field:'state',title:'状态',width:100,formatter:formatDetailState}
		]],
		singleSelect:true,
		fitColumns:true
	});
	
	//订单详情窗口的配置
	var orderDlgCfg = {
			title:'订单详情',
			height:320,
			width:750,
			modal:true,
			closed:true
	};
	//订单详情窗口 的工具栏
	var orderDlgToolbar = new Array();
	//导出
	orderDlgToolbar.push({
		text:'导出',
		iconCls:'icon-excel',
		handler:doExport
	});
	
	//审核按钮
	if(oper == 'doCheck'){
		orderDlgToolbar.push({
				text:'审核',
				iconCls:'icon-search',
				handler:doCheck
		});
	}
	//入库双击事件
	if(oper == 'doInStore' || oper == 'doOutStore'){
		$('#itemgrid').datagrid({
			onDblClickRow:function(rowIndex, rowData){
				$('#itemDlg').dialog('open');
				$('#id').val(rowData.uuid);
				$('#goodsuuid').html(rowData.goodsuuid);
				$('#goodsname').html(rowData.goodsname);
				$('#num').html(rowData.num);
			}
		});
	}
	if(oper == 'returnOrder'){
		var btnTxt = '采购退货申请';
		if(type == 2){
			btnTxt = "销售退货申请";
			$('#ordersupplier').html("客户");
		}
		$('#grid').datagrid({
			toolbar:[
			    {
			    	text:btnTxt,
			    	iconCls:'icon-add',
			    	handler:function(){
			    		$('#addOrdersDlg').dialog('open');
			    	}
			    }
			]
		});
	}
	//动态给订单详情窗口 添加工具栏
	if (orderDlgToolbar.length > 0) {
		orderDlgCfg.toolbar = orderDlgToolbar;
	}
	$('#ordersDlg').dialog(orderDlgCfg);
	
	var dlgTitle = "出库";
	if(type == 2){
		dlgTitle = "入库"
	}
	//出入库窗口
	$('#itemDlg').dialog({
		title:dlgTitle,
		width:300,
		height:200,
		modal:true,
		closed:true,
		buttons:[
		    {
		    	text:dlgTitle,
		    	iconCls:'icon-save',
		    	handler:doInOutStore
		    }
		]
	});
	
	
	
	//采购申请窗口
	$('#addOrdersDlg').dialog({
		width:710,
		height:400,
		title:'申请',
		modal:true,
		closed:true
	});
});

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

/**
 * 订单状态
 * @param value
 * @returns {String}
 */
function formatState(value){
	//采购: 0:未审核 1:已审核, 2:已入库, 3:已出库；销售：0:未入库 1:已入库
	if(type==1){
		switch(value * 1){
		case 0:return '未审核';
		case 1: return '已审核';
		case 2: return '已出库';
		case 3: return '已出库';
		default: return '';
		}
	}else{
		switch(value * 1){
		case 0:return '未审核';
		case 1: return '已审核';
		case 2: return '已审核';
		case 3: return '已入库';
		default: return '';
		}
		
	}
}

/**
 * 明细状态
 * @param value
 * @returns {String}
 */
function formatDetailState(value){
	if(type==1){
		switch(value * 1){
		case 0: return '未出库';
		case 1: return '已出库';
		default: return '';
		}
	}else{
		switch(value * 1){
		case 0: return '未入库';
		case 1: return '已入库';
		default: return '';
		}
	}
}

/**
 * 审核
 */
function doCheck(){
	$.messager.confirm("确认","确认要审核吗？",function(yes){
		if(yes){
			var url = 'returnorders_doCheck';
			$.ajax({
				url:url,
				data:{id:$('#uuid').html()},
				dataType:'json',
				type:'post',
				success:function(rtn){
					$.messager.alert("提示",rtn.message,'info',function(){
						if(rtn.success){
							$('#ordersDlg').dialog("close");
							$('#grid').datagrid('reload');
						}
					});
				}
			});
		}
	});
}

/**
 * 入库
 */
function doInOutStore(){
	var message = "确认要入库吗？";
	var url = "returnorderdetail_doInStore";
	if(type == 1){
		message = "确认要出库吗？";
		url = "returnorderdetail_doOutStore";
	}
	$.messager.confirm("确认",message,function(yes){
		if(yes){
			var submitData = $('#itemForm').serializeJSON();
			if(submitData.storeuuid == ''){
				$.messager.alert("提示","请选择仓库",'info');
				return;
			}
			$.ajax({
				url:url,
				data:submitData,
				dataType: 'json',
				type: 'post',
				success:function(rtn){
					$.messager.alert("提示",rtn.message,'info',function(){
						if(rtn.success){
							//关闭入库窗口
							$('#itemDlg').dialog('close');
							//修改明细的状态
							var row = $('#itemgrid').datagrid('getSelected');
								row.state = '1';
							//取出数据
							var data = $('#itemgrid').datagrid('getData');
							//加载本地数据
							$('#itemgrid').datagrid('loadData',data);
							
							//循环判断是否所有明细都 已经入库
							var flg = true;
							$.each(data.rows,function(i,row){
								if(row.state * 1 == 0){
									flg = false;
									return false;//跳出循环
								}
							});
							if(flg == true){
								//关闭详情窗口
								$('#ordersDlg').dialog('close');
								$('#grid').datagrid('reload');
							}
						}						
					});
				}
			});
		}
	});
}

/*function doExport(){
	$.messager.confirm("导出","确认要导出吗？",function(yes){
		if(yes){
			//$.download('returnorders_export',{id:$('#uuid').html()});
			var url = 'returnorders_export';
			$.ajax({
				url:url,
				data:{id:$('#uuid').html()},
				type:'post',
				success:function(rtn){
				
				}
			});
		}
	});
}*/

/**
 * 根据类型获取对应的列
 * @returns {Array}
 */
function getColumns(){
	if(type == 1){
		//采购退货
		return [[
		         {field:'uuid',title:'编号',width:100},
		         {field:'createtime',title:'生成日期',width:100,formatter:formatDate},
		         {field:'checktime',title:'审核日期',width:100,formatter:formatDate},
		         {field:'endtime',title:'出库日期',width:100,formatter:formatDate},
		         {field:'createrName',title:'下单员',width:100},
		         {field:'checkerName',title:'审核员',width:100},
		         {field:'enderName',title:'库管员',width:100},
		         {field:'supplierName',title:'供应商',width:100},
		         {field:'totalmoney',title:'合计金额',width:100},
		         {field:'state',title:'状态',width:100,formatter:formatState},
		         {field:'waybillsn',title:'运单号',width:100},
		         {field:'orderuuid',title:'原订单编号',width:100}
		         ]]
	}
	if(type == 2){
		//销售退货显示的列
		return [[
		         {field:'uuid',title:'编号',width:100},
		         {field:'createtime',title:'生成日期',width:100,formatter:formatDate},
		         {field:'checktime',title:'审核日期',width:100,formatter:formatDate},
		         {field:'endtime',title:'入库日期',width:100,formatter:formatDate},
		         {field:'createrName',title:'下单员',width:100},
		         {field:'checkerName',title:'审核员',width:100},
		         {field:'enderName',title:'库管员',width:100},
		         {field:'supplierName',title:'客户',width:100},
		         {field:'totalmoney',title:'合计金额',width:100},
		         {field:'state',title:'状态',width:100,formatter:formatState},
		         {field:'waybillsn',title:'运单号',width:100},
		         {field:'orderuuid',title:'原订单编号',width:100}
		         ]]
	}
}

/**
 * 导出
 */
function doExport(){
	$.download("returnorders_export",{id:$('#uuid').html()});
}