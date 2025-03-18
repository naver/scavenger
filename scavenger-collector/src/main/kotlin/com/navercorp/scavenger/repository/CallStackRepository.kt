package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CallStackEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface CallStackRepository : DelegatableJdbcRepository<CallStackEntity, Long>
