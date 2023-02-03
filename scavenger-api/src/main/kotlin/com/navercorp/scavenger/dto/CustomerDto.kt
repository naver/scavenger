package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.Customer

data class CustomerDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(customer: Customer): CustomerDto {
            return CustomerDto(
                id = requireNotNull(customer.id),
                name = customer.name
            )
        }
    }
}
