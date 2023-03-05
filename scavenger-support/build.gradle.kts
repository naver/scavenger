plugins {
    val kotlinVersion = "1.6.10"
    val springBootVersion = "2.5.12"
    val springDependencyManagementVersion = "1.0.11.RELEASE"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}

repositories {
    mavenCentral()
}
