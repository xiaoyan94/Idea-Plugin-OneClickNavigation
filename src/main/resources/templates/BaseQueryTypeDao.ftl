<#include "common.ftl">
package com.zhiyin.dao.${packageName};

import java.util.List;
import java.util.Map;

public interface I${ObjectName}Dao {

    List<Map> query${ObjectName}List(Map para);

    int import${ObjectName}(Map<String, Object> params);

    int delete${ObjectName}(Map<String, Object> params);
}