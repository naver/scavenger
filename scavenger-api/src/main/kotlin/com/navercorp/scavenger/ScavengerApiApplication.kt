package com.navercorp.scavenger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ScavengerApiApplication

fun main(args: Array<String>) {
    runApplication<ScavengerApiApplication>(*args)
}
