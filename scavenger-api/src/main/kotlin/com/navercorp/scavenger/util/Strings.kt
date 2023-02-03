package com.navercorp.scavenger.util

object Strings {
    inline fun String?.ifNullOrEmpty(defaultValue: () -> String): String =
        if (isNullOrEmpty()) defaultValue() else this
}
