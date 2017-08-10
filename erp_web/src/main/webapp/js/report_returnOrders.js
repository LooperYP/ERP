var yvalue=[];
$(function(){
	$('#grid').datagrid({
		url: 'report_returnOrdersReport',
        singleSelect:true,
		columns:[[
		    {field:'name',title:'商品类型',width:100},
		    {field:'y',title:'销售额',width:100}
		]],
		onLoadSuccess:function(_data){
			//在数据加载成功的时候触发。
			var rows = _data.rows;
			for(var i = 0; i < rows.length; i++){
				rows[i].colorIndex = i;
				yvalue.push(rows[i].name);
			}
			showChart(_data.rows);
		}
	});
	
	//查询
	$('#btnSearch').bind('click',function(){
		var queryParam = $('#searchForm').serializeJSON();
		if(queryParam.endDate != ''){
			queryParam.endDate += " 23:59:59";
		}
		$('#grid').datagrid('load',queryParam);
	});
	//showChart();
});

function showChart(_data){
	var colors = ['#058DC7', '#50B432'];
	
	$('#pieChart').highcharts({
        chart: {
            type: 'bar'
        },
        title: {
            text: '销售退货统计表'
        },
        subtitle: {
            text: 'Source: <a href="https://www.itheima.com">www.itheima.com</a>'
        },
        xAxis: {
            categories: yvalue,
            title: {
                text: null
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: '总金额 (￥)',
                align: 'high'
            },
            labels: {
                overflow: 'justify'
            }
        },
        tooltip: {
            valueSuffix: ' 元'
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -40,
            y: 80,
            floating: true,
            borderWidth: 1,
            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
            shadow: true
        },
        credits: {
            enabled: false
        },
        series: [{
            // type: 'pie',
            name: '总金额',
            data: _data
        }]
        /*chart: {
            type: 'pie',
            options3d: {
                enabled: true,
                alpha: 45,
                beta: 0
            }
        },
        title: {
            text: '销售退货统计表'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                depth: 35,
                dataLabels: {
                    enabled: true,
                    format: '{point.name}'
                },
                showInLegend: true
            }
        },
        colors:colors,
        series: [{
            type: 'pie',
            name: '比例',
            data: _data
        }],
        credits: {
	    	href: "www.itheima.com",
	    	text: "www.itheima.com"
    	}*/
    });
}