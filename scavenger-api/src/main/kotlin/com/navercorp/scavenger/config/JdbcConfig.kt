package com.navercorp.scavenger.config

import com.navercorp.scavenger.util.SqlRayQueryLogEntryCreator
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.NoneFallbackParameterSource
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.jdbc.core.convert.JdbcConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.relational.core.mapping.NamingStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty
import javax.sql.DataSource

@Configuration
class JdbcConfig : AbstractJdbcConfiguration() {
    @Bean
    fun namingStrategy() = object : NamingStrategy {
        override fun getColumnName(property: RelationalPersistentProperty): String = property.name
    }

    @Bean
    fun sqlParameterSourceFactory(
        mappingContext: RelationalMappingContext,
        jdbcConverter: JdbcConverter
    ): SqlParameterSourceFactory {
        return EntityConvertibleSqlParameterSourceFactory(
            ConvertibleParameterSourceFactory(DefaultJdbcParameterSourceConverter(emptyList()), NoneFallbackParameterSource()),
            mappingContext,
            jdbcConverter
        )
    }

    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE)
    class DataSourceBeanPostProcessor : BeanPostProcessor {
        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
            return if (bean is DataSource) {
                val queryLoggingListener = SLF4JQueryLoggingListener().apply {
                    logLevel = SLF4JLogLevel.DEBUG
                    queryLogEntryCreator = SqlRayQueryLogEntryCreator()
                }

                ProxyDataSourceBuilder.create(bean)
                    .listener(queryLoggingListener)
                    .countQuery()
                    .build()
            } else {
                bean
            }
        }
    }

    override fun jdbcCustomConversions() = JdbcCustomConversions(
        listOf(IntToBooleanConverter())
    )

    @ReadingConverter
    class IntToBooleanConverter : Converter<Int, Boolean> {
        override fun convert(source: Int) = source == 1
    }
}
