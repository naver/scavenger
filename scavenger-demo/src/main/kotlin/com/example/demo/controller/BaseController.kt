package com.example.demo.controller

import com.example.demo.annotation.ScavengerEnabled

@ScavengerEnabled
open class BaseController {
    fun base(): String {
        try {
            throw Exception()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "hello"
    }
}
