package com.example.demo.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class AspectConfig {

    @Around("@annotation(com.example.demo.annotation.AopAnnotation)")
    fun aroundMethod(joinPoint: ProceedingJoinPoint) {
        joinPoint.proceed()
    }
}
