package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.Application

data class ApplicationDto(
    val id: Long,
    val name: String
) {

    companion object {
        fun from(application: Application): ApplicationDto {
            return ApplicationDto(application.id, application.name)
        }
    }
}
