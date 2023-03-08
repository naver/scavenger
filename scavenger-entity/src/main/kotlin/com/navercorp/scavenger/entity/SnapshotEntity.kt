package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("snapshots")
data class SnapshotEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("name")
    val name: String,

    @Column("customerId")
    val customerId: Long,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("filterInvokedAtMillis")
    val filterInvokedAtMillis: Long,

    @Column("packages")
    val packages: String = "",

    @MappedCollection(idColumn = "snapshotId")
    val applications: Set<ApplicationRefEntity>,

    @MappedCollection(idColumn = "snapshotId")
    val environments: Set<EnvironmentRefEntity>,
)
