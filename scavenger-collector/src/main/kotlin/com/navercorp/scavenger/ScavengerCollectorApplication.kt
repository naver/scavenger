package com.navercorp.scavenger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ScavengerCollectorApplication

fun main(args: Array<String>) {
    runApplication<ScavengerCollectorApplication>(*args)
}
