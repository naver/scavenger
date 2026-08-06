package com.navercorp.scavenger.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant

@Table("call_stacks")
data class CallStackEntity(
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

    @Column("callerSignatureHash")
    val callerSignatureHash: String,

    @Column("invokedAtMillis")
    val invokedAtMillis: Long,

    @Column("createdAt")
    val createdAt: Instant
)
