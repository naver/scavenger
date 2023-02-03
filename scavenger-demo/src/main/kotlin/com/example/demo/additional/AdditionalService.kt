package com.example.demo.additional

class AdditionalService {
    fun get(): Int = WOW().doSth()

    class WOW {
        fun doSth(): Int {
            return 2
        }
    }
}
