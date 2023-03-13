package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("github_mappings")
data class GithubMappingEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("package")
    val basePackage: String,

    @Column("url")
    val url: String,
)
