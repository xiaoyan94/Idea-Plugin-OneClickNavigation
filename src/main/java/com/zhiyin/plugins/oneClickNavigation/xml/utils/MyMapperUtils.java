package com.zhiyin.plugins.oneClickNavigation.xml.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.GenericAttributeValue;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Statement;
import com.zhiyin.plugins.service.MyProjectService;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MyMapperUtils {

    public static List<Statement> getStatementsByNamespace(Project project, String namespace) {
        List<Statement> statements = new ArrayList<>();
        Map<String, List<Mapper>> xmlFileMap = project.getService(MyProjectService.class).getXmlFileMap();
        List<Mapper> mappers = xmlFileMap.get(namespace);
        if (!CollectionUtils.isEmpty(mappers)) {
            for (Mapper mapper : mappers) {
                statements.addAll(mapper.getStatements());
            }
        }
        return statements;
    }

    public static List<XmlAttributeValue> getMapperElementsByNamespace(Project project, String namespace) {
        List<XmlAttributeValue> statements = new ArrayList<>();
        Map<String, List<Mapper>> xmlFileMap = project.getService(MyProjectService.class).getXmlFileMap();
        List<Mapper> mappers = xmlFileMap.get(namespace);
        if (!CollectionUtils.isEmpty(mappers)) {
            for (Mapper mapper : mappers) {
                statements.add(mapper.getNamespace().getXmlAttributeValue());
            }
        }
        return statements;
    }

}
