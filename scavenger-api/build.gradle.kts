import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.8.10"
    val springBootVersion = "2.5.12"
    val springDependencyManagementVersion = "1.0.11.RELEASE"

    kotlin("jvm") version kotlinVersion
    id("org.gradle.idea")
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("com.adarshr.test-logger") version "3.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":scavenger-entity"))
    implementation(project(":scavenger-schema"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-repository:${property("springDataJdbcPlusVersion")}")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-sql:${property("springDataJdbcPlusVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("mysql:mysql-connector-java")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.h2database:h2:2.1.210")
    implementation("org.liquibase:liquibase-core")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("net.ttddyy:datasource-proxy:1.7")
    implementation("com.github.vertical-blank:sql-formatter:2.0.4")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.4")
    }
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<BootJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<ProcessResources> {
    dependsOn(tasks.getByPath(":scavenger-frontend:vite"))
}
