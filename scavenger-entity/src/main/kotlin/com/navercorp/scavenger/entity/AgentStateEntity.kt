package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("agent_state")
data class AgentStateEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("customerId")
    val customerId: Long,

    @Column("jvmUuid")
    val jvmUuid: String,

    @Column("createdAt")
    val createdAt: Instant = Instant.now(),

    @Column("lastPolledAt")
    val lastPolledAt: Instant,

    @Column("nextPollExpectedAt")
    val nextPollExpectedAt: Instant,

    @Column("timestamp")
    val timestamp: Instant = Instant.now(),

    @Column("enabled")
    val enabled: Boolean,
)
