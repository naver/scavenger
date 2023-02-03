package com.navercorp.scavenger.repository

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class CustomerDao(
    entityJdbcProvider: EntityJdbcProvider,
    customerRepository: CustomerRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    CustomerRepository by customerRepository
