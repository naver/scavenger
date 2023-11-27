rootProject.name = "scavenger"

include("scavenger-collector")
include("scavenger-api")
include("scavenger-demo")
include("scavenger-schema")
include("scavenger-entity")
include("scavenger-agent-java")
include("scavenger-agent-python")
include("scavenger-model")
include("scavenger-old-agent-java")
include("scavenger-old-model")
include("scavenger-demo")
include("scavenger-demo-extension")
include("scavenger-frontend")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}
