package com.navercorp.scavenger.service

import com.navercorp.scavenger.leader.EventBroadcaster
import com.navercorp.scavenger.leader.EventListener
import com.navercorp.scavenger.leader.LeadershipContext
import com.navercorp.scavenger.leader.SimpleEventBroadcaster
import com.navercorp.scavenger.repository.LeadershipDao
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Instant

@Service
class LeadershipService(
    val leadershipDao: LeadershipDao
) {
    private val broadcaster: EventBroadcaster<LeadershipContext> = SimpleEventBroadcaster()
    private val hostName = InetAddress.getLocalHost().hostName

    @Scheduled(fixedDelay = 5000, initialDelay = 60 * 1000)
    fun run() {
        val now = Instant.now()
        leadershipDao.tryAcquireLeadership(hostName, now, now.minusSeconds(60))
        val currentLeader = leadershipDao.getLeader()
        broadcaster.broadcast(
            LeadershipContext(
                currentLeader == hostName,
                currentLeader
            )
        )
    }

    fun addListener(listener: EventListener<LeadershipContext>) {
        broadcaster.addListener(listener)
    }
}
