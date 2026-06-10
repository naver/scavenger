package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.SnapshotRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
class SnapshotServiceTest {
    @Autowired
    private lateinit var sut: SnapshotService

    @Autowired
    private lateinit var snapshotRepository: SnapshotRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private val txTemplate by lazy { TransactionTemplate(transactionManager) }

    @Test
    @Transactional
    fun deleteSnapshot() {
        sut.deleteSnapshot(1, 1)

        Assertions.assertThat(snapshotRepository.findByCustomerIdAndId(1, 1)).isNotPresent
    }

    @Test
    fun `createSnapshot's saveSnapshotWithLimit commits via REQUIRES_NEW even when outer transaction rolls back`() {
        val name = "aop-redirect-${System.currentTimeMillis()}"
        try {
            txTemplate.execute { status ->
                sut.createSnapshot(
                    customerId = 1,
                    name = name,
                    applicationIdList = listOf(1),
                    environmentIdList = listOf(1),
                    filterInvokedAtMillis = 0,
                    packages = ""
                )
                status.setRollbackOnly()
            }

            val surviving = sut.listSnapshots(1).firstOrNull { it.name == name }
            assertThat(surviving)
                .describedAs("saveSnapshotWithLimit's @Transactional(REQUIRES_NEW) requires proxy(this) to route through the AOP proxy")
                .isNotNull
        } finally {
            sut.listSnapshots(1)
                .filter { it.name == name }
                .forEach { sut.deleteSnapshot(1, requireNotNull(it.id)) }
        }
    }
}
