package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.CustomerDto
import com.navercorp.scavenger.service.CustomerService
import com.navercorp.scavenger.util.Strings.ifNullOrEmpty
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class CustomerController(
    val customerService: CustomerService,
    @Value("\${scavenger.group-id-header:}") val groupIdHeader: String,
    @Value("\${scavenger.user-id-header:}") val userIdHeader: String
) {

    @GetMapping("/customers")
    fun getCustomers(request: HttpServletRequest): List<CustomerDto> {
        val groupId = request.getHeader(groupIdHeader).ifNullOrEmpty { DEFAULT_GROUP_ID }
        return customerService.getCustomers(groupId)
    }

    @PostMapping("/customers")
    fun createCustomer(@RequestBody param: CreateCustomerRequestParams, request: HttpServletRequest): CustomerDto {
        val groupId = request.getHeader(groupIdHeader).ifNullOrEmpty { DEFAULT_GROUP_ID }
        val userId = request.getHeader(userIdHeader).ifNullOrEmpty { DEFAULT_USER_ID }
        return customerService.createCustomer(groupId, userId, param.name)
    }

    @DeleteMapping("/customers/{customerId}")
    fun deleteCustomer(@PathVariable customerId: Long, request: HttpServletRequest) {
        val groupId = request.getHeader(groupIdHeader).ifNullOrEmpty { DEFAULT_GROUP_ID }
        val userId = request.getHeader(userIdHeader).ifNullOrEmpty { DEFAULT_USER_ID }
        customerService.deleteCustomer(groupId, userId, customerId, false)
    }

    @PostMapping("/customers/{customerId}/reset")
    fun resetCustomer(@PathVariable customerId: Long, request: HttpServletRequest) {
        val groupId = request.getHeader(groupIdHeader).ifNullOrEmpty { DEFAULT_GROUP_ID }
        val userId = request.getHeader(userIdHeader).ifNullOrEmpty { DEFAULT_USER_ID }
        customerService.deleteCustomer(groupId, userId, customerId)
    }

    @GetMapping("/customers/_query")
    fun getCustomers(@RequestParam name: String, request: HttpServletRequest): CustomerDto {
        val groupId = request.getHeader(groupIdHeader).ifNullOrEmpty { DEFAULT_GROUP_ID }
        return customerService.getCustomerByName(groupId, name)
    }

    data class CreateCustomerRequestParams(
        val name: String
    )

    companion object {
        private const val DEFAULT_GROUP_ID = "default-group"
        private const val DEFAULT_USER_ID = "anonymous"
    }
}
