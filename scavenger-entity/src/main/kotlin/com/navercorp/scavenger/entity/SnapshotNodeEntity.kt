package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("snapshot_nodes")
data class SnapshotNodeEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("snapshotId")
    val snapshotId: Long,

    @Column("signature")
    val signature: String,

    @Column("type")
    val type: String? = null,

    @Column("lastInvokedAtMillis")
    val lastInvokedAtMillis: Long?,

    @Column("parent")
    val parent: String,

    @Column("usedCount")
    val usedCount: Int = 0,

    @Column("unusedCount")
    val unusedCount: Int = 0,

    @Column("customerId")
    val customerId: Long
)
