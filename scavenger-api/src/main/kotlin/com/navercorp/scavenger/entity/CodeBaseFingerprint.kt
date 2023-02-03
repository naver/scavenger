package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("codebase_fingerprints")
data class CodeBaseFingerprint(
    @Id
    @Column("id")
    val id: Long,

    @Column("customerId")
    val customerId: Long,
)
