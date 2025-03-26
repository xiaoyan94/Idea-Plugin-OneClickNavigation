<#include "common.ftl">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>${fileName}</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">

    <link href="../3rdTools/JqueryEasyUI/themes/gray/easyui.css" rel="stylesheet" type="text/css"/>
    <link href="../3rdTools/JqueryEasyUI/themes/icon.css" rel="stylesheet" type="text/css"/>

    <script type="text/javascript" src="../3rdTools/JqueryEasyUI/jquery.min.js"></script>
    <script type="text/javascript" src="../3rdTools/JqueryEasyUI/jquery.easyui.min.js"></script>
    <#noparse>
    <script type="text/javascript" src="../3rdTools/JqueryEasyUI/locale/easyui-lang-${language}.js"></script>
    </#noparse>
    <script type="text/javascript" src="../3rdTools/JqueryEasyUI/validate.js"></script>
    <script id="i18n" src="../ViewScript/Createi18n.js" module="${appName}"></script>
    <script src="../ViewScript/FormatDate.js"></script>
    <script src="../ViewScript/FwkExtend.js"></script>
    <script src="../ViewScript/easyui.datagrid.fwk.js"></script>
    <style>
        table.view {
            border: 0px solid #A8CFEB;
            border-collapse: collapse;
            margin-bottom: 5px;
            border-bottom: none;
            border-left: none;
            border-top: none;
            border-right: none;
        }

        .view th {
            padding-left: 10px;
            padding-right: 5px;
            padding-top: 5px;
            padding-bottom: 5px;
            height: 23px;
            width: 150px;
            border: 1px solid silver;
            background-color: #FFFFFF;
        }

        .view td {
            padding-left: 10px;
            padding-right: 5px;
            padding-top: 5px;
            padding-bottom: 5px;
            height: 23px;
            width: 150px;
            border: 1px solid silver;
            background-color: #FAFCFF;
        }

        #searchSubDiv {
            overflow: hidden;
        }
    </style>
    <script>
        var _MainModel = "${ObjectName}";
        var _MainGrid = "${ObjectName}Grid";
        $(function () {
            var starttime = getToday();
            var queryData = {
                <#noparse>
                requestUri: "${request.getRequestUri()}",
                </#noparse>
                // createstarttime: starttime,
                datefrom: starttime,
            };
            // $("#querycreatestarttime").datebox("setValue", starttime);
            $("#querydatefrom").datebox("setValue", starttime);

            var dataQryUrl = "../${appName}/${ObjectName}/query${ObjectName}List";
            var columnQryUrl = "../${appName}/layout/getMultiDataGridColumns";
            var dataGridMap = {
                "${ObjectName}Grid": {
                    gridPara: {
                        "url": dataQryUrl,
                        "onSelect": onSelect,
                        "pagination": true,
                        "singleSelect": true
                    },
                    "query": true,
                    "autoAddEdit": false
                }
            };
            loadMultiDataGrid(queryData, columnQryUrl, dataGridMap);
            // bindValidateDateEvent('querymaintainstarttime', 'querycreateendtime');
            // bindValidateDateEvent('querycreatestarttime', 'querymaintainendtime');
            bindValidateDateEvent('querydatefrom', 'querydateto');

            $('#queryfactoryid').combobox({editable: true});
        })

        function onSelect(rowIndex, rowData) {

        }

        function Refresh() {
            $("#${ObjectName}Grid").datagrid("reload");
        }

        function Export() {
            var url = "../${appName}/${ObjectName}/export${ObjectName}";
            var queryData = getQuery${ObjectName}GridFieldCondition();
            var factory = '';
            if ($("#queryfactoryid").length > 0) {
                factory = $("#queryfactoryid").combobox('getValue');
                queryData.factoryid = factory;
            }
            ExportExcelDataByPara(url, <#noparse>'${request.getRequestUri()}'</#noparse>, "${ObjectName}Grid", JSON.stringify(queryData));
        }

    </script>
</head>
<body class="easyui-layout">
<div data-options="region:'center',title:'', border:false" style="padding:0px;">
    <div class="easyui-layout" data-options="fit:true,border:false">
        <#noparse><#assign gridName = "</#noparse>${ObjectName}<#noparse>Grid">
        <#include "/MesRoot/include/IncQueryWithParam.html"/>
        </#noparse>
        <div data-options="region:'center',title:'',border:false" style="padding:0px;">
            <div class="easyui-layout" data-options="fit:true">
                <#noparse><@menufunc group="ToolBar"/></#noparse>
                <div data-options="region:'center',border:false">
                    <table id="${ObjectName}Grid" data-options="region:'center',singleSelect:true,collapsible:false,border:false">
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</div>

<script type="text/javascript">
    $("#queryfactoryid").css({"width": "200px"});
</script>
</body>
</html>