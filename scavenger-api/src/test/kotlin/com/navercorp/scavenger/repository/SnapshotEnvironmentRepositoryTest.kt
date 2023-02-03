package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.EnvironmentRef
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class SnapshotEnvironmentRepositoryTest {
    @Autowired
    private lateinit var sut: SnapshotEnvironmentDao

    val customerId: Long = 1
    val environmentId: Long = 1
    val snapshotId: Long = 1

    @Test
    fun countByCustomerIdAndApplicationId() {
        assertThat(sut.countByCustomerIdAndEnvironmentId(customerId, environmentId)).isGreaterThan(0)
    }

    @Test
    fun findByCustomerIdAndApplicationId() {
        assertThat(sut.findByCustomerIdAndEnvironmentId(customerId, environmentId)).hasSizeGreaterThan(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndSnapshotId() {
        sut.deleteByCustomerIdAndSnapshotId(customerId, snapshotId)
        assertThat(sut.findByCustomerIdAndSnapshotId(customerId, snapshotId)).hasSize(0)
    }

    @Test
    @Transactional
    fun insertAll() {
        val param = listOf(
            EnvironmentRef(customerId = 0, environmentId = 1, snapshotId = 1),
            EnvironmentRef(customerId = 0, environmentId = 1, snapshotId = 2)
        ).toSet()
        sut.insertAll(param)

        assertThat(sut.findByCustomerIdAndEnvironmentId(0, 1)).hasSize(2)
    }
}
