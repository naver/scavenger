package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.repository.CustomerRepository
import org.springframework.stereotype.Component

@Component
class McpCustomerResolver(
    private val customerRepository: CustomerRepository,
    private val mcpProperties: ScavengerMcpProperties
) {
    fun resolveCustomerId(customerName: String): Long {
        return customerRepository.findByNameAndGroupId(customerName, mcpProperties.defaultGroupId)
            .orElseThrow { IllegalArgumentException("Customer '$customerName' not found in group '${mcpProperties.defaultGroupId}'") }
            .id
    }

    fun getGroupId(): String = mcpProperties.defaultGroupId
}
