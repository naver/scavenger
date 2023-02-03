package com.example.demo.controller

import com.example.demo.client.LocalApiClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FeignController(
    val localApiClient: LocalApiClient
) {
    @GetMapping("/run-feign")
    fun runFeignRun(): String {
        return localApiClient.testFeign()
    }

    @GetMapping("/test-feign")
    fun testFeign(): String {
        return System.currentTimeMillis().toString()
    }
}
