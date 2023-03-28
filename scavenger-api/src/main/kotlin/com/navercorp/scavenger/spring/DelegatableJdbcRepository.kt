package com.navercorp.scavenger.spring

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.Optional

interface DelegatableJdbcRepository<T : Any, ID : Any> : JdbcRepository<T, ID> {
    override fun count(): Long

    override fun delete(entity: T)

    override fun findAll(): MutableIterable<T>

    override fun findById(id: ID): Optional<T>

    override fun findAll(pageable: Pageable): Page<T>

    override fun findAll(sort: Sort): MutableIterable<T>

    override fun findAllById(ids: MutableIterable<ID>): MutableIterable<T>

    override fun deleteAll()

    override fun deleteAll(entities: MutableIterable<T>)

    override fun deleteAllById(ids: MutableIterable<ID>)

    override fun deleteById(id: ID)

    override fun existsById(id: ID): Boolean

    override fun <S : T> insert(entity: S): S

    override fun <S : T> insertAll(entities: MutableIterable<S>?): MutableIterable<S>

    override fun <S : T> save(entity: S): S

    override fun <S : T> saveAll(entities: MutableIterable<S>): MutableIterable<S>

    override fun <S : T> update(entity: S): S

    override fun <S : T> updateAll(entities: MutableIterable<S>?): MutableIterable<S>
}
