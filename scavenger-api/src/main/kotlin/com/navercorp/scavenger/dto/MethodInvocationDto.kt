package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.MethodInvocationEntity

data class MethodInvocationDto(
    val signature: String,
    val invokedAtMillis: Long

) {

    companion object {
        fun from(entity: MethodInvocationEntity): MethodInvocationDto {
            return MethodInvocationDto(entity.signature, entity.invokedAtMillis)
        }
    }
}
