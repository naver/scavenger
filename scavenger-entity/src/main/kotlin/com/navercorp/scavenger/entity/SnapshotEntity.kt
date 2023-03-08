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
    val applications: MutableSet<ApplicationRefEntity> = mutableSetOf(),

    @MappedCollection(idColumn = "snapshotId")
    val environments: MutableSet<EnvironmentRefEntity> = mutableSetOf()
) {

    fun addApplication(applicationId: Long) {
        applications.add(ApplicationRefEntity(applicationId, this.customerId, id))
    }

    fun addEnvironment(environmentId: Long) {
        environments.add(EnvironmentRefEntity(environmentId, this.customerId, id))
    }
}
