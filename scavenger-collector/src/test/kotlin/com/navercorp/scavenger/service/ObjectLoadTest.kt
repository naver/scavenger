package com.navercorp.scavenger.service

import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.io.ObjectInputStream

class ObjectLoadTest {

    @Test
    fun loadObject() {
        ObjectInputStream(ClassPathResource("10.113.121.208-1635507481829.upload").inputStream).use {
            val readObject = it.readObject()
            println(readObject)
        }
    }
}
