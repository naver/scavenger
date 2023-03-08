package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.CustomerEntity

data class CustomerDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(customerEntity: CustomerEntity): CustomerDto {
            return CustomerDto(
                id = requireNotNull(customerEntity.id),
                name = customerEntity.name
            )
        }
    }
}
