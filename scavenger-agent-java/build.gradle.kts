import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    signing
    id("com.gradleup.shadow") version "8.3.3"
    id("io.freefair.lombok") version "8.6"
    id("org.unbroken-dome.test-sets") version "4.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    withJavadocJar()
    withSourcesJar()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")

    manifest {
        attributes["Premain-Class"] = "com.navercorp.scavenger.javaagent.ScavengerAgent"
        attributes["Implementation-Version"] = project.version
    }

    isEnableRelocation = true
    relocationPrefix = "sc"

    mergeServiceFiles()

    minimize {
        exclude(dependency("io.grpc:grpc-stub:.*"))
        exclude(dependency("io.grpc:grpc-okhttp:.*"))
        exclude(dependency("com.squareup.okhttp3:okhttp:.*"))
    }
    exclude("**/*.kotlin_*")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.check {
    dependsOn("integrationTest")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":scavenger-demo:build")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":scavenger-model"))
    implementation("net.bytebuddy:byte-buddy:1.14.3")
    implementation("org.ow2.asm:asm:9.5")
    implementation("org.ow2.asm:asm-tree:9.5")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.google.protobuf:protobuf-java-util:${property("protobufVersion")}")
    implementation("io.grpc:grpc-stub:${property("grpcVersion")}")
    implementation("io.grpc:grpc-okhttp:${property("grpcVersion")}")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.mockito:mockito-inline:4.3.1")
}

testSets {
    register("integrationTest")
}

dependencies {
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test:2.5.12")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-aop:2.5.12")
    "integrationTestImplementation"("com.github.tomakehurst:wiremock:2.27.2")
    "integrationTestImplementation"("org.grpcmock:grpcmock-junit5:0.13.0")
}

fun javaPaths(vararg versions: Int) = versions.joinToString(",",
    transform = { version: Int ->
        "$version:" + javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(version))
        }.get().executablePath
    })

val integrationTestRuntimeClasspath = configurations.named("integrationTestRuntimeClasspath").get().asPath

tasks.named<Test>("integrationTest") {
    dependsOn(tasks.shadowJar)
    mustRunAfter(tasks.jar)
    shouldRunAfter(tasks.test)
    useJUnitPlatform()

    inputs.files(file("build.gradle.kts"))
    inputs.files(tasks.shadowJar.get().outputs.files)
    outputs.dir(layout.buildDirectory.dir("test-results/integrationTest").get().asFile)

    systemProperty("integrationTest.scavengerAgent", tasks.shadowJar.get().outputs.files.asPath)
    systemProperty("integrationTest.classpath", "build/classes/java/integrationTest:$integrationTestRuntimeClasspath")
    systemProperty("integrationTest.javaPaths", javaPaths(8, 11, 17, 21))
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
                    developer {
                        id.set("sohyun-ku")
                        name.set("Sohyun Ku")
                        email.set("kusohyeon@gmail.com")
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
