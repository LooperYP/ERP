$(function(){
	$('#grid').datagrid({
		columns:[[
		    {field:'name',title:'月份',width:100},
		    {field:'y',title:'销售额',width:100}
		]],
		onLoadSuccess:function(_data){
			//在数据加载成功的时候触发。
			$.each(_data.rows,function(i,row){
				row.name += "月";
			});
			showChart(_data.rows);
		}
	});
	
	//查询
	$('#btnSearch').bind('click',function(){
		var queryParam = $('#searchForm').serializeJSON();
		
		$('#grid').datagrid('load',queryParam);
	});
	//showChart();
	//默认选中当年
	var date = new Date();
	$('#yearCob').combobox('setValue', date.getFullYear());
	$('#grid').datagrid({
		url: 'report_trendReport',
		queryParams:{year:date.getFullYear()}
	});
});

function showChart(_data){
	var yearMonth = [];
	for(var i = 1; i<= 12; i++){
		yearMonth.push(i+"月");
	}
	$('#pieChart').highcharts({
        title: {
            text: $('#yearCob').combobox('getValue') + "年度销售趋势分析",
            x: -20 //center
        },
        subtitle: {
            text: 'Source: www.itheima.com',
            x: -20
        },
        xAxis: {
            categories: yearMonth
        },
        yAxis: {
            title: {
                text: 'RMB (￥)'
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: '￥'
        },
        legend: {
            layout: 'vertical',
            align: 'center',
            verticalAlign: 'bottom',
            borderWidth: 0
        },
        credits: {
	    	href: "www.itheima.com",
	    	text: "www.itheima.com"
    	},
    	tooltips:{
    		valuePrefix:"月"
    	},
        series: [{
            name: '年度销售趋势',
            data: _data
        }]
    });
}