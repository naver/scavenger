import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "2.3.21"
    val springBootVersion = "3.5.9"
    val springDependencyManagementVersion = "1.1.7"

    kotlin("jvm") version kotlinVersion
    id("org.gradle.idea")
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("com.adarshr.test-logger") version "4.0.0"
}

repositories {
    mavenCentral()
}

extra["tomcat.version"] = "10.1.50"

dependencies {
    implementation(project(":scavenger-entity"))
    implementation(project(":scavenger-schema"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Spring AI 1.1.x — 2.0 requires Spring Boot 4.0; this project is on Boot 3.5.9
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-repository:${property("springDataJdbcPlusVersion")}")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-sql:${property("springDataJdbcPlusVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.mysql:mysql-connector-j")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.liquibase:liquibase-core")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("net.ttddyy:datasource-proxy:1.11.0")
    implementation("com.github.vertical-blank:sql-formatter:2.0.5")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.springframework.ai:spring-ai-bom:1.1.8")
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict"))
        javaParameters = true
    }
}

tasks.withType<BootJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<ProcessResources> {
    dependsOn(tasks.getByPath(":scavenger-frontend:vite"))
}
