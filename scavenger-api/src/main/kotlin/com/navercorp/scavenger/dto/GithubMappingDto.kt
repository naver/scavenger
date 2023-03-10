package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.GithubMappingEntity

data class GithubMappingDto(
    val id: Long,
    val customerId: Long,
    val basePackage: String,
    val url: String
) {
    companion object {
        fun from(entity: GithubMappingEntity): GithubMappingDto {
            return GithubMappingDto(entity.id, entity.customerId, entity.basePackage, entity.url)
        }
    }
}
