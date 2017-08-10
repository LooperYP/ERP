var oper = Request['oper'];
$(function(){
	$('#grid').datagrid({
		singleSelect: true,
		pagination: true,
		columns:[[
		          {field:'uuid',title:'编号',width:100},
		          {field:'goodsName',title:'商品',width:100},
		          {field:'storeName',title:'仓库',width:100},
		          {field:'num',title:'数量',width:100},
		          {field:'type',title:'类型',width:100,formatter:formatType},
		          {field:'createtime',title:'登记日期',width:100,formatter: formatDate},
		          {field:'checktime',title:'审核日期',width:100,formatter: formatDate},
		          {field:'createrName',title:'登记人',width:100},
		          {field:'checkerName',title:'审核人',width:100},
		          {field:'state',title:'状态',width:100,formatter:formatState},
		          {field:'remark',title:'备注',width:100}
		          ]],
	})
	
	//增加盘盈盘亏登记按钮
	if(oper == 'doCreate'){
		$('#grid').datagrid({
			url:'inventory_listByPage?t1.state=0',
			toolbar:[
			    {
			    	text:'盘盈盘亏登记',
			    	iconCls:'icon-add',
			    	handler:function(){
			    		$('#addInvertoryForm').form('clear');//Form表单数据清空功能
			    		$('#addInvertoryDlg').dialog('open');
			    	}
			    }
			]
		});
	}
	//增加盘盈盘亏双击审核功能
	if(oper == 'doCheck'){
		$('#grid').datagrid({
			url:'inventory_listByPage?t1.state=0',
			onDblClickRow:function(rowIndex, rowData){
				$('#editDlg').dialog('open');
				$('#uuid').html(rowData.uuid);
				$('#createtime').html(formatDate(rowData.createtime));
				$('#goodsName').html(rowData.goodsName);
				$('#storeName').html(rowData.storeName);
				$('#num').html(rowData.num);
				$('#type').html(formatType(rowData.type));
				$('#remark').html(rowData.remark);
			}
		});
	}
	
	if(oper=='query'){
		$('#grid').datagrid({
			url:'inventory_listByPage',
		});
	}
	
	//出入库窗口
	$('#editDlg').dialog({
	    title:"盘盈盘亏审核",
		width:300,
		height:280,
		modal:true,
		closed:true,
		buttons:[
		    {
		    	text:"审核",
		    	iconCls:'icon-save',
		    	handler:doCheck
		    }
		]
	});
	
	//初始化编辑窗口
	$('#addInvertoryDlg').dialog({
		title: '盘盈盘亏登记',//窗口标题
		width: 260,//窗口宽度
		height: 200,//窗口高度
		closed: true,//窗口是是否为关闭状态, true：表示关闭
		modal: true,//模式窗口
		buttons:[{
			text:'保存',
			iconCls: 'icon-save',
			handler:function(){
				//当所有验证都通过后，才可提交
				if(!$('#addInvertoryForm').form('validate')){
					return;
				}
				//用于记录Invertory信息
				var submitData= $('#addInvertoryForm').serializeJSON();
				$.ajax({
					url: "inventory_add",
					data: submitData,
					dataType: 'json',
					type: 'post',
					success:function(rtn){
						//{success:true, message: 操作失败}
						$.messager.alert('提示',rtn.message, 'info',function(){
							if(rtn.success){
								//关闭弹出的窗口
								$('#addInvertoryDlg').dialog('close');
								//刷新表格
								$('#grid').datagrid('reload');
							}
						});
					}
				});
			}
		},{
			text:'关闭',
			iconCls:'icon-cancel',
			handler:function(){
				//关闭弹出的窗口
				$('#addInvertoryDlg').dialog('close');
			}
		}]
	});
	
	
	//点击查询按钮
	$('#btnSearch').bind('click',function(){
		//把表单数据转换成json对象
		var formData = $('#searchForm').serializeJSON();
		$('#grid').datagrid('load',formData);
	});
});


/**
 * 审核
 */
function doCheck(){
	$.messager.confirm("确认","确认要审核吗？",function(yes){
		if(yes){
			$.ajax({
				url:'inventory_doCheck',
				data:{id:$('#uuid').html()},
				dataType:'json',
				type:'post',
				success:function(rtn){
					$.messager.alert("提示",rtn.message,'info',function(){
						if(rtn.success){
							$('#editDlg').dialog("close");
							$('#grid').datagrid('reload');
						}
					});
				}
			});
		}
	});
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

/**
 * 盘点状态
 * @param value
 * @returns {String}
 */
function formatState(value){
	switch(value * 1){
		case 0: return '未审核';
		case 1: return '已审核';
		case 2: return '已确认';
		default: return '';
	}
}
/**
 * 盘点类型
 * @param value
 * @returns {String}
 */
function formatType(value){
	switch(value * 1){
	case 0: return '盘盈';
	case 1: return '盘亏';
	default: return '';
	}
}
