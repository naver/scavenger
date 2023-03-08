package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ApplicationEntity

data class ApplicationDto(
    val id: Long,
    val name: String
) {

    companion object {
        fun from(applicationEntity: ApplicationEntity): ApplicationDto {
            return ApplicationDto(applicationEntity.id, applicationEntity.name)
        }
    }
}
