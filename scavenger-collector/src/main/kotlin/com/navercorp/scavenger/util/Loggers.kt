package com.navercorp.scavenger.util

import org.slf4j.MDC

fun withCustomerIdMdc(customerId: Long, func: () -> Unit) {
    MDC.put("customerId", customerId.toString())
    try {
        func()
    } finally {
        MDC.remove("customerId")
    }
}
