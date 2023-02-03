plugins {
    java
    id("org.gradle.idea")
    id("com.adarshr.test-logger") version "3.0.0"
    id("io.freefair.lombok") version "6.5.1"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.6.1")
    implementation("javax.validation:validation-api:1.1.0.Final")

    testImplementation("com.google.code.gson:gson:2.8.9")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.compileJava {
    sourceCompatibility = "7"
    targetCompatibility = "7"
}

tasks.compileTestJava {
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
