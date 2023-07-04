package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ApplicationRefEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class SnapshotApplicationRepositoryTest {
    @Autowired
    private lateinit var sut: SnapshotApplicationDao

    val customerId: Long = 1
    val applicationId: Long = 1
    val snapshotId: Long = 1

    @Test
    fun countByCustomerIdAndApplicationId() {
        assertThat(sut.countByCustomerIdAndApplicationId(customerId, applicationId)).isGreaterThan(0)
    }

    @Test
    fun findAllByCustomerIdAndApplicationId() {
        assertThat(sut.findAllByCustomerIdAndApplicationId(customerId, applicationId)).hasSizeGreaterThan(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndSnapshotId() {
        sut.deleteByCustomerIdAndSnapshotId(customerId, snapshotId)
        assertThat(sut.findAllByCustomerIdAndSnapshotId(customerId, snapshotId)).hasSize(0)
    }

    @Test
    @Transactional
    fun insertAll() {
        val param = listOf(
            ApplicationRefEntity(customerId = 0, applicationId = 1, snapshotId = 1),
            ApplicationRefEntity(customerId = 0, applicationId = 1, snapshotId = 2)
        ).toSet()
        sut.insertAll(param)

        assertThat(sut.findAllByCustomerIdAndApplicationId(0, 1)).hasSize(2)
    }
}
