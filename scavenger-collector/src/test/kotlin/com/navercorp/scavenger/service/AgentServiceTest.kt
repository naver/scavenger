package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.exception.LicenseKeyMismatchException
import com.navercorp.scavenger.exception.LicenseKeyNotFoundException
import com.navercorp.scavenger.exception.UnknownPublicationException
import com.navercorp.scavenger.util.SamplePublications
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectOutputStream

@Transactional
@Nested
@SpringBootTest
@DisplayName("AgentService class")
class AgentServiceTest {
    @Autowired
    lateinit var sut: AgentService

    @MockBean
    lateinit var licenseService: LicenseService

    @MockBean
    lateinit var publicationImportService: PublicationImportService

    @MockBean
    lateinit var agentStateService: AgentStateService

    @MockBean
    lateinit var operationService: OperationService

    fun createCustomer(customerId: Long = 42, licenseKey: String = "random-apiKey") = CustomerEntity(
        id = customerId,
        name = "test",
        licenseKey = licenseKey,
        groupId = "0"
    )

    @Nested
    @DisplayName("saveLegacyPublication method")
    inner class SaveLegacyPublicationMethod {
        private val pub = SamplePublications.legacyInvocationPublication

        fun Any.toInputStream(): InputStream {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use {
                it.writeObject(this)
            }

            return ByteArrayInputStream(baos.toByteArray())
        }

        @Nested
        @DisplayName("if apiKey does not exist")
        inner class ApiKeyNotExist {
            private lateinit var thrown: Throwable

            private val apiKey = "random-apiKey"

            @BeforeEach
            fun saveLegacyPublicationWithNonExistentKey() {
                doThrow(LicenseKeyNotFoundException())
                    .whenever(licenseService)
                    .check(apiKey)

                thrown = catchThrowable {
                    sut.saveLegacyPublication(apiKey, InputStream.nullInputStream())
                }
            }

            @Test
            @DisplayName("it throws LicenseKeyNotFoundException")
            fun saveLegacyPublication_throwExceptionWhenApiKeyNotExist() {
                assertThat(thrown).isInstanceOf(LicenseKeyNotFoundException::class.java)
            }

            @Test
            @DisplayName("it does not import publication")
            fun saveLegacyPublication_notImportPublicationWhenApiKeyNotExist() {
                verifyZeroInteractions(publicationImportService)
            }
        }

        @Nested
        @DisplayName("if apiKey is different from publication customerId")
        inner class DifferentCustomerId {
            private lateinit var thrown: Throwable

            private val customer = createCustomer(pub.commonData.customerId + 1)

            @BeforeEach
            fun saveLegacyPublicationWithCustomer() {
                doReturn(customer)
                    .whenever(licenseService)
                    .check(customer.licenseKey)

                thrown = catchThrowable {
                    sut.saveLegacyPublication(customer.licenseKey, pub.pub.toInputStream())
                }
            }

            @Test
            @DisplayName("it throws LicenseKeyMisMatchException")
            fun saveLegacyPublication_throwExceptionWhenDifferentCustomerId() {
                assertThat(thrown).isInstanceOf(LicenseKeyMismatchException::class.java)
            }

            @Test
            @DisplayName("it does not import publication")
            fun saveLegacyPublication_notImportWhenDifferentCustomerId() {
                verifyZeroInteractions(publicationImportService)
            }
        }

        @Nested
        @DisplayName("if publication is not supported")
        inner class InvalidModel {
            private val notSupportedInputStream = (object : java.io.Serializable {}).toInputStream()
            private val customer = createCustomer(pub.commonData.customerId)

            @BeforeEach
            fun saveLegacyPublicationWithCustomer() {
                doReturn(customer)
                    .whenever(licenseService)
                    .check(customer.licenseKey)
                doReturn(false)
                    .whenever(operationService)
                    .isDisabledCustomerId(anyLong())
            }

            @Test
            @DisplayName("it throws UnknownPublicationException")
            fun saveLegacyPublication_throwExceptionWhenInvalidModel() {

                assertThrows<UnknownPublicationException> {
                    sut.saveLegacyPublication("random-apiKey", notSupportedInputStream)
                }
            }
        }

        @Nested
        @DisplayName("if imported successfully")
        inner class Successful {
            private val customer = createCustomer(pub.commonData.customerId)

            @BeforeEach
            fun saveLegacyPublicationWithCustomer() {
                doReturn(customer)
                    .whenever(licenseService)
                    .check(customer.licenseKey)

                sut.saveLegacyPublication(customer.licenseKey, pub.pub.toInputStream())
            }

            @Test
            @DisplayName("it runs publication import with customerId")
            fun saveLegacyPublication_invokePublicationImportWhenSuccessful() {
                verify(publicationImportService).import(customer.id, pub)
            }
        }
    }

