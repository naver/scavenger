package com.example.demo.client

import com.example.demo.annotation.ScavengerEnabled
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@ScavengerEnabled
@FeignClient(name = "LocalApiClient", url = "http://localhost:8090")
interface LocalApiClient {

    @GetMapping("test-feign")
    fun testFeign(): String
}
