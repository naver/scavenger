package com.navercorp.scavenger

import com.navercorp.scavenger.mcp.ScavengerMcpProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ScavengerMcpProperties::class)
class ScavengerApiApplication

fun main(args: Array<String>) {
    runApplication<ScavengerApiApplication>(*args)
}
