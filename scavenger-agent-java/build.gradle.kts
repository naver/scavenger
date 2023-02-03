import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.freefair.lombok") version "6.5.1"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")

    manifest {
        attributes["Premain-Class"] = "com.navercorp.scavenger.javaagent.ScavengerAgent"
        attributes["Implementation-Version"] = project.version
    }

    dependsOn("relocateShadowJar")
    mergeServiceFiles()

    minimize {
        exclude(dependency("io.grpc:grpc-stub:.*"))
        exclude(dependency("io.grpc:grpc-okhttp:.*"))
        exclude(dependency("com.squareup.okhttp3:okhttp:.*"))
    }
    exclude("**/*.kotlin_*")
}

tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = "sc"
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":scavenger-demo:build")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":scavenger-model"))
    implementation("net.bytebuddy:byte-buddy:1.12.6")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.google.protobuf:protobuf-java-util:${property("protobufVersion")}")
    implementation("io.grpc:grpc-stub:${property("grpcVersion")}")
    implementation("io.grpc:grpc-okhttp:${property("grpcVersion")}")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.mockito:mockito-inline:4.3.1")
}

tasks.withType<ProcessResources> {
    filesMatching("internal.properties") {
        expand(mapOf("scavenger_version" to project.version))
    }
}


publishing {
    publications {
        create<MavenPublication>("agent") {
            groupId = "com.navercorp.scavenger"
            artifactId = "javaagent"
            from(components["java"])
        }
    }
    repositories {
        maven {
            credentials {
                username = ""
                password = ""
            }
            name = "navercorp"
            url = if (version.toString().endsWith("-SNAPSHOT")) {
                uri("")
            } else {
                uri("")
            }
        }
    }
}
