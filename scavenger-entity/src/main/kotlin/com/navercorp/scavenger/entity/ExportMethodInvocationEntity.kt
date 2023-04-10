package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("invocations")
data class ExportMethodInvocationEntity(
    @Id
    val id: Long = 0,

    @Column("modifiers")
    val modifiers: String,

    @Column("signature")
    val signature: String,

    @Column("createdAt")
    val createdAt: Instant = Instant.now(),

    @Column("lastSeenAtMillis")
    val lastSeenAtMillis: Long? = null,

    @Column("invokedAtMillis")
    val invokedAtMillis: Long,

    @Column("status")
    val status: String,

    @Column("timestamp")
    val timestamp: Instant
)
