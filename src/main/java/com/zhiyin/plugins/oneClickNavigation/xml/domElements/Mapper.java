package com.zhiyin.plugins.oneClickNavigation.xml.domElements;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MyBatis xml 文件的 DOM 元素映射
 * <br/>
 * mapper 标签
 *
 */
//@NameStrategy(JavaNameStrategy.class)
//@Namespace(Constants.MYBATIS_DTD_CLASSPATH)
public interface Mapper extends DomElement {

    /**
     * mapper 文件对应的 DAO 的类名
     * @return mapper 标签的 namespace 属性
     */
    @Attribute("namespace")
    @NameValue
    @Required
    @Nullable
    GenericAttributeValue<String> getNamespace();
    @NotNull
    @SubTagsList({"select", "insert", "update", "delete"})
    List<Statement> getStatements();
    @NotNull
    @SubTagList("select")
    List<Select> getSelects();
    @NotNull
    @SubTagList("insert")
    List<Insert> getInserts();
    @NotNull
    @SubTagList("update")
    List<Update> getUpdates();
    @NotNull
    @SubTagList("delete")
    List<Delete> getDeletes();


}
