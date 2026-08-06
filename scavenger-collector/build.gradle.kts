import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "2.3.21"
    val springBootVersion = "3.5.9"
    val springDependencyManagementVersion = "1.1.7"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.gradle.idea")
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.springframework.boot") version springBootVersion
    id("com.adarshr.test-logger") version "4.0.0"
}

extra["tomcat.version"] = "10.1.50"

dependencies {
    implementation(project(":scavenger-entity"))
    implementation(project(":scavenger-model"))
    implementation(project(":scavenger-old-model"))
    implementation(project(":scavenger-schema"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("com.linecorp.armeria:armeria-bom:1.31.3"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-repository:${property("springDataJdbcPlusVersion")}")
    implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-sql:${property("springDataJdbcPlusVersion")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // test
    implementation("com.mysql:mysql-connector-j")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.liquibase:liquibase-core")
    implementation("com.linecorp.armeria:armeria-spring-boot3-starter")
    implementation("com.linecorp.armeria:armeria-tomcat10")
    implementation("com.linecorp.armeria:armeria-grpc")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("net.ttddyy:datasource-proxy:1.11.0")
    implementation("com.github.vertical-blank:sql-formatter:2.0.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.mockito:mockito-inline:4.3.1")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.rest-assured:rest-assured:${property("restAssuredVersion")}")
    testImplementation("io.rest-assured:rest-assured-common:${property("restAssuredVersion")}")
    testImplementation("io.rest-assured:spring-mock-mvc:${property("restAssuredVersion")}")
    testImplementation("io.rest-assured:spring-mock-mvc-kotlin-extensions:${property("restAssuredVersion")}")
    testImplementation("io.rest-assured:json-path:${property("restAssuredVersion")}")
    testImplementation("io.rest-assured:xml-path:${property("restAssuredVersion")}")
    testImplementation("io.grpc:grpc-testing:${property("grpcVersion")}")
    testImplementation("io.grpc:grpc-inprocess:${property("grpcVersion")}")
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
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

repositories {
    mavenCentral()
}
