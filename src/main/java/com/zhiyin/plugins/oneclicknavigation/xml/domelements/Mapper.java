package com.zhiyin.plugins.oneclicknavigation.xml.domelements;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * MyBatis xml 文件的 DOM 元素映射
 * <br/>
 * mapper 标签
 *
 * @author yan on 2024/2/25 00:18
 */
public interface Mapper extends DomElement {

    /**
     * mapper 文件对应的 DAO 的类名
     * @return mapper 标签的 namespace 属性
     */
    @Attribute("namespace")
    GenericAttributeValue<String> getNamespace();




}
