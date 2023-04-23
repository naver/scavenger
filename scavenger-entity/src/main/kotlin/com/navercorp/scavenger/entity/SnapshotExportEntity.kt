package com.navercorp.scavenger.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("snapshots")
data class SnapshotExportEntity(
    @Column("filterInvokedAtMillis")
    val filterInvokedAtMillis: Long? = null,

    @Column("packages")
    val packages: String? = null,

    @Column("status")
    val status: String? = null,

    @Column("excludeAbstract")
    val excludeAbstract: Int? = null,

    @Column("parent")
    val parent: String,

    @Column("signature")
    val signature: String,

    @Column("type")
    val type: String? = null,

    @Column("usedCount")
    val usedCount: Int,

    @Column("unusedCount")
    val unusedCount: Int,

    @Column("lastInvokedAtMillis")
    val lastInvokedAtMillis: Long? = null,
)
