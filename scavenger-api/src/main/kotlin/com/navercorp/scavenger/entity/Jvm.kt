package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("jvms")
data class Jvm(
    @Id
    @Column("id")
    val id: Long,

    @Column("applicationId")
    val applicationId: Long,

    @Column("environmentId")
    val environmentId: Long,

    @Column("customerId")
    val customerId: Long,
)
