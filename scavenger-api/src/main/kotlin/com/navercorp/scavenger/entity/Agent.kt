package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("agent_state")
data class Agent(
    @Id
    @Column("id")
    val id: Long,

    @Column("jvmUuid")
    val jvmUuid: String,

    @Column("createdAt")
    val createdAt: Instant,

    @Column("lastPolledAt")
    val lastPolledAt: Instant,

    @Column("nextPollExpectedAt")
    val nextPollExpectedAt: Instant,

    @Column("hostname")
    val hostname: String,

    @Column("applicationVersion")
    val applicationVersion: String,

    @Column("applicationName")
    val applicationName: String,

    @Column("environmentName")
    val environmentName: String
)
