<#assign superAdminFields = ["id", "factoryid", "factoryname"] />
<#assign shouldHideColumns = ["id", "factoryid", "state", "status", "delflag"] />
<#assign shouldHideColumnsSuffix = "id" />
<#include "common.ftl">
<#--TODO 生成MOC-->
<?xml version="1.0" encoding="UTF-8" ?>
<Moc name="${ObjectName}" table="${tableName}">
    <FieldDef>
        <#list columns as column>
            <Field name="${column.name}" type="${column.field.mocTypeName}" length="${column.field.nameLength?c}"/>
        </#list>
    </FieldDef>
</Moc>