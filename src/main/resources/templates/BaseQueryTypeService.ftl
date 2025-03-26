<#include "common.ftl">
package com.zhiyin.service.${packageName};

import com.zhiyin.aop.DataSetType;
import com.zhiyin.dao.${packageName}.I${ObjectName}Dao;
import com.zhiyin.service.BaseService;
import com.zhiyin.service.dict.DictTransformBuilder;
import com.zhiyin.utils.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ${ObjectName}Service extends BaseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private I${ObjectName}Dao ${objectName}Dao;

    public Map query${ObjectName}List(Map<String, Object> params) throws Exception {
        return queryDaoDataT(I${ObjectName}Dao.class, ${objectName}Dao, "query${ObjectName}List", params);
    }
}
