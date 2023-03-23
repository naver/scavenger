plugins {
    idea
}

allprojects {
    group = "com.navercorp.scavenger"
    version = "1.0.4"

    repositories {
        mavenCentral()
    }

    // setting for frontend build
    apply(plugin = "idea")
    idea {
        module {
            outputDir = file("build/classes/kotlin/main")
            testOutputDir = file("build/classes/kotlin/test")
        }
    }
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
