import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "2.2.0"
    val springBootVersion = "3.2.4"
    val springDependencyManagementVersion = "1.1.4"

    kotlin("jvm") version kotlinVersion
    id("org.gradle.idea")
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("com.adarshr.test-logger") version "3.0.0"
}

repositories {
    mavenCentral()
}

extra["tomcat.version"] = "10.1.42"

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
    implementation("com.mysql:mysql-connector-j")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.h2database:h2:2.1.210")
    implementation("org.liquibase:liquibase-core")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("net.ttddyy:datasource-proxy:1.7")
    implementation("com.github.vertical-blank:sql-formatter:2.0.4")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

kotlin {
    jvmToolchain(21) // Use Java 21 for Spring Boot compatibility, while Kotlin 2.2.0 provides Java 24 language support
}

ktlint {
    version.set("1.7.1")
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true) // Temporarily ignore ktlint failures for Java 24 migration
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        javaParameters.set(true)
        // jvmTarget defaults to toolchain version (21)
    }
}

tasks.withType<BootJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<ProcessResources> {
    dependsOn(tasks.getByPath(":scavenger-frontend:vite"))
}
