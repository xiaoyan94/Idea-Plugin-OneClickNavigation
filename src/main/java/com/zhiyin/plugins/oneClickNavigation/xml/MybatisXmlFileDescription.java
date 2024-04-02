package com.zhiyin.plugins.oneClickNavigation.xml;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileDescription;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Mapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * dom.fileMetaData扩展点的实现类,为了索引目标 Mybatis XML 文件
 *
 * @author yan on 2024/2/24 23:57
 */
public class MybatisXmlFileDescription extends DomFileDescription<Mapper> {

    public MybatisXmlFileDescription() {
        super(Mapper.class, "mapper");
    }


//    @Override
//    protected void initializeFileDescription() {
//        registerNamespacePolicy(Constants.MYBATIS_DTD_CLASSPATH, Constants.MYBATIS_POSSIBLE_NAMESPACES);
//    }

    /**
     * 满足条件的才会被索引
     * @param file xml文件
     * @param module 模块
     * @return true 则索引
     */
    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {

        XmlTag rootTag = file.getRootTag();
        String rootTagName = super.getRootTagName();
        return rootTag != null && rootTagName.equals(rootTag.getName());

//        return super.isMyFile(file, module);
    }
}
