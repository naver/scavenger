package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.Environment

data class EnvironmentDto(
    val id: Long,
    val name: String
) {

    companion object {
        fun from(environment: Environment): EnvironmentDto {
            return EnvironmentDto(environment.id, environment.name)
        }
    }
}
