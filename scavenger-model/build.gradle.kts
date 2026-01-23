import com.google.protobuf.gradle.id

plugins {
    `java-library`
    idea
    kotlin("jvm") version "2.3.0"
    id("com.google.protobuf") version "0.9.5"
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-java:${property("protobufVersion")}")
    api("com.google.protobuf:protobuf-kotlin:${property("protobufVersion")}")
    api("io.grpc:grpc-stub:${property("grpcVersion")}")
    api("io.grpc:grpc-kotlin-stub:${property("grpcKotlinVersion")}")
    api("io.grpc:grpc-protobuf:${property("grpcVersion")}")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

kotlin {
    jvmToolchain(8)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${property("protobufVersion")}"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${property("grpcVersion")}"
        }

        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${property("grpcKotlinVersion")}:jdk8@jar"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
                id("python") {
                    outputSubDir = "$projectDir/scavenger-agnet-python"
                }
            }
        }
    }
}
