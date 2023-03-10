package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("methods")
data class MethodEntity(
    @Id
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("visibility")
    val visibility: String? = null,

    @Column("signature")
    val signature: String? = null,

    @Column("signatureHash")
    val signatureHash: String,

    @Column("createdAt")
    val createdAt: Instant = Instant.now(),

    @Column("lastSeenAtMillis")
    val lastSeenAtMillis: Long? = null,

    @Column("declaringType")
    val declaringType: String? = null,

    @Column("methodName")
    val methodName: String? = null,

    @Column("modifiers")
    val modifiers: String? = null,

    @Column("garbage")
    val garbage: Boolean,
)
