$(function(){
	//初始化表格
	$('#grid').datagrid({
	    columns:[[
	        {field:'name',title:'月份',width:100},
	        {field:'-',title:'销售退货总额',width:100,precision:2,formatter: function(value,row,index){
				return parseFloat(row["水果"] + row["饼干"] + row["调味品"]).toFixed(2);
			}}
	    ]],
	    singleSelect:true,
	    onLoadSuccess:function(data){
			var shui = [];//水果销售退货额
			var bing = [];//饼干销售退货额
			var tiao = [];//调味品销售退货额
			var money = [];//销售退货总额
			$.each(data.rows,function(i,row){
				shui.push(row["水果"]);
				bing.push(row["饼干"]);
				tiao.push(row["调味品"]);
				money.push(row["水果"] + row["饼干"] + row["调味品"]);
	    	});
			showColumn(shui,bing,tiao,money);
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
	    url:'report_returnTrendReport.action',
	    queryParams:{
	    	year:$('#yearCombo').combobox('getValue')
	    }
	});
});
function showColumn(shui,bing,tiao,money) {
	var yearMonth = [];
	for (var i = 1; i <= 12; i++) {
		yearMonth.push(i + '月');
	}
	 $('#trendChart').highcharts({
        chart: {
            type: 'column'
        },
        title: {//标题
            text: $('#yearCombo').combobox('getValue')+'年度销售退货趋势图'
        },
        subtitle: {
            text: 'Source: www.heima.com'
        },
        xAxis: {
            categories: yearMonth,
            crosshair: true
        },
        yAxis: {
            min: 0,
            title: {
                text: 'RMB (￥)'
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                '<td style="padding:0"><b>{point.y:.1f} ￥</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [{//数据
            name: '水果',
            data: shui

        }, {
            name: '饼干',
            data: bing

        }, {
            name: '调味品',
            data: tiao

        }, {
            name: '退货总额',
            data: money

        }],
        credits: {
	    	href: "www.itheima.com",
	    	text: "www.itheima.com"
    	}
    });
}
