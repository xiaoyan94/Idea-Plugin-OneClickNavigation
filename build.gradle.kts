import dev.bmac.gradle.intellij.PluginUploader
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogSectionUrlBuilder
import org.jetbrains.changelog.markdownToHTML


fun properties(key: String) = project.findProperty(key).toString()

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.1.0")

        classpath("software.amazon.awssdk:s3:2.20.64")
        classpath("software.amazon.awssdk:auth:2.20.64")
        classpath("software.amazon.awssdk:aws-core:2.20.64")
//        classpath(platform("software.amazon.awssdk:bom:2.21.1"))
    }
}

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"

    id("org.jetbrains.changelog") version "2.2.0" // Gradle Changelog Plugin

    id("dev.bmac.intellij.plugin-uploader") version "1.3.5"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // 添加OkHttp依赖项
    implementation("com.alibaba:fastjson:2.0.28")

}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType")) // Target IDE Platform

    plugins.set(listOf("com.intellij.java", "properties", "JavaScript", "com.intellij.jsp"))

    updateSinceUntilBuild.set(false)
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl.set(properties("pluginRepositoryUrl"))
    sectionUrlBuilder.set(ChangelogSectionUrlBuilder { repositoryUrl, currentVersion, previousVersion, isUnreleased ->
        "$repositoryUrl${
            properties(
                "pluginName"
            )
        }-$currentVersion.zip"
    })
}

