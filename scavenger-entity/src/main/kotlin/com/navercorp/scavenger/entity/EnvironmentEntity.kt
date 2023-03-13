package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("environments")
data class EnvironmentEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("name")
    val name: String,

    @Column("customerId")
    val customerId: Long,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("updatedAt")
    val updatedAt: Instant = Instant.now(),

    @Column("enabled")
    val enabled: Boolean,
)
