package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.MethodInvocation

data class MethodInvocationDto(
    val signature: String,
    val invokedAtMillis: Long

) {

    companion object {
        fun from(methodInvocation: MethodInvocation): MethodInvocationDto {
            return MethodInvocationDto(
                methodInvocation.signature,
                methodInvocation.invokedAtMillis
            )
        }
    }
}
