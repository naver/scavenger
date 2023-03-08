package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.CustomerEntity

data class CustomerDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(entity: CustomerEntity): CustomerDto {
            return CustomerDto(id = entity.id, name = entity.name)
        }
    }
}
