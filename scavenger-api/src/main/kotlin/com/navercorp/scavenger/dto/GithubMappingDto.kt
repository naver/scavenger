package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.GithubMapping

data class GithubMappingDto(
    val id: Long,
    val customerId: Long,
    val basePackage: String,
    val url: String
) {
    companion object {
        fun from(githubMapping: GithubMapping): GithubMappingDto {
            return githubMapping.run {
                GithubMappingDto(checkNotNull(id), customerId, basePackage, url)
            }
        }
    }
}
