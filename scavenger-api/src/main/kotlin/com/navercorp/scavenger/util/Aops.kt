package com.navercorp.scavenger.util

import org.springframework.aop.framework.AopContext
import org.springframework.aop.support.AopUtils

fun <T> proxy(target: T): T {
    return if (AopUtils.isAopProxy(target)) {
        target
    } else {
        try {
            @Suppress("UNCHECKED_CAST")
            AopContext.currentProxy() as T
        } catch (e: IllegalStateException) {
            return target
        }
    }
}
