package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.repository.CustomerRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class McpLicenseService(
    private val customerRepository: CustomerRepository
) {
    // unknown keys throw and are therefore never cached — only valid keys stay in the cache
    @Cacheable(MCP_LICENSE_CACHE)
    fun resolveCustomerId(licenseKey: String): Long {
        val customer = customerRepository.findByLicenseKey(licenseKey).orElse(null)
            ?: throw McpException(
                McpError(
                    code = McpError.Code.AUTH_INVALID,
                    message = "Invalid licenseKey.",
                    hint = "Verify the licenseKey value against the Scavenger workspace."
                )
            )
        return customer.id
    }

    companion object {
        const val MCP_LICENSE_CACHE = "mcpLicense"
    }
}
