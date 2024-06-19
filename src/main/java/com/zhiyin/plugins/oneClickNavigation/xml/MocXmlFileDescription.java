package com.zhiyin.plugins.oneClickNavigation.xml;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileDescription;
import com.zhiyin.plugins.oneClickNavigation.xml.domElements.Moc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MocXmlFileDescription extends DomFileDescription<Moc> {
    public MocXmlFileDescription() {
        super(Moc.class, "Moc");
    }

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
    }
}
