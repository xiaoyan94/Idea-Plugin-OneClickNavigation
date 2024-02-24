package com.zhiyin.plugins.oneclicknavigation.xml;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileDescription;
import com.zhiyin.plugins.oneclicknavigation.xml.domelements.Mapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * dom.fileMetaData扩展点的实现类,为了索引目标 Mybatis XML 文件
 *
 * @author yan on 2024/2/24 23:57
 */
public class MybatisXmlFileDescription extends DomFileDescription<Mapper> {

    public MybatisXmlFileDescription() {
        this(Mapper.class, "mapper");
    }

    public MybatisXmlFileDescription(Class<Mapper> rootElementClass, @NonNls String rootTagName, @NonNls String @NotNull ... allPossibleRootTagNamespaces) {
        super(rootElementClass, rootTagName, allPossibleRootTagNamespaces);
    }

    /**
     * 满足条件的才会被索引
     * @param file xml文件
     * @param module 模块
     * @return true 则索引
     */
    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {

        boolean isMyFile = super.isMyFile(file, module);

        String fileName = file.getName();

        XmlTag rootTag = file.getRootTag();
        String rootTagName = super.getRootTagName();
        return null != rootTag && rootTag.getName()
                                         .equals(rootTagName);

//        return super.isMyFile(file, module);
    }
}
