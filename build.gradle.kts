plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "cn.lin037"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations.compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
}

repositories {
    mavenCentral()
}

dependencies {
    // SpringDoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    // SpringEmail
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 添加热启动支持
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Sa-Token
    implementation("cn.dev33:sa-token-spring-boot3-starter:1.44.0")
    implementation("cn.dev33:sa-token-redisson-spring-boot-starter:1.44.0")

    // 大模型Token计算
    implementation("com.knuddels:jtokkit:1.1.0")

    // LangChain4j
    implementation("dev.langchain4j:langchain4j-spring-boot-starter:1.3.0-beta9")
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot-starter:1.3.0-beta9")
    implementation("dev.langchain4j:langchain4j-community-dashscope-spring-boot-starter:1.3.0-beta9")
    implementation("dev.langchain4j:langchain4j-pgvector:1.3.0-beta9")

    // 排除存在漏洞的 poi-ooxml，使用最新稳定版
    implementation("dev.langchain4j:langchain4j-document-parser-apache-tika:1.3.0-beta9") {
        exclude(group = "org.apache.tika", module = "tika-core")
        exclude(group = "org.apache.tika", module = "tika-parsers")
    }
    // 手动指定安全版本（请根据实际情况替换为官方修复版本）
    implementation("org.apache.tika:tika-core:3.1.0")
    implementation("org.apache.tika:tika-parsers-standard-package:3.1.0")

    // 数据库
    implementation("org.postgresql:postgresql")
    implementation("com.alibaba:druid-spring-boot-starter:1.2.20")

    // Redis
    implementation("org.redisson:redisson-spring-boot-starter:3.45.0")

    // 持久层框架
    implementation("cn.xbatis:xbatis-spring-boot-starter:1.9.1-M5-spring-boot3")

    // Hutool工具包
    implementation("cn.hutool:hutool-crypto:5.8.36")
    implementation("cn.hutool:hutool-core:5.8.36")
    implementation("cn.hutool:hutool-json:5.8.36")
    implementation("cn.hutool:hutool-http:5.8.36")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // 日志
    implementation("org.slf4j:slf4j-api")

    // 测试
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.awaitility:awaitility:4.2.1")

    testImplementation("org.springframework.boot:spring-boot-starter-mail")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    // 添加FlexMark，用于Markdown解析
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
}

tasks.withType<Test> {
    useJUnitPlatform()
}