import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "dev.lunapuppygirl.lunarstorage"
version = "0.0.1-SNAPSHOT"
description = "LunarStorage"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

node {
    version = "22.15.0"
    npmVersion = "10.9.2"
    download = true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("com.bucket4j:bucket4j_jdk17-core:8.18.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    implementation("com.google.code.gson:gson:2.14.0")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "dev.lunapuppygirl.lunarstorage.LunarStorageApplication"
        )
    }
}

tasks.register<NpmTask>("tailwindBuild") {
    description = "Build CSS file"
    args = listOf("run", "tw:build")
    workingDir = file("src/main/frontend")
}

tasks.register<NpmTask>("tailwindWatch") {
    description = "Build CSS file every change"
    args = listOf("run", "tw:watch")
    workingDir = file("src/main/frontend")
}

tasks.named("processResources") {
    dependsOn("tailwindBuild")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
