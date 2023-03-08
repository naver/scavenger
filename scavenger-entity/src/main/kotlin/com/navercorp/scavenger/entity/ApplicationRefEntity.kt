package com.navercorp.scavenger.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("snapshot_application")
data class ApplicationRefEntity(
    @Column("applicationId")
    val applicationId: Long,

    @Column("customerId")
    val customerId: Long,

    @Column("snapshotId")
    val snapshotId: Long?
)
