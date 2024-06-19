package com.zhiyin.plugins.oneClickNavigation.xml.domElements;

import com.intellij.util.xml.*;

/**
 * Mybatis xml 文件中 ["select", "insert", "update", "delete"] 标签的 DOM 元素映射
 *
 * 
 */
public interface Statement extends DomElement {

    /**
     * 对应 DAO 方法名
     * @return 标签的 id 属性
     */
    @Attribute("id")
    @NameValue
    @Required
    GenericAttributeValue<String> getId();

}
