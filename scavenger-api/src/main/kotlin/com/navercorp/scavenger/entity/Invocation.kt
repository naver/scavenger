package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("invocations")
data class Invocation(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("customerId")
    val customerId: Long,

    @Column("applicationId")
    val applicationId: Long,

    @Column("environmentId")
    val environmentId: Long
)
