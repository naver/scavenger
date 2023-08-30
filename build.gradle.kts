plugins {
    idea
    id("net.researchgate.release") version "3.0.2"
}

allprojects {
    group = "com.navercorp.scavenger"

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

release {
    val releaseVersion = if (hasProperty("release.releaseVersion")) {
        property("release.releaseVersion")
    } else {
        version
    }

    pushReleaseVersionBranch.set("release/${releaseVersion}")
    tagTemplate.set("v${releaseVersion}")
    preTagCommitMessage.set("Release ")
    newVersionCommitMessage.set("Update next development version after Release")
    with(git) {
        requireBranch.set("main")
    }
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":scavenger-old-agent-java").afterEvaluate {
    tasks.all {
        onlyIf {
            project.hasProperty("oldAgent")
        }
    }
}
