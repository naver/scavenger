package com.navercorp.scavenger.repository

import com.navercorp.scavenger.repository.sql.LeadershipSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class LeadershipDao(
    entityJdbcProvider: EntityJdbcProvider
) : ExtendedJdbcDaoSupport(entityJdbcProvider), SingleValueSelectTrait {

    private val sql: LeadershipSql = super.sqls(::LeadershipSql)

    fun tryAcquireLeadership(memberId: String, now: Instant, expirationDeadline: Instant): Int {
        return update(
            sql.tryAcquireLeadership(),
            mapParameterSource()
                .addValue("memberId", memberId)
                .addValue("lastSeenActive", now)
                .addValue("lastSeenActiveWithMargin", expirationDeadline)
        )
    }

    fun forceReelection(): Int {
        return update(
            sql.forceReelection(),
            mapParameterSource()
        )
    }

    fun getLeader(): String? {
        return selectSingleValue(
            sql.selectLeader(),
            mapParameterSource(),
            String::class.java
        )
    }
}
