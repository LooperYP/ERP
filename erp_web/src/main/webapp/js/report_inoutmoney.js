$(function(){
	//初始化表格
	$('#grid').datagrid({
	    columns:[[
	        {field:'month',title:'月份',width:30},
	        {field:'inMoney',title:'销售收入',width:80},
	        {field:'inMoneyReturn',title:'采购退货收入',width:80},
	        {field:'outMoney',title:'采购支出',width:80},
	        {field:'outMoneyReturn',title:'销售退货支出',width:80}
	    ]],
	    singleSelect:true,
	    onLoadSuccess:function(data){
			var inMoney = [];//销售收入
			var inMoneyReturn = [];//采购退货收入
			var outMoney = [];//采购支出
			var outMoneyReturn = [];//销售退货支出
			$.each(data.rows,function(i,row){
				inMoney.push(row["inMoney"]);
				inMoneyReturn.push(row["inMoneyReturn"]);
				outMoney.push(row["outMoney"]);
				outMoneyReturn.push(row["outMoneyReturn"]);
	    	});
			showColumn(inMoney,inMoneyReturn,outMoney,outMoneyReturn);
	    }
	});
	//查询
	$('#btnSearch').bind('click',function(){
		var formData = $('#searchForm').serializeJSON();
		$('#grid').datagrid('load',formData);
	});
	var date = new Date();
	$('#yearCombo').combobox('setValue',date.getFullYear());
	
	$('#grid').datagrid({
	    url:'report_getInOutMoney.action',
	    queryParams:{
	    	year:$('#yearCombo').combobox('getValue')
	    }
	});
});
function showColumn(inMoney,inMoneyReturn,outMoney,outMoneyReturn) {
	var yearMonth = [];
	var inOutMoney = [];
	for (var i = 1; i <= 12; i++) {
		yearMonth.push(i + '月');
		inOutMoney.push(inMoney[i-1]+inMoneyReturn[i-1]-outMoney[i-1]-outMoneyReturn[i-1]);
	}
	 $('#trendChart').highcharts({
		 chart: {
	            type: 'column',
	            options3d: {
	                enabled: true,
	                alpha: 15,
	                beta: 15,
	                viewDistance: 25,
	                depth: 40
	            },
	            marginTop: 60,
	            marginRight: 40
	        },

	        title: {
	            text: $('#yearCombo').combobox('getValue')+'年度收入支出趋势图'
	        },
	        subtitle: {
	            text: 'Source: www.heima.com'
	        },
	        xAxis: {
	            categories: yearMonth
	        },

	        yAxis: {
	            allowDecimals: false,
	            min: 0,
	            title: {
	                text: 'RMB (￥)'
	            }
	        },
	        tooltip: {
	        	valueSuffix: ' ￥',
	            headerFormat: '<b>{point.key}</b><br>',
	            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y} / {point.stackTotal} ￥'
	        },

	        plotOptions: {
	            column: {
	                stacking: 'normal',
	                depth: 40
	            }
	        },
	        series: [{
	            name: '销售收入',
	            data: inMoney,
	            stack: 'inMoney'
	        }, {
	            name: '采购退货收入',
	            data: inMoneyReturn,
	            stack: 'inMoney'
	        }, {
	            name: '采购支出',
	            data: outMoney,
	            stack: 'outMoney'
	        }, {
	            name: '销售退货支出',
	            data: outMoneyReturn,
	            stack: 'outMoney'
	        }, {
	            name: '利润额',
	            data: inOutMoney,
	            stack: 'inOutMoney'
	        }],
	        credits: {
		    	href: "www.itheima.com",
		    	text: "www.itheima.com"
	    	}
    });
}