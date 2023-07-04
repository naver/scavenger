package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.GithubMappingDto
import com.navercorp.scavenger.entity.GithubMappingEntity
import com.navercorp.scavenger.repository.GithubMappingRepository
import org.springframework.stereotype.Service

@Service
class GithubMappingService(val githubMappingRepository: GithubMappingRepository) {

    fun getGithubMappings(customerId: Long): List<GithubMappingDto> {
        return githubMappingRepository.findAllByCustomerId(customerId).map { GithubMappingDto.from(it) }
    }

    fun createGithubMapping(customerId: Long, basePackage: String, url: String): GithubMappingDto {
        val githubMappingEntity = GithubMappingEntity(customerId = customerId, basePackage = basePackage, url = url)
        return githubMappingRepository.insert(githubMappingEntity).let {
            GithubMappingDto.from(it)
        }
    }

    fun deleteGithubMapping(customerId: Long, mappingId: Long) {
        githubMappingRepository.deleteByCustomerIdAndId(customerId, mappingId)
    }
}
