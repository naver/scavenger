package com.navercorp.scavenger.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("customers")
data class Customer(
    @Id
    val id: Long = 0,

    @Column("name")
    val name: String,

    @Column("licenseKey")
    val licenseKey: String,
)
