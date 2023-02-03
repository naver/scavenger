import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "3.5.0"
}

node {
    // Whether to download and install a specific Node.js version or not
    // If false, it will use the globally installed Node.js
    // If true, it will download node using above parameters
    // Note that npm is bundled with Node.js
    download.set(true)

    // Version of node to download and install (only used if download is true)
    // It will be unpacked in the workDir
    version.set("16.18.0")
}

tasks.register<NpmTask>("vite") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
}

tasks.register<NpmTask>("watch") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "watch"))
}
