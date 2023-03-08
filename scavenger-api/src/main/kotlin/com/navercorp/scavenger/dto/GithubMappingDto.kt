package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.GithubMappingEntity

data class GithubMappingDto(
    val id: Long,
    val customerId: Long,
    val basePackage: String,
    val url: String
) {
    companion object {
        fun from(githubMappingEntity: GithubMappingEntity): GithubMappingDto {
            return githubMappingEntity.run {
                GithubMappingDto(checkNotNull(id), customerId, basePackage, url)
            }
        }
    }
}
