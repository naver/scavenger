package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("invocations")
data class InvocationEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("applicationId")
    val applicationId: Long,

    @Column("environmentId")
    val environmentId: Long,

    @Column("signatureHash")
    val signatureHash: String,

    @Column("invokedAtMillis")
    val invokedAtMillis: Long,

    @Column("status")
    val status: String,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("lastSeenAtMillis")
    val lastSeenAtMillis: Long?,

    @Column("timestamp")
    val timestamp: Instant
)
