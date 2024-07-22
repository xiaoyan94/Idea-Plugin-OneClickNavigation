<#assign superAdminFields = ["id", "factoryid", "factoryname"] />
<#assign shouldHideColumns = ["id", "factoryid", "state", "status", "delflag"] />
<#assign shouldHideColumnsSuffix = "id" />
<#assign appName = moduleName />
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
<#list grid.queryFields as field>
<#if field.name?lowerCase == "factoryid">
            <Field id="${field.name}" name="factoryid" ref="${field.name}" easyuiClass="easyui-combobox" required="true" condition="{usertype} == '1'">
                <Combobox>
                    <ComboxUrl value="../Basic/Factory/getFactoryViewList"/>
                    <ComboxValueField value="id"/>
                    <ComboxTextField value="name"/>
                </Combobox>
            </Field>
<#elseIf field.name?lowerCase == "id">
<#elseIf field.isQueryField?? && field.isQueryField == "true">
            <Field id="${field.name}" name="${field.name?lowerCase}"<#if field.ref??> ref="${field.ref}"</#if><#if field.label??> label="${field.label}" easyuiClass="${field.easyuiClass!"easyui-textbox"}"</#if>/>
<#else>
</#if>
</#list>

<#noParse>
            <Statement isLogger="false">
                <![CDATA[
                    select ${select_para_list}, b.name as factoryname
                    from ${table} a
                    left join biz_base_factory b on a.factoryid = b.id
                    where 1 = 1
                    ${condition_para_list}
                    order by a.id desc
                ]]>
            </Statement>
</#noParse>
        </Query>

        <Dialog>
<#list grid.dialogFields as field>
<#if field.name?lowerCase == "id">
            <Field id="Id" name="id" ref="Id" isEditHidden="true" isAddHidden="true"/>
<#elseIf field.name?lowerCase == "factoryid">
            <Field id="FactoryId" name="FactoryId" ref="FactoryId" condition="{usertype} == '1'" layout="99:2" required="true" editable="false" easyuiClass="easyui-combobox" >
                <Combobox>
                    <ComboxUrl value="../Basic/Factory/getFactoryViewList"/>
                    <ComboxValueField value="id"/>
                    <ComboxTextField value="name"/>
                </Combobox>
            </Field>
<#elseIf field.isDialogField?? && field.isDialogField>
            <Field id="${field.name}" name="${field.name}"<#if field.ref??> ref="${field.ref}"</#if> layout="${field.layout!"100:1"}" required="${field.required!'false'}" editable="${field.editable!"true"}" easyuiClass="${field.easyuiClass!"easyui-textbox"}"<#if field.type == "string" && (!field.easyuiClass?? || field.easyuiClass == "easyui-textbox") && field.length??> maxLength="${field.length}"</#if>/>
<#else>
</#if>
            </#list>
        </Dialog>

        <RequestOperation>
            <RequestUrl id="save" url="../${appName}/${grid.dataGridName}/save"/>
            <RequestUrl id="update" url="../${appName}/${grid.dataGridName}/update"/>
            <RequestUrl id="del" url="../${appName}/${grid.dataGridName}/delete"/>
            <RequestUrl id="findById" url="../${appName}/${grid.dataGridName}/findById"/>
        </RequestOperation>
    </DataGrid>
    
</#list>
</ViewDefine>
