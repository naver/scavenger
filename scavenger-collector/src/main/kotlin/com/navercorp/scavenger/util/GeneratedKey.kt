package com.navercorp.scavenger.util

import org.springframework.jdbc.support.GeneratedKeyHolder

fun GeneratedKeyHolder.getFirstKey(): Long? =
    if (this.keyList.isEmpty()) {
        null
    } else {
        val keyIter = this.keyList[0].values.iterator()
        if (keyIter.hasNext()) {
            keyIter.next() as? Long
        } else {
            null
        }
    }
