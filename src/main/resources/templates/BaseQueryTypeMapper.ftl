<#include "common.ftl">
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper     PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"     "classpath://mybatis-3-mapper.dtd">

<mapper namespace="com.zhiyin.dao.${packageName}.I${ObjectName}Dao">

    <select id="query${ObjectName}List" parameterType="Map" resultType="Map">
${dataGrids[0].sql}
        <where>
        <#list dataGrids[0].queryFields as field>
        <#if field.name?lowerCase?endsWith("id") || field.name?lowerCase?endsWith("status") || field.name?lowerCase?endsWith("state")>
            <if test="${field.name?lowerCase} != null and ${field.name?lowerCase} != ''">
                and a.${field.name} = <#noParse>#{</#noParse>${field.name?lowerCase}}
            </if>
        <#elseIf field.name?lowerCase?endsWith("time") || field.name?lowerCase?endsWith("date")>
            <if test="${field.name?lowerCase}from != null and ${field.name?lowerCase}from != ''">
                and a.${field.name} >= <#noParse>concat(#{</#noParse>${field.name?lowerCase}from}, ' 00:00:00')
            </if>
            <if test="${field.name?lowerCase}to != null and ${field.name?lowerCase}to != ''">
                and a.${field.name} <![CDATA[ <= ]]> <#noParse>concat(#{</#noParse>${field.name?lowerCase}to}, ' 23:59:59')
            </if>
        <#else>
            <if test="${field.name?lowerCase} != null and ${field.name?lowerCase} != ''">
                and a.${field.name} like concat('%',<#noParse>#{</#noParse>${field.name?lowerCase}}, '%')
            </if>
        </#if>
        </#list>
        </where>
    </select>

    <update id="import${ObjectName}" parameterType="Map">
    </update>

    <update id="delete${ObjectName}" parameterType="Map">
        delete a
        from ${dataGrids[0].tableName} a
        <#noParse>
        where a.factoryid = #{factoryid}
        <choose>
            <when test="id != null and id != ''">
                and a.id = #{id}
            </when>
            <when test="idlist != null and idlist.size() > 0">
                and a.id in
                <foreach collection="idlist" item="item" open="(" close=")" separator=",">
                    #{item}
                </foreach>
            </when>
            <otherwise>
                and 1 = 0  <!-- 避免删除全表的保护条件 -->
            </otherwise>
        </choose>
        </#noParse>
    </update>
</mapper>