<#include "common.ftl">
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper     PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"     "classpath://mybatis-3-mapper.dtd">

<mapper namespace="com.zhiyin.dao.${packageName}.I${ObjectName}Dao">

    <select id="query${ObjectName}List" parameterType="Map" resultType="Map">
${dataGrids[0].sql}
    </select>

</mapper>