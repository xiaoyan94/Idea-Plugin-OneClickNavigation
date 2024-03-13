package com.zhiyin.plugins.oneClickNavigation.xml;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.zhiyin.plugins.resources.Constants;

/**
 * @author yan on 2024/2/28 00:08
 */
public class MyBatisSchemaProvider implements StandardResourceProvider {
    @Override
    public void registerResources(ResourceRegistrar registrar) {
        registrar.addStdResource(Constants.MYBATIS_DTD_CLASSPATH, "/schemas/mybatis-3-mapper.dtd",
                getClass());
//        registrar.addStdResource(Constants.MYBATIS_DTD_DEFAULT, "/schemas/mybatis-3-mapper.dtd",
//                getClass());
    }
}
