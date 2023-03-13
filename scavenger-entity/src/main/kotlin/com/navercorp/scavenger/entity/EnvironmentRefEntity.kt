package com.navercorp.scavenger.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("snapshot_environment")
data class EnvironmentRefEntity(
    @Column("environmentId")
    val environmentId: Long,

    @Column("customerId")
    val customerId: Long,

    @Column("snapshotId")
    val snapshotId: Long?
)
