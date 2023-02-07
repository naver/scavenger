plugins {
    java
    id("com.google.protobuf") version "0.8.18"
}

tasks.register<Exec>("protoc") {
    commandLine(
        "bash",
        "-c",
        "poetry run python -m grpc_tools.protoc -I ../scavenger-model/src/main/proto --python_out=./scavenger --pyi_out=./scavenger " +
            "--grpc_python_out=./scavenger ../scavenger-model/src/main/proto/com/navercorp/scavenger/model/*.proto &&" +
            "mv ./scavenger/com/navercorp/scavenger/model/* ./scavenger/model/ && rm -r ./scavenger/com &&" +
            "find ./scavenger -type f -name \"*.py*\" -exec sed -i '' -e 's/from com.navercorp.scavenger.model/from scavenger.model/g' {} \\;"
    )
}