    @Nested
    @DisplayName("savePublication method")
    inner class SavePublicationMethod {
        private val pub = SamplePublications.protoInvocationPublication

        @Nested
        @DisplayName("if apiKey does not exist")
        inner class ApiKeyNotExist {
            private lateinit var thrown: Throwable

            @BeforeEach
            fun savePublicationWithNonExistentKey() {
                doThrow(LicenseKeyNotFoundException())
                    .whenever(licenseService)
                    .check(pub.commonData.apiKey)

                thrown = catchThrowable {
                    sut.savePublication(pub)
                }
            }

            @Test
            @DisplayName("it throws LicenseKeyNotFoundException")
            fun savePublication_throwExceptionWhenApiKeyNotExist() {
                assertThat(thrown).isInstanceOf(LicenseKeyNotFoundException::class.java)
            }

            @Test
            @DisplayName("it does not import publication")
            fun savePublication_notImportPublicationWhenApiKeyNotExist() {
                verifyZeroInteractions(publicationImportService)
            }
        }

        @Nested
        @DisplayName("if imported successfully")
        inner class Successful {
            private val customer = createCustomer(licenseKey = pub.commonData.apiKey)

            @BeforeEach
            fun savePublicationWithCustomer() {
                doReturn(customer)
                    .whenever(licenseService)
                    .check(customer.licenseKey)

                sut.savePublication(pub)
            }

            @Test
            @DisplayName("it runs publication import with customerId")
            fun savePublication_invokePublicationImportWhenSuccessful() {
                verify(publicationImportService).import(customer.id, pub)
            }
        }
    }

    @Nested
    @DisplayName("getConfig method")
    inner class GetConfigMethod {

        @Nested
        @DisplayName("if apiKey does not exist")
        inner class ApiKeyNotExist {
            private lateinit var thrown: Throwable

            @BeforeEach
            fun getConfigWithNonExistentApiKey() {
                val apiKey = "random-apiKey"

                doThrow(LicenseKeyNotFoundException())
                    .whenever(licenseService)
                    .check(apiKey)

                thrown = catchThrowable {
                    sut.getConfig(apiKey, "random-jvmUuid")
                }
            }

            @Test
            @DisplayName("it throws LicenseKeyNotFoundException")
            fun getConfig_throwExceptionWhenApiKeyNotExist() {
                assertThat(thrown).isInstanceOf(LicenseKeyNotFoundException::class.java)
            }

            @Test
            @DisplayName("it does not update agent state")
            fun getConfig_notUpdateAgentStateWhenApiKeyNotExist() {
                verifyZeroInteractions(agentStateService)
            }
        }

        @Nested
        @DisplayName("if succeed")
        inner class Successful {
            private val customer = createCustomer()
            private val jvmUuid = "random-jvmUuid"

            @BeforeEach
            fun getConfigWithCustomer() {
                doReturn(customer)
                    .whenever(licenseService)
                    .check(customer.licenseKey)

                sut.getConfig(customer.licenseKey, jvmUuid)
            }

            @Test
            @DisplayName("it updates agent state")
            fun getConfig_updateAgentStateWhenSuccessful() {
                verify(agentStateService).update(eq(customer.id), eq(jvmUuid), anyInt())
            }
        }
    }
}
