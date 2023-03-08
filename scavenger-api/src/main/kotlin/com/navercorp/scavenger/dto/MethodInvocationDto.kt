package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.MethodInvocationEntity

data class MethodInvocationDto(
    val signature: String,
    val invokedAtMillis: Long

) {

    companion object {
        fun from(methodInvocationEntity: MethodInvocationEntity): MethodInvocationDto {
            return MethodInvocationDto(
                methodInvocationEntity.signature,
                methodInvocationEntity.invokedAtMillis
            )
        }
    }
}
