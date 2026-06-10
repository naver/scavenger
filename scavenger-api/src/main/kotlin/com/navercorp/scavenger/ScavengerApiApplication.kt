package com.navercorp.scavenger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
class ScavengerApiApplication

fun main(args: Array<String>) {
    runApplication<ScavengerApiApplication>(*args)
}
