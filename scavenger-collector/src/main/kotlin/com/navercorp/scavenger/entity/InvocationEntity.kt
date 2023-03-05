package com.navercorp.scavenger.entity

import io.codekvast.javaagent.model.v4.SignatureStatus4
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("invocations")
class InvocationEntity(
    @Id
    val id: Long,

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
    val status: SignatureStatus4,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("lastSeenAtMillis")
    val lastSeenAtMillis: Long?,

    @Column("timestamp")
    val timestamp: Instant
)
