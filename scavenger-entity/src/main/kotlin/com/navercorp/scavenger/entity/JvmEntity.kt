package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("jvms")
data class JvmEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("applicationId")
    val applicationId: Long,

    @Column("environmentId")
    val environmentId: Long,

    @Column("uuid")
    val uuid: String,

    @Column("hostname")
    val hostname: String,

    @Column("codeBaseFingerprint")
    val codeBaseFingerprint: String?,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("publishedAt")
    val publishedAt: Instant,

)
