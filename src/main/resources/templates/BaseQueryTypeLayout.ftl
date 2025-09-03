<#assign superAdminFields = ["id", "factoryid", "factoryname"] />
<#assign shouldHideColumns = ["id", "factoryid", "state", "status", "delflag"] />
<#assign shouldHideColumnsSuffix = "id" />
<#include "common.ftl">
<?xml version="1.0" encoding="UTF-8"?>
<ViewDefine>
    <#list dataGrids as grid>
    <DataGrid id="${grid.dataGridName}Grid">
        <Columns>
            <#if grid.ckDummyColumn?? && grid.ckDummyColumn == "true">
            <Column name="dummy" type="int">
                <Field value="ck"/>
                <Title value=" " chs=" " eng=" "/>
                <Align value="left"/>
                <CheckBox value="true"/>
            </Column>
            </#if>

            <#list grid.columns as column>
            <Column name="${column.name}" type="${column.type}"<#if superAdminFields?seqContains(column.name)> condition="{usertype} == '1'"</#if>>
                <Field value="${column.name}"/>
                <Title value="${column.i18nKey!column.chs!column.name}" chs="${column.chs!column.name}" eng="${column.eng!column.name}"/>
                <Align value="${column.align!'left'}"/>
                <Width value="${column.width!"150"}"/>
                <#if !column.hidden??>
                <Hidden value="${(shouldHideColumns?seqContains(column.name) || column.name?endsWith('id') || column.name?endsWith('status') || column.name?endsWith('state'))?string('true','false')}"/>
                <#else>
                <Hidden value="${column.hidden}"/>
                </#if>
            </Column>
            <#if column.name?lowerCase?endsWith('state') || column.name?lowerCase?endsWith('status')>
            <Column name="${column.name}dsp" type="${column.type}"<#if superAdminFields?seqContains(column.name)> condition="{usertype} == '1'"</#if>>
                <Field value="${column.name}dsp"/>
                <Title value="${column.i18nKey!column.name}" chs="${column.chs!column.name}" eng="${column.eng!column.name}"/>
                <Align value="${column.align!'left'}"/>
                <Width value="${column.width!"150"}"/>
                <Hidden value="false"/>
            </Column>
            </#if>
        </#list>
        </Columns>

        <Query>
            <Field id="FactoryId" ref="FactoryId" condition="{usertype} == '1'" easyuiClass="easyui-combobox">
                <Combobox>
                    <ComboxUrl value="../Basic/Factory/getFactoryViewList"/>
                    <ComboxValueField value="id"/>
                    <ComboxTextField value="name"/>
                </Combobox>
            </Field>
            <#list grid.queryFields as field>
            <#if field.name?lowerCase == "factoryid">
            <#elseIf field.name?lowerCase == "id">
            <#elseIf field.name?lowerCase == "orderid">
            <#elseIf field.name?lowerCase == "orderflowid">
            <#elseIf field.name?lowerCase == "orderchildid">
            <#elseIf field.name?lowerCase == "productid">
            <#elseIf field.name?lowerCase == "materialid">
            <#elseIf field.name?lowerCase == "moldid">
            <#elseIf field.isQueryField?? && field.isQueryField == "true" && field.name?lowerCase?endsWith("id")>
            <Field id="${field.name}" ref="${field.name}" easyuiClass="easyui-combobox">
                <Combobox>
                    <ComboxUrl value="../Basic/Factory/getFactoryViewList"/>
                    <ComboxValueField value="id"/>
                    <ComboxTextField value="name"/>
                    <ComboxMultipleSel value="false"/>
                </Combobox>
            </Field>
            <#elseIf field.isQueryField?? && field.isQueryField == "true" && field.easyuiClass?? && field.easyuiClass == "easyui-combobox">
            <Field id="${field.name}" ref="${field.name}" easyuiClass="easyui-combobox">
                <Combobox>
                    <ComboxUrl value="../Basic/Factory/getFactoryViewList"/>
                    <ComboxValueField value="id"/>
                    <ComboxTextField value="name"/>
                    <ComboxMultipleSel value="false"/>
                </Combobox>
            </Field>
            <#elseIf field.isQueryField?? && field.isQueryField == "true" && field.easyuiClass?? && field.easyuiClass?contains("date")>
            <Field id="${field.name?lowerCase}from" name="${field.name?lowerCase}from" label="${field.name?lowerCase}从" easyuiClass="${field.easyuiClass!"easyui-datebox"}"/>
            <Field id="${field.name?lowerCase}to" name="${field.name?lowerCase}to" label="到" easyuiClass="${field.easyuiClass!"easyui-datebox"}"/>
            <#elseIf field.isQueryField?? && field.isQueryField == "true" && field.easyuiClass?? && field.easyuiClass?contains("time")>
            <Field id="${field.name?lowerCase}from" name="${field.name?lowerCase}from" label="${field.name?lowerCase}从" easyuiClass="${field.easyuiClass!"easyui-datebox"}"/>
            <Field id="${field.name?lowerCase}to" name="${field.name?lowerCase}to" label="到" easyuiClass="${field.easyuiClass!"easyui-datebox"}"/>
            <#elseIf field.isQueryField?? && field.isQueryField == "true">
            <Field id="${field.name}" name="${field.name?lowerCase}"<#if field.ref??> ref="${field.ref}"</#if><#if field.label??> label="${field.label}" easyuiClass="${field.easyuiClass!"easyui-textbox"}"</#if>/>
            <#else>
            </#if>
        </#list>
        </Query>
    </DataGrid>
    </#list>
</ViewDefine>
