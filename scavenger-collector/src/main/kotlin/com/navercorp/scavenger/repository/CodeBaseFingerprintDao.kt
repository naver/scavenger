package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CodeBaseFingerprint
import com.navercorp.scavenger.repository.sql.CodeBaseFingerPrintSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class CodeBaseFingerprintDao(
    entityJdbcProvider: EntityJdbcProvider,
    codeBaseFingerprintRepository: CodeBaseFingerprintRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    CodeBaseFingerprintRepository by codeBaseFingerprintRepository {
    private val sql = super.sqls(::CodeBaseFingerPrintSql)

    fun updatePublishedAt(codeBaseFingerprint: CodeBaseFingerprint) {
        update(
            sql.updatePublishedAt(),
            beanParameterSource(codeBaseFingerprint)
        )
    }

    fun deleteAllByCustomerIdAndCodeBaseFingerprintIn(customerId: Long, codeBaseFingerprints: Set<String>): Int {
        if (codeBaseFingerprints.isEmpty()) {
            return 0
        }
        return update(
            sql.deleteAllByCustomerIdAndCodeBaseFingerprintIn(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("codeBaseFingerprints", codeBaseFingerprints)
        )
    }
}
