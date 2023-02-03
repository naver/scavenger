package com.example.demo.service

import com.example.demo.annotation.ScavengerEnabled

@ScavengerEnabled
abstract class MyParentService {
    open fun test(): Int {
        try {
            throw Exception()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 1
    }
}
