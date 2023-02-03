package com.example.demo.service

import org.springframework.stereotype.Service

@Service
class BridgeService(myService: MyService) {
    val aa: Any
    init {
        aa = myService
    }

    fun doSth(): Int {
        return (aa as MyService).test()
    }
}
