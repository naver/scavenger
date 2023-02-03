package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.Customer
import com.navercorp.scavenger.exception.LicenseKeyNotFoundException
import com.navercorp.scavenger.repository.CustomerDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.transaction.annotation.Transactional

@Transactional
@Nested
@SpringBootTest
@DisplayName("LicenseService class")
class LicenseServiceTest {
    @Autowired
    lateinit var sut: LicenseService

    @Autowired
    lateinit var customerDao: CustomerDao

    @Autowired
    lateinit var cacheManager: CacheManager

    @Nested
    @DisplayName("check method")
    inner class CheckMethod {

        @Nested
        @DisplayName("if key is not found")
        inner class UnknownKey {

            @Test
            @DisplayName("it throws LicenseKeyNotFoundException")
            fun check_throwExceptionWhenUnknownKey() {
                assertThrows<LicenseKeyNotFoundException> {
                    sut.check("unknown-licenseKey")
                }
            }
        }

        @Nested
        @DisplayName("if key is found")
        inner class KnownKey {
            private val licenseKey = "asdfasdf"
            private lateinit var customer: Customer

            @BeforeEach
            fun insertCustomerWithKey() {
                customer = customerDao.insert(
                    Customer(name = "test", licenseKey = licenseKey)
                )
            }

            @Test
            @DisplayName("it returns customer")
            fun check_returnCustomerWhenKnownKey() {
                assertThat(sut.check(licenseKey)).isEqualTo(customer)
            }
        }

        @Nested
        @DisplayName("if key is cached")
        inner class CachedKey {
            private lateinit var customer: Customer

            @BeforeEach
            fun checkLicenseKeyAndDelete() {
                customer = customerDao.insert(
                    Customer(name = "test", licenseKey = "licenseKey")
                )
                sut.check(customer.licenseKey)
                customerDao.delete(customer)
            }

            @Test
            @DisplayName("it returns cached value")
            fun check_returnCachedValueWhenCachedKey() {
                assertThat(sut.check(customer.licenseKey))
                    .isEqualTo(customer)
            }

            @Test
            @DisplayName("it throws LicenseKeyNotFoundException after eviction")
            fun check_throwExceptionAfterEvictionWhenCachedKey() {
                cacheManager.getCache("license")!!.evict(customer.licenseKey)
                assertThrows<LicenseKeyNotFoundException> {
                    sut.check(customer.licenseKey)
                }
            }
        }
    }
}
