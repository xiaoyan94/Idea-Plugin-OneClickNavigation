plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.17.1"
}

group = "com.zhiyin.plugins"
version = "1.0-SNAPSHOT"

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
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "8"
    targetCompatibility = "8"
  }

  patchPluginXml {
    sinceBuild.set("221")
    untilBuild.set("231.*")
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
    jvmArgs("-Xmx4096m","-XX:ReservedCodeCacheSize=512m","-Xms1024m","-javaagent:/Users/yan/Downloads/jetbra/ja-netfilter.jar=jetbrains")
  }

}
