package com.zhiyin.plugins.oneclicknavigation.xml.domelements;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * Mybatis xml 文件中 ["select", "insert", "update", "delete"] 标签的 DOM 元素映射
 *
 * @author yan on 2024/2/25 00:44
 */
public interface Statement {

    /**
     * 对应 DAO 方法名
     * @return 标签的 id 属性
     */
    @Attribute("id")
    GenericAttributeValue<String> getId();

}
