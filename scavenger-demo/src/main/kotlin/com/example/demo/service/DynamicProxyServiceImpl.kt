package com.example.demo.service

import com.example.demo.annotation.AopAnnotation
import org.springframework.stereotype.Service

@Service
class DynamicProxyServiceImpl : DynamicProxyService {

    @AopAnnotation
    override fun aspect(): String {
        return "test"
    }
}
