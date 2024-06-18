package com.navercorp.scavenger.leader

import io.github.oshai.kotlinlogging.KotlinLogging

class SimpleEventBroadcaster<T> : EventBroadcaster<T> {
    val logger = KotlinLogging.logger {}

    private val listeners = mutableListOf<EventListener<T>>()

    override fun addListener(listener: EventListener<T>) {
        synchronized(listeners) { listeners.add(listener) }
    }

    override fun removeListener(listener: EventListener<T>) {
        synchronized(listeners) { listeners.removeIf { obj: EventListener<T>? -> listener == obj } }
    }

    override fun broadcast(payload: T) {
        var copyOfListeners: List<EventListener<T>>
        synchronized(listeners) { copyOfListeners = ArrayList(listeners) }
        for (listener in copyOfListeners) {
            try {
                listener.onEvent(payload)
                logger.debug { "broadcast() Invoked listener: $listener" }
            } catch (ex: Exception) {
                logger.error(ex) { "broadcast() Invoked listener: $listener" }
            }
        }
    }
}
