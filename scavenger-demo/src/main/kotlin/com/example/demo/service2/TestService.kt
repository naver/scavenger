package com.example.demo.service2

import com.example.demo.annotation.ScavengerEnabled
import com.example.demo.service.MyInterface
import com.example.demo.service.MyParentService
import org.springframework.stereotype.Service

@ScavengerEnabled
@Service
class TestService : MyParentService(), MyInterface {
    fun call(): String {
        return "test"
    }
}
