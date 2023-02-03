package com.navercorp.scavenger.repository

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.SingleColumnRowMapper
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.support.KeyHolder

open class ExtendedJdbcDaoSupport(entityJdbcProvider: EntityJdbcProvider) : JdbcDaoSupport(entityJdbcProvider) {

    override fun <R : Any?> getRowMapper(returnType: Class<R>?): RowMapper<R> {
        if (returnType?.isPrimitive == true || returnType == String::class.java) {
            return SingleColumnRowMapper()
        } else {
            return super.getRowMapper(returnType)
        }
    }

    @Throws(DataAccessException::class)
    open fun update(sql: String, paramSource: SqlParameterSource): Int {
        return jdbcOperations.update(sql, paramSource)
    }

    @Throws(DataAccessException::class)
    open fun update(sql: String, paramSource: SqlParameterSource, generatedKeyHolder: KeyHolder): Int {
        return jdbcOperations.update(sql, paramSource, generatedKeyHolder)
    }

    open fun batchUpdate(sql: String, batchArgs: Array<SqlParameterSource>): IntArray {
        return jdbcOperations.batchUpdate(sql, batchArgs)
    }
}
