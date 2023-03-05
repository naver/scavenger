package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("environments")
data class EnvironmentEntity(
    @Id
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("name")
    val name: String,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("updatedAt")
    val updatedAt: Instant = Instant.now(),

    @Column("enabled")
    val enabled: Boolean,
)
