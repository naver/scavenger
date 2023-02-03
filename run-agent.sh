#!/bin/bash

./gradlew build -p scavenger-agent-java -x test
./gradlew build -p scavenger-demo -x test

java -Dscavenger.configuration=./scavenger-demo/scavenger.conf  \
     -javaagent:$(find scavenger-agent-java/build/libs/scavenger-agent-*.jar | head -1) \
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar ./scavenger-demo/build/libs/scavenger-demo-1.0.1-SNAPSHOT.jar
