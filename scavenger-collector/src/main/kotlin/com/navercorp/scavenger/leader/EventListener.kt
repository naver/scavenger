package com.navercorp.scavenger.leader

interface EventListener<T> {
    fun onEvent(payload: T)
}
