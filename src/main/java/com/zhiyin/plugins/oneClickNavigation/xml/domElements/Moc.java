package com.zhiyin.plugins.oneClickNavigation.xml.domElements;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Moc extends DomElement {

    @Attribute("name")
    @Required
    @NotNull
    GenericAttributeValue<String> getName();

    @Attribute("table")
    @Required
    @NotNull
    GenericAttributeValue<String> getTable();

    @NotNull
    @SubTagList("FieldDef")
    List<FieldDef> getFieldDefs();

    @Nullable
    @SubTagList("Enums")
    List<Field> getEnums();

    interface FieldDef extends DomElement {
        @Nullable
        @SubTagList("Field")
        List<Field> getFields();
    }

    interface Field extends DomElement {
        @Attribute("name")
        @Nullable
        GenericAttributeValue<String> getName();

        @Attribute("type")
        @NameValue
        @Nullable
        GenericAttributeValue<String> getType();

        @Attribute("length")
        @NameValue
        @Nullable
        GenericAttributeValue<String> getLength();

        @Attribute("enum")
        GenericAttributeValue<String> getEnum();
    }

    interface Enums extends DomElement {}
}
