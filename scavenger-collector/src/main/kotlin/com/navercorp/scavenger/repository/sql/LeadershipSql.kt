package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class LeadershipSql : SqlGeneratorSupport() {
    fun tryAcquireLeadership(): String =
        """
            INSERT INTO
            leadership
            (   
                anchor,
                memberId,
                lastSeenActive
            )
            VALUES
            (
                1,
                :memberId,
                :lastSeenActive
            )
            ON DUPLICATE KEY UPDATE
                memberId = CASE WHEN lastSeenActive < :lastSeenActiveWithMargin THEN VALUES(memberId) ELSE memberId END,
                lastSeenActive = CASE WHEN memberId = VALUES(memberId) THEN VALUES(lastSeenActive) ELSE lastSeenActive END
        """

    fun forceReelection() =
        """
           DELETE FROM leadership
        """

    fun selectLeader() =
        """
            SELECT 
                MAX(memberId) AS leader 
            FROM 
                leadership 
            WHERE 
                anchor = 1
        """
}
