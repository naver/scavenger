package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.exception.LicenseKeyNotFoundException
import com.navercorp.scavenger.repository.CustomerDao
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class LicenseService(
    val customerDao: CustomerDao
) {
    @Cacheable(LICENSE_CACHE)
    fun check(licenseKey: String): CustomerEntity {
        return customerDao.findByLicenseKey(licenseKey) ?: run {
            throw LicenseKeyNotFoundException()
        }
    }

    companion object {
        const val LICENSE_CACHE = "license"
    }
}
