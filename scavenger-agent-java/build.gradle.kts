import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.freefair.lombok") version "6.5.1"
}

java {
    withJavadocJar()
    withSourcesJar()
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
    implementation("net.bytebuddy:byte-buddy:1.12.23")
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
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
            artifactId = project.name
            from(components["java"])

            pom {
                name.set("Scavenger java agent")
                description.set("Java agent for Scavenger, a runtime dead code analysis tool")
                url.set("https://github.com/naver/scavenger")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("taeyeon-Kim")
                        name.set("Taeyeon Kim")
                        email.set("duszzang@gmail.com")
                    }
                    developer {
                        id.set("dbgsprw")
                        name.set("Minjeong Yoo")
                        email.set("dbgsprw@gmail.com")
                    }
                    developer {
                        id.set("kojandy")
                        name.set("Ohjun Kwon")
                        email.set("kojandy@gmail.com")
                    }
                    developer {
                        id.set("junoyoon")
                        name.set("JunHo Yoon")
                        email.set("junoyoon@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git:github.com/naver/scavenger.git")
                    developerConnection.set("scm:git:ssh://github.com/naver/scavenger.git")
                    url.set("https://github.com/naver/scavenger")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
            name = "OSSRH"
            url = if (version.toString().endsWith("-SNAPSHOT")) {
                uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }
}

signing {
    sign(publishing.publications["agent"])
}
