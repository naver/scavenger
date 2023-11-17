package com.example.demo.controller

import com.example.demo.additional.AdditionalService
import com.example.demo.service.AsyncService
import com.example.demo.service.BridgeService
import com.example.demo.service.CglibProxyService
import com.example.demo.service.DynamicProxyService
import com.example.demo.service.PojoService
import com.example.demo.service2.TestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MyController(
    private val myService: BridgeService,
    private val dynamicProxyService: DynamicProxyService,
    private val cglibProxyService: CglibProxyService,
    private val asyncService: AsyncService,
    private val testService: TestService
): BaseController() {
    @GetMapping("")
    fun hello(): String {
        dynamicProxyService.aspect()
        cglibProxyService.aspect()
        return "myservice called ${myService.doSth()} / pojoService called ${PojoService().pojoHello()} / myService2 called ${testService.call()}"
    }

    @GetMapping("/additional")
    fun additional(): String {
        return "additional service is called ${AdditionalService().get()}"
    }

    @GetMapping("/async")
    fun async(): String {
        return asyncService.asyncJob().toString()
    }

    @RestController
    class MyTest {
        @GetMapping("/nesting")
        fun nesting(): String {
            return "additional service is called ${AdditionalService().get()}"
        }
    }
}
