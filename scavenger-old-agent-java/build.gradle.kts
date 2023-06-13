import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
    java
    `maven-publish`
    signing
    id("io.freefair.lombok") version "6.5.1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.unbroken-dome.test-sets") version "4.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    val okhttp3Version = "3.12.13"
    val aspectjVersion = "1.9.2"
    val guavaVersion = "20.0"

    implementation(project(":scavenger-old-model"))
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttp3Version") // the last version that does not bring Kotlin stuff
    implementation("org.aspectj:aspectjweaver:$aspectjVersion")

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-migrationsupport:5.7.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:1.5.22.RELEASE")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.compileJava {
    sourceCompatibility = "7"
    targetCompatibility = "7"
}

tasks.compileTestJava {
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

lombok {
    version.set("1.18.20")
}

tasks.withType<Delombok> {
    nocopy.set(true)
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
    manifest {
        attributes["Automatic-Module-Name"] = "${group}-${project.name}"
        attributes["Created-By"] = "Hallin Information Technology AB"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Premain-Class"] = "io.codekvast.javaagent.CodekvastAgent"
        attributes["Specification-Version"] = project.version
    }

    dependencies {
        exclude(dependency("com.google.code.findbugs:jsr305:"))
        exclude(dependency("com.google.errorprone:error_prone_annotations:"))
        exclude(dependency("com.google.j2objc:j2objc-annotations:"))
        exclude(dependency("org.codehaus.mojo:animal-sniffer-annotations:"))
    }

    exclude("META-INF/maven/**")
    exclude("aspectj*.dtd")
    exclude("publicsuffixes.gz")
    exclude("module-info.class")

    relocate("afu", "ck.afu")
    relocate("aj.org", "ck.aj.org")
    relocate("com", "ck.com")
    relocate("javax.annotation", "ck.javax.annotation")
    relocate("javax.validation", "ck.javax.validation")
    relocate("okhttp3", "ck.okhttp3")
    relocate("okio", "ck.okio")
    relocate("org.aspectj", "ck.org.aspectj")
    relocate("org.checkerframework", "ck.org.checkerframework")

    minimize {
        exclude(dependency("org.aspectj:aspectjweaver:"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val javaPath = { version: Int ->
        try {
            "$version:" + javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(version))
            }.get().executablePath
        } catch (e : Exception) {
            logger.warn("The scavenger old agent should be built on JDK1.7. But it does not exist in this build", e)
            ""
        }
}

testSets {
    create("integrationTest")
}

dependencies {
    "integrationTestImplementation"("com.github.tomakehurst:wiremock:2.27.2")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-parent:1.5.22.RELEASE")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-validation:1.5.22.RELEASE")
    "integrationTestImplementation"("javax.annotation:javax.annotation-api:1.2")
    "integrationTestRuntimeOnly"("ch.qos.logback:logback-classic:1.2.3")
}

val integrationTestRuntimeClasspath = configurations.named("integrationTestRuntimeClasspath").get().asPath

tasks.named<JavaCompile>("compileIntegrationTestJava") {
    sourceCompatibility = "7"
    targetCompatibility = "7"
}

tasks.named<Test>("integrationTest") {
    dependsOn(tasks.shadowJar)
    shouldRunAfter(tasks.test)
    useJUnit()

    inputs.files(file("build.gradle.kts"))
    inputs.files(tasks.shadowJar.get().outputs.files)
    // inputs.files (sourceSets.integrationTest.output)
    outputs.dir(file("$buildDir/test-results/integrationTest"))

    systemProperty("integrationTest.codekvastAgent", tasks.shadowJar.get().outputs.files.asPath)
    systemProperty("integrationTest.classpath", "build/classes/java/integrationTest:$integrationTestRuntimeClasspath")
    systemProperty("integrationTest.javaPaths", javaPath(7))
}

publishing {
    publications {
        create<MavenPublication>("agent") {
            groupId = "com.navercorp.scavenger"
            artifactId = project.name
            from(components["java"])

            pom {
                name.set("Scavenger java agent for jdk7 support")
                description.set("Java agent for jdk7 support of Scavenger, a runtime dead code analysis tool")
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