tasks {
    // Set the JVM compatibility        versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
//        untilBuild.set(properties("pluginUntilBuild"))

        changeNotes.set(
            """
      <h4>1.0.2</h4>
      <ul>
      <li>支持XML(Layout/ImpMapper)中折叠显示I18n中文资源串.</li>
      </ul>
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

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        val pluginDescriptionProvider = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
        pluginDescription.set(pluginDescriptionProvider)

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes.set(with(changelog) {
            renderItem(
                (getOrNull(properties("pluginVersion")) ?: getUnreleased())
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        })
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
        jvmArgs(
            "-Dfile.encoding=UTF-8",
            "-Xmx4096m",
            "-XX:ReservedCodeCacheSize=512m",
            "-Xms1024m",
            "-javaagent:\"F:\\Tools\\jetbra-ded4f9dc4fcb60294b21669dafa90330f2713ce4\\jetbra\\ja-netfilter.jar\"=jetbrains"
        )
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    prepareSandbox {
        if (!properties("skipProguard").toBoolean()) {
            dependsOn("proguard")
            doFirst {
                val original = File("build/libs/${rootProject.name}.jar")
                println(original.absolutePath)
                val obfuscated = File("build/${rootProject.name}-${version}-obfuscated.jar")
                println(obfuscated.absolutePath)
                if (original.exists() && obfuscated.exists()) {
                    original.delete()
                    obfuscated.renameTo(original)
                    println("plugin file obfuscated")
                } else {
                    println("error: some file does not exist, plugin file not obfuscated")
                }
            }
        }

    }

    generateBlockMap {
        // Depend on either signPlugin or buildPlugin, depending on which task provides the file in the uploadPlugin
        dependsOn(project.tasks.named("buildPlugin"))
    }

    // IDEA官方文档提供的第三方上传插件 https://plugins.jetbrains.com/docs/intellij/custom-plugin-repository.html
    // 会生成子文件夹；同版本号会报错不会覆盖。
    uploadPlugin {
        dependsOn("buildPlugin", "updateLocalPluginXml")

        val archiveFile = project.tasks.buildPlugin.get().archiveFile
        file.set(archiveFile.get().asFile)
        pluginName.set(properties("pluginName"))
        pluginId.set("${properties("pluginGroup")}.${properties("pluginName")}")
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
//        pluginDescription.set(file("README.md").readText())
//        changeNotes.set(file("CHANGELOG.md").readText())
        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        val pluginDescriptionProvider = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
        pluginDescription.set(pluginDescriptionProvider)
        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes.set(with(changelog) {
            renderItem(
                (getOrNull(properties("pluginVersion")) ?: getUnreleased())
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        })

        repoType.set(PluginUploader.RepoType.S3)
        url.set(properties("r2.non-aws.s3.endpoint"))
        authentication.set("${properties("r2.s3.accessKeyId")}:${properties("r2.s3.secretAccessKey")}")

        println("uploadPlugin: ${version.get()} successfully uploaded")
    }
}

// 仅在本地生成xml
task<dev.bmac.gradle.intellij.UpdateXmlTask>("updateLocalPluginXml"){
    dependsOn("patchChangelog")
    updateFile.set(file(properties("updatePluginXmlFileName")))
    pluginName.set(properties("pluginName"))
    version.set(properties("pluginVersion"))
    println("updateLocalPluginXml: ${version.get()}")
    downloadUrl.set("${properties("pluginRepositoryUrl")}${pluginName.get()}-${version.get()}.zip")
    pluginId.set("${properties("pluginGroup")}.${pluginName.get()}")
    sinceBuild.set(properties("pluginSinceBuild"))
    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    val pluginDescriptionProvider = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
        val start = "<!-- Plugin description -->"
        val end = "<!-- Plugin description end -->"

        with(it.lines()) {
            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
        }
    }
    pluginDescription.set(pluginDescriptionProvider)

    val changelog = project.changelog // local variable for configuration cache compatibility
    // Get the latest available change notes from the changelog file
    changeNotes.set(with(changelog) {
        renderItem(
            (getOrNull(properties("pluginVersion")) ?: getUnreleased())
                .withHeader(false)
                .withEmptySections(false),
            Changelog.OutputType.HTML,
        )
    })
}

// 上传到 R2 根目录：同版本号直接覆盖。
tasks.register("uploadPluginToR2ByAmazonS3") {
    dependsOn("buildPlugin", "updateLocalPluginXml")

    doLast {
//    val region = Region.CN_Beijing
        val region = Region.of(properties("r2.s3.region"))
        val bucketName = properties("r2.bucketName")
        val key = "${rootProject.name}-${version}.zip"
        val file = File("${project.rootDir}/build/distributions/${rootProject.name}-${version}.zip")

        println("Uploading file to S3: ${file.absolutePath}")

        // Create AWS credentials
        val accessKey = properties("r2.s3.accessKeyId")
        val secretKey = properties("r2.s3.secretAccessKey")
        val awsCredentials = AwsBasicCredentials.create(accessKey, secretKey)

        val s3Client = S3Client.builder().region(region)
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .endpointOverride(URI.create(properties("r2.s3.endpoint")))
            .build()

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
        println("File uploaded successfully to bucket: $bucketName, key: $key")

        val updateXmlFile = File("${project.rootDir}/${properties("updatePluginXmlFileName")}")
        val putUpdateXmlFileRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(updateXmlFile.name)
            .build()
        s3Client.putObject(putUpdateXmlFileRequest, RequestBody.fromFile(updateXmlFile))
        println("File uploaded successfully to bucket: $bucketName, key: ${updateXmlFile.name}")

        println("File uploaded successfully to S3.")
    }
}

if (hasProperty("buildScan")) {
    extensions.findByName("buildScan")?.withGroovyBuilder {
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    verbose()
//    keepdirectories()// By default, directory entries are removed.
    ignorewarnings()
    target("11")

    // Alternatively put your config in a separate file
    configuration("config.pro")

    // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
    injars(tasks.named("jar"))

    outjars("build/${rootProject.name}-${version}-obfuscated.jar")

    val javaHome = System.getProperty("java.home")
    // Automatically handle the Java version of this build, don't support JBR
    // As of Java 9, the runtime classes are packaged in modular jmod files.
//        libraryjars(
//            // filters must be specified first, as a map
//            mapOf("jarfilter" to "!**.jar",
//                  "filter"    to "!module-info.class"),
//            "$javaHome/jmods/java.base.jmod"
//        )

    // Add all JDK deps
    if (!properties("skipProguard").toBoolean()) {
        File("$javaHome/jmods/").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    }

//    libraryjars(configurations.runtimeClasspath.get().files)
    val ideaPath = properties("localIdeaPath")

    // Add all java plugins to classpath
//    File("$ideaPath/plugins/java/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    // Add all IDEA libs to classpath
//    File("$ideaPath/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }

    libraryjars(configurations.compileClasspath.get())

    dontshrink()
    dontoptimize()

//    allowaccessmodification() //you probably shouldn't use this option when processing code that is to be used as a library, since classes and class members that weren't designed to be public in the API may become public

    adaptclassstrings("**.xml")
    adaptresourcefilecontents("**.xml")// or   adaptresourcefilecontents()

    // Allow methods with the same signature, except for the return type,
    // to get the same obfuscation name.
    overloadaggressively()
    // Put all obfuscated classes into the nameless root package.
//    repackageclasses("")

    printmapping("build/proguard-mapping.txt")

    adaptresourcefilenames()
    optimizationpasses(9)
    allowaccessmodification()
    mergeinterfacesaggressively()
    renamesourcefileattribute("SourceFile")
    keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

//    keep("""class org.jetbrains.plugins.template.MyBundle
//    """.trimIndent())
//
//    keep("""class beansoft.mykeep.**
//    """.trimIndent())
//    keep("class beansoft.mykeep.**{*;}")

}