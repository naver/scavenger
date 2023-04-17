package com.navercorp.scavenger.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("snapshots")
data class ExportSnapshotMethodEntity(
    @Column("filterInvokedAtMillis")
    val filterInvokedAtMillis: Long,

    @Column("packages")
    val packages: String? = null,

    @Column("status")
    val status: String? = null,

    @Column("excludeAbstract")
    val excludeAbstract: String? = null,

    @Column("parent")
    val parent: String? = null,

    @Column("signature")
    val signature: String,

    @Column("type")
    val type: String? = null,

    @Column("usedCount")
    val usedCount: Int = 0,

    @Column("unusedCount")
    val unusedCount: Int = 0,

    @Column("lastInvokedAtMillis")
    val lastInvokedAtMillis: Long? = null,
)
