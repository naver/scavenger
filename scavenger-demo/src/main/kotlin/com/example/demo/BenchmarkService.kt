package com.example.demo

import org.springframework.stereotype.Service

@Service
class BenchmarkService {
    fun doNothing(a: Int): Int {
        return a + 1
    }
}
