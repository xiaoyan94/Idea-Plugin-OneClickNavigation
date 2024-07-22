<?xml version="1.0" encoding="UTF-8"?>
<Moc name="${mocName}" table="${tableName}">
    <FieldDef>
        <#list fields as field>
        <Field name="${field.name}" type="${field.type}"<#if field.type == "string"> length="${field.length}"</#if>/>
        </#list>
    </FieldDef>
</Moc>
