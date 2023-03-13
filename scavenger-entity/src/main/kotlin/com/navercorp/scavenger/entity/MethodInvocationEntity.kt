package com.navercorp.scavenger.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("invocations")
data class MethodInvocationEntity(
    @Column("signature")
    val signature: String,

    @Column("invokedAtMillis")
    val invokedAtMillis: Long,

    @Column("methodName")
    val methodName: String
)
