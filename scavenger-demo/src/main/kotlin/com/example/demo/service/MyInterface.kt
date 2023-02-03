package com.example.demo.service

import com.example.demo.annotation.ScavengerEnabled

@ScavengerEnabled
interface MyInterface {
    fun test(): Int
}
