plugins {
    val kotlinVersion = "1.9.24"

    java
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.2.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}
