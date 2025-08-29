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
        <#noparse>
        var _factoryId = '${factoryId}';
        </#noparse>
        $(function () {
            var today = getToday();
            var beforeDate = getBeforeDate(7);
            var queryData = {
                <#noparse>
                requestUri: "${request.getRequestUri()}",
                // maintaintimefrom: beforeDate,
                // maintaintimeto: today,
                </#noparse>
            };
            // $("#querymaintaintimefrom").datebox("setValue", beforeDate);
            // $("#querymaintaintimeto").datebox("setValue", today);

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
            // bindValidateDateEvent('querymaintaintimefrom', 'querymaintaintimeto');

            bindImportSaveEvent();
            bindImportCloseEvent();

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

        function DownloadTemplate(){
            downloadTemplateFile(_MainModel);
        }

        function Import() {
            $("#ffImport").form('validate');
            $("#DivImport").dialog('open').dialog('setTitle', zhiyin.i18n.translate("com.zhiyin.mes.app.web.dataImport"));
            $('#DivImport').window('center');
            // $("#factoryId").val(_factoryId);
        }

        var lock_ = false;
        function bindImportSaveEvent() {
            $("#btnImportSave").click(function () {
                if (lock_) {
                    return;
                }
                bindConfirmMsgEvent();
                var validate = $("#ffImport").form('validate');
                if (validate == false) {
                    return false;
                }
                $.messager.progress({
                    title: zhiyin.i18n.translate('com.zhiyin.mes.app.order.please_wait'),
                });

                lock_ = true;

                var postData = $("#ffImport").serializeArray();
                $('#ffImport').form({
                    url: "../${appName}/${ObjectName}/import${ObjectName}",
                    success: function (data) {
                        $.messager.progress('close');
                        data = JSON.parse(data);
                        $("#textError").textbox("setValue", "");
                        if (data.successful != "1") {
                            if (data.error != "") {
                                $("#textError").textbox("setValue", data.error + '\n' + data.hint);
                            } else if (data.hint != "") {
                                $("#textError").textbox("setValue", data.hint);
                                $('#btnConfirmMsg').bind('click', impExcle);
                            }
                            $("#DivError").dialog("open").dialog('setTitle', zhiyin.i18n.translate("com.zhiyin.mes.app.web.warn"));
                            $('#DivError').window('center');

                            $("#DivImport").dialog("close");
                            $("#ffImport").form("clear");
                        } else {
                            $.messager.alert(zhiyin.i18n.translate("com.zhiyin.mes.app.web.warn"), zhiyin.i18n.translate("com.zhiyin.mes.app.web.operation_success"));
                            $("#DivImport").dialog("close");
                            $("#ffImport").form("clear");
                            Refresh();
                        }

                        $("#batchConfirmInfoGrid").datagrid("clearSelections");
                        $("#batchConfirmInfoGrid").datagrid("reload");
                    },
                    error: function (date) {
                        $.messager.progress('close');
                        $.messager.alert(zhiyin.i18n.translate("com.zhiyin.mes.app.web.operation_failure"), data);
                    }
                });
                $('#ffImport').submit();
                lock_ = false;
            });
        }

        function bindImportCloseEvent() {
            $("#btnImportClose").off('click').click(function () {
                $('#DivImport').dialog('close');
                $("#ffImport").form("clear")
            });
        }

        function bindConfirmMsgEvent() {
            $("#btnConfirmMsg").off('click').click(function () {
                $('#DivError').dialog('close');
                $("#textError").textbox("setValue", "");
                Refresh();
            });
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

<#noparse>
<div id="DivImport" class="easyui-dialog" style="width:550px;height:auto;"
     closed="true" resizable="true" modal="true" data-options="iconCls: 'icon-add',buttons: '#dlg-buttons'">
    <form id="ffImport" method="post" enctype="multipart/form-data" novalidate="novalidate">
        <input id="factoryId" name="factoryId" hidden="true" disabled>
        <table width="100%" height="100%" id="tblAdd" class="view">
            <tr>
                <th style="width:85px;overflow:hidden;noWrap:true">
                    <label style='float:right;width:85px;text-align:right' for="file"><@message
                        key="com.zhiyin.mes.app.web.product_product_upload"/>：</label>
                </th>
                <td colspan="5">
                    <textarea style="width:99%" id="file" name="file"
                              buttonText="<@message key='com.zhiyin.mes.app.web.product_product_upload_alert'/>"
                              class="easyui-filebox"
                              data-options="required:true,prompt:'<@message key='com.zhiyin.mes.app.web.product_product_upload_back'/>'"></textarea>
                </td>
            </tr>
            <tr>
                <td colspan="6"
                    style="text-align:right;padding-top:10px;border-bottom-style:none;border-right-style:none;border-left-style:none;">
                    <a href="javascript:void(0)" class="easyui-linkbutton" id="btnImportSave" iconcls="icon-ok"><@message
                        key="com.zhiyin.mes.app.web.product_confirm_btn"/></a>
                    <a href="javascript:void(0)" class="easyui-linkbutton" id="btnImportClose" iconcls="icon-cancel"><@message
                        key="com.zhiyin.mes.app.web.product_close_btn"/></a>
                </td>
            </tr>
        </table>
    </form>
</div>

<div id="DivError" class="easyui-dialog" style="width:700px;height:250px;"
     closed="true" resizable="true" modal="true">
    <div style="display: flex; flex-direction: column; height: 100%;">
        <div style="flex: 1; padding: 10px; overflow: hidden;">
            <input id="textError" class="easyui-textbox"
                   data-options="multiline:true,readonly:true"
                   style="width:100%;height:100%;box-sizing:border-box;">
        </div>
        <div style="text-align: right; padding: 10px; border-top: 1px solid #ddd;">
            <a href="javascript:void(0)" class="easyui-linkbutton" id="btnConfirmMsg" iconcls="icon-ok"><@message key="com.zhiyin.mes.app.web.product_close_btn"/></a>
        </div>
    </div>
</div>
</#noparse>

<script type="text/javascript">
    $("#queryfactoryid").css({"width": "200px"});
</script>
</body>
</html>