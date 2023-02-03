package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
