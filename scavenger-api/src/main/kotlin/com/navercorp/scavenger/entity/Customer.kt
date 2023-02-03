package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("customers")
data class Customer(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("name")
    val name: String,

    @Column("groupId")
    val groupId: String,

    @Column("licenseKey")
    val licenseKey: String,
)
