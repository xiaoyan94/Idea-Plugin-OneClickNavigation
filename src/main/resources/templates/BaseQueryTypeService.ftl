<#include "common.ftl">
package com.zhiyin.service.${packageName};

// import com.zhiyin.aop.DataSetType;
import com.zhiyin.dao.DaoResultBuilder;
import com.zhiyin.dao.${packageName}.I${ObjectName}Dao;
import com.zhiyin.i18n.I18nUtil;
import com.zhiyin.service.BaseService;
import com.zhiyin.service.BizCommonService;
import com.zhiyin.service.dict.DictTransformBuilder;
import com.zhiyin.service.excel.DataImportParseResult;
import com.zhiyin.service.excel.EasyExcelUtils;
import com.zhiyin.service.excel.ExcelImportService;
import com.zhiyin.service.excel.mapper.Mapper;
import com.zhiyin.service.utils.CollectorsUtils;
import com.zhiyin.utils.StringHelper;
import com.zhiyin.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ${ObjectName}Service extends BaseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private I${ObjectName}Dao ${objectName}Dao;

    @Resource
    private ExcelImportService excelImportService;

    @Resource
    private BizCommonService bizCommonService;

    public Map query${ObjectName}List(Map<String, Object> params) throws Exception {
        return queryDaoDataT(I${ObjectName}Dao.class, ${objectName}Dao, "query${ObjectName}List", params);
    }

    public Map import${ObjectName}(FileInputStream fis, String clientIp, Map<String, Object> params) throws Exception {
        String userCode = StringUtils.getStringFromMap(params, "usercode");
        String factoryId = StringUtils.getStringFromMap(params, "factoryid");
        params.put("clientid", clientIp);
        Mapper importMapper = excelImportService.getImportMapper(${ObjectName}Service.class, factoryId, "${ObjectName}");
        DataImportParseResult dataResult = EasyExcelUtils.readBaseDataExcel(fis, importMapper, factoryId, clientIp);

        List<Map<String, Object>> filterList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        String msgRowNo = "Excel" + I18nUtil.getMessage(userCode, "com.zhiyin.mes.app.web.product_rowno") + ":%s ";
        String msgPryKeyRepeat = I18nUtil.getMessage(userCode, "com.zhiyin.mes.app.web.product.repeat") + ":%s ";

        List<String> errorMessages = new ArrayList<>(dataResult.getErrorMessages());

        for (Map<String, Object> row : dataResult.getRows()) {
            stringBuilder.setLength(0);

            String rowNo = row.get("rowno").toString();
            stringBuilder.append(String.format(msgRowNo, rowNo));
            boolean isEmpty = false;

            // 字段校验

            if (isEmpty) {
                filterList.add(row);
                errorMessages.add(stringBuilder.toString());
            } else {
                row.put("maintainer", params.get("maintainer"));
                row.put("maintaintime", params.get("maintaintime"));
            }
        }

        dataResult.getRows().removeAll(filterList);
        filterList = CollectorsUtils.filterList(dataResult.getRows(), "code");
        for (Map<String, Object> map : filterList) {
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(String.format(msgRowNo, map.get("rowno")));
            stringBuilder.append(String.format(msgPryKeyRepeat, map.get("code")));
            errorMessages.add(stringBuilder.toString());
        }
        dataResult.getRows().removeAll(filterList);
        excelImportService.insertTempDataByExcel(dataResult);

        // 插入业务表
        // ${objectName}Dao.import${objectName}(params);
        bizCommonService.resetSeq("${tableName}");
        Map retMap;
        if (!errorMessages.isEmpty()) {
            DaoResultBuilder resultBuilder = new DaoResultBuilder();
            resultBuilder.setError(String.join("\n", errorMessages));
            retMap = resultBuilder.retValue();
        } else {
            return wrapAffectedResult(1);
        }
        return retMap;
    }
}
