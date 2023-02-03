package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.SnapshotRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class SnapshotServiceTest {
    @Autowired
    private lateinit var sut: SnapshotService

    @Autowired
    private lateinit var snapshotRepository: SnapshotRepository

    @Test
    @Transactional
    fun deleteSnapshot() {
        sut.deleteSnapshot(1, 1)

        Assertions.assertThat(snapshotRepository.findByCustomerIdAndId(1, 1)).isNotPresent
    }
}
