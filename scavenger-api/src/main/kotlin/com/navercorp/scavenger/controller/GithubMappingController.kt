package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.GithubMappingDto
import com.navercorp.scavenger.exception.DuplicateKeyException
import com.navercorp.scavenger.service.GithubMappingService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class GithubMappingController(val githubMappingService: GithubMappingService) {

    @GetMapping("/customers/{customerId}/mappings")
    fun getMappings(@PathVariable customerId: Long): List<GithubMappingDto> {
        return githubMappingService.getGithubMappings(customerId)
    }

    @PostMapping("/customers/{customerId}/mappings")
    fun createMapping(
        @PathVariable customerId: Long,
        @RequestBody createGithubMappingRequestParams: CreateGithubMappingRequestParams
    ): GithubMappingDto {
        return try {
            githubMappingService.createGithubMapping(
                customerId,
                createGithubMappingRequestParams.basePackage,
                createGithubMappingRequestParams.url
            )
        } catch (duplicateKeyException: org.springframework.dao.DuplicateKeyException) {
            throw DuplicateKeyException()
        }
    }

    @DeleteMapping("/customers/{customerId}/mappings/{mappingId}")
    fun deleteMapping(
        @PathVariable customerId: Long,
        @PathVariable mappingId: Long
    ) {
        githubMappingService.deleteGithubMapping(customerId, mappingId)
    }

    data class CreateGithubMappingRequestParams(
        val basePackage: String,
        val url: String
    )
}
