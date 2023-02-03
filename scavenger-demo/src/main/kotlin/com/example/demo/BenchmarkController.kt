package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.system.measureNanoTime

@RestController
class BenchmarkController(
    val benchmarkService: BenchmarkService
) {
    @GetMapping("/benchmark")
    fun benchmark(): String {
        val times = measureNanoTime {
            repeat(100) {
                repeat(10_000_000) {
                    benchmarkService.doNothing(it)
                }
                println("perform $it")
            }
        }
        return times.toString();
    }
}
