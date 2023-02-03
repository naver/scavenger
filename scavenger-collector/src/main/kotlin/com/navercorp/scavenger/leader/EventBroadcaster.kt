package com.navercorp.scavenger.leader

interface EventBroadcaster<T> {
    fun addListener(listener: EventListener<T>)
    fun removeListener(listener: EventListener<T>)
    fun broadcast(payload: T)
}
