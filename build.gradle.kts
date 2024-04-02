plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.17.1"
}

group = "com.zhiyin.plugins"
version = "1.0.1"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2022.1.4")
  type.set("IU") // Target IDE Platform

  plugins.set(listOf("com.intellij.java", "properties", "JavaScript"))
}

tasks {
  // Set the JVM compatibility        versions
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.encoding = "UTF-8"
  }

  patchPluginXml {
    sinceBuild.set("221")
    untilBuild.set("233.*")

    changeNotes.set(
      """
      <h4>1.0.1</h4>
      <ul>
      <li>支持新版Idea (233). </li>
      </ul>
      <h4>1.0.0</h4>
      <ul>
      <li>支持Dao接口, MyBatis mapper文件互相跳转;</li>
      <li>支持Service调用Dao方法直接跳转到mapper文件;</li>
      <li>支持queryDaoDataT, 直接调转到mapper文件; queryDaoDataT的方法参数与dao接口方法互相跳转, 自动补全;</li>
      <li>支持Java代码中折叠显示I18n中文资源串, 以及资源串未配置检测;</li>
      <li>支持JavaScript代码中折叠显示I18n中文资源串;</li>
      </ul>
      """.trimIndent()
    )
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  runIde {
//    jvmArgs("-Xmx4096m","-XX:ReservedCodeCacheSize=512m","-Xms1024m","-javaagent:/Users/yan/Downloads/jetbra/ja-netfilter.jar=jetbrains")
    jvmArgs("-Xmx4096m","-XX:ReservedCodeCacheSize=512m","-Xms1024m","-javaagent:\"F:\\Tools\\jetbra-ded4f9dc4fcb60294b21669dafa90330f2713ce4\\jetbra\\ja-netfilter.jar\"=jetbrains")
  }

}
