package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("codebase_fingerprints")
data class CodeBaseFingerprintEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("applicationId")
    val applicationId: Long,

    @Column("codeBaseFingerprint")
    val codeBaseFingerprint: String,

    @Column("createdAt")
    val createdAt: Instant = Instant.now(),

    @Column("publishedAt")
    val publishedAt: Instant
)
