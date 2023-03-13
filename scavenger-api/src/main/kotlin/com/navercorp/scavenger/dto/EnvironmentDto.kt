package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.EnvironmentEntity

data class EnvironmentDto(
    val id: Long,
    val name: String
) {

    companion object {
        fun from(entity: EnvironmentEntity): EnvironmentDto {
            return EnvironmentDto(entity.id, entity.name)
        }
    }
}
