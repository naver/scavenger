package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.EnvironmentEntity

data class EnvironmentDto(
    val id: Long,
    val name: String
) {

    companion object {
        fun from(environmentEntity: EnvironmentEntity): EnvironmentDto {
            return EnvironmentDto(environmentEntity.id, environmentEntity.name)
        }
    }
}
