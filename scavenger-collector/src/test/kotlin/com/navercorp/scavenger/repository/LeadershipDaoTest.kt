package com.navercorp.scavenger.repository

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@SpringBootTest
class LeadershipDaoTest {

    @Autowired
    lateinit var sut: LeadershipDao

    @Test
    fun tryAcquireLeadershipTest() {
        sut.forceReelection()
        sut.tryAcquireLeadership("hello", Instant.now(), Instant.now().minusSeconds(1))
        assertThat(sut.getLeader()).isEqualTo("hello")
        sut.tryAcquireLeadership("hello2", Instant.now(), Instant.now().minusSeconds(1))
        assertThat(sut.getLeader()).isEqualTo("hello")
        Thread.sleep(1005)
        sut.tryAcquireLeadership("hello2", Instant.now(), Instant.now().minusSeconds(1))
        assertThat(sut.getLeader()).isEqualTo("hello2")
    }
}
