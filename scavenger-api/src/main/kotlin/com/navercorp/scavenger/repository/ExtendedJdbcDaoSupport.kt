package com.navercorp.scavenger.repository

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.SingleColumnRowMapper

open class ExtendedJdbcDaoSupport(entityJdbcProvider: EntityJdbcProvider) : JdbcDaoSupport(entityJdbcProvider) {

    override fun <R : Any?> getRowMapper(returnType: Class<R>?): RowMapper<R> {
        return if (returnType?.isPrimitive == true || returnType == String::class.java) {
            SingleColumnRowMapper()
        } else {
            super.getRowMapper(returnType)
        }
    }
}
