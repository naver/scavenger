package com.example.demo.service

class PojoService {
    var test = 0

    fun pojoHello(): Int {
        pojoPrivate()
        return 1
    }

    fun getTestWithNoReturn() {
        pojoPrivate()
    }

    fun getTestWithOneParameter(param: Int): Int {
        return param
    }

    private fun pojoPrivate(): Int {
        return 2
    }
}
