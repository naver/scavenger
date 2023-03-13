package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CommonImportDto
import com.navercorp.scavenger.dto.CommonImportResultDto
import com.navercorp.scavenger.repository.ApplicationDao
import com.navercorp.scavenger.repository.EnvironmentDao
import com.navercorp.scavenger.repository.JvmDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.properties.Delegates

@Transactional
@Nested
@SpringBootTest
@DisplayName("CommonImportService class")
class CommonImportServiceTest {
    @Autowired
    lateinit var sut: CommonImportService

    @Autowired
    lateinit var applicationDao: ApplicationDao

    @Autowired
    lateinit var environmentDao: EnvironmentDao

    @Autowired
    lateinit var jvmDao: JvmDao

    val sample = CommonImportDto(
        customerId = 1,
        appName = "appName",
        environment = "environment",
        appVersion = "appVersion",
        jvmUuid = "jvmUuid",
        codeBaseFingerprint = "codeBaseFingerprint",
        jvmStartedAtMillis = Instant.now().toEpochMilli(),
        publishedAtMillis = Instant.now().toEpochMilli(),
        hostname = "hostname"
    )

    @Nested
    @DisplayName("import method")
    inner class ImportMethod {

        @Nested
        @DisplayName("if new application is imported")
        inner class NewApplication {
            private var previousCount by Delegates.notNull<Long>()
            private lateinit var result: CommonImportResultDto

            @BeforeEach
            fun importSample() {
                previousCount = applicationDao.count()
                result = sut.import(sample)
            }

            @Test
            @DisplayName("it inserts new record")
            fun import_insertNewRecordWhenNewApplication() {
                assertThat(applicationDao.count()).isEqualTo(previousCount + 1)
            }

            @Test
            @DisplayName("it inserts application correctly")
            fun import_insertCorrectlyWhenNewApplication() {
                assertThat(applicationDao.findById(result.applicationId))
                    .hasValueSatisfying {
                        assertThat(it.name).isEqualTo(sample.appName)
                        assertThat(it.customerId).isEqualTo(sample.customerId)
                    }
            }
        }

        @Nested
        @DisplayName("if new environment is imported")
        inner class NewEnvironment {
            private var previousCount by Delegates.notNull<Long>()
            private lateinit var result: CommonImportResultDto

            @BeforeEach
            fun importSample() {
                previousCount = environmentDao.count()
                result = sut.import(sample)
            }

            @Test
            @DisplayName("it inserts new record")
            fun import_insertNewRecordWhenNewEnvironment() {
                assertThat(environmentDao.count()).isEqualTo(previousCount + 1)
            }

            @Test
            @DisplayName("it inserts environment correctly")
            fun import_insertCorrectlyWhenNewEnvironment() {
                assertThat(environmentDao.findById(result.environmentId))
                    .hasValueSatisfying {
                        assertThat(it.name).isEqualTo(sample.environment)
                        assertThat(it.customerId).isEqualTo(sample.customerId)
                    }
            }
        }

        @Nested
        @DisplayName("if new jvm is imported")
        inner class NewJvm {
            private var previousCount by Delegates.notNull<Long>()
            private lateinit var result: CommonImportResultDto

            @BeforeEach
            fun importSample() {
                previousCount = jvmDao.count()
                result = sut.import(sample)
            }

            @Test
            @DisplayName("it inserts new record")
            fun import_insertNewRecordWhenNewJvm() {
                assertThat(jvmDao.count()).isEqualTo(previousCount + 1)
            }

            @Test
            @DisplayName("it inserts jvm correctly")
            fun import_insertCorrectlyWhenNewJvm() {
                assertThat(jvmDao.findById(result.jvmId))
                    .hasValueSatisfying {
                        assertThat(it.uuid).isEqualTo(sample.jvmUuid)
                        assertThat(it.customerId).isEqualTo(sample.customerId)
                        assertThat(it.codeBaseFingerprint).isEqualTo(sample.codeBaseFingerprint)
                    }
            }
        }

        @Nested
        @DisplayName("if existing jvm is imported")
        inner class ExistingJvm {
            var jvmId by Delegates.notNull<Long>()

            @BeforeEach
            fun importSample() {
                jvmId = sut.import(sample).jvmId
            }

            @Test
            @DisplayName("it_does_not_insert_new_jvm")
            fun import_notInsertNewWhenExistingJvm() {
                val previousCount = jvmDao.count()

                sut.import(sample)

                assertThat(jvmDao.count()).isEqualTo(previousCount)
            }

            @Test
            @DisplayName("it updates same record")
            fun import_updateSameRecordWhenExistingJvm() {
                val result = sut.import(sample.copy(codeBaseFingerprint = "new-codeBaseFingerprint"))

                assertThat(result.jvmId).isEqualTo(jvmId)
            }

            @Test
            @DisplayName("it updates codeBaseFingerprint")
            fun import_updateCodeBaseFingerprintWhenExistingJvm() {
                val newCodeBaseFingerprint = "new-codeBaseFingerprint"

                sut.import(sample.copy(codeBaseFingerprint = newCodeBaseFingerprint))

                assertThat(jvmDao.findById(jvmId))
                    .hasValueSatisfying {
                        assertThat(it.codeBaseFingerprint).isEqualTo(newCodeBaseFingerprint)
                    }
            }

            @ParameterizedTest
            @ValueSource(longs = [-10, 10])
            @DisplayName("it updates publishedAt")
            fun import_updatePublishedAtWhenExistingJvm(offset: Long) {
                val newPublishedAt = sample.publishedAtMillis + offset

                sut.import(sample.copy(publishedAtMillis = newPublishedAt))

                assertThat(jvmDao.findById(jvmId))
                    .hasValueSatisfying {
                        assertThat(it.publishedAt.toEpochMilli()).isEqualTo(newPublishedAt)
                    }
            }
        }

        @Nested
        @DisplayName("if existing application is imported")
        inner class ExistingApplication {
            var applicationId by Delegates.notNull<Long>()

            @BeforeEach
            fun importSample() {
                applicationId = sut.import(sample).applicationId
            }

            @Test
            @DisplayName("it does not import new application")
            fun import_notImportNewWhenExistingApplication() {
                val previousCount = applicationDao.count()

                sut.import(sample)

                assertThat(applicationDao.count()).isEqualTo(previousCount)
            }

            @Test
            @DisplayName("it updates to older createdAt")
            fun import_updateToOlderCreatedAtWhenExistingApplication() {
                val newCreatedAt = sample.jvmStartedAtMillis - 10

                sut.import(sample.copy(jvmStartedAtMillis = newCreatedAt))

                assertThat(applicationDao.findById(applicationId))
                    .hasValueSatisfying {
                        assertThat(it.createdAt.toEpochMilli()).isEqualTo(newCreatedAt)
                    }
            }

            @Test
            @DisplayName("it ignores newer createdAt")
            fun import_ignoreNewerCreatedAtWhenExistingApplication() {
                val newCreatedAt = sample.jvmStartedAtMillis + 10

                sut.import(sample.copy(jvmStartedAtMillis = newCreatedAt))

                assertThat(applicationDao.findById(applicationId))
                    .hasValueSatisfying {
                        assertThat(it.createdAt.toEpochMilli()).isEqualTo(sample.jvmStartedAtMillis)
                    }
            }
        }

        @Nested
        @DisplayName("if existing environment is imported")
        inner class ExistingEnvironment {
            var environmentId by Delegates.notNull<Long>()

            @BeforeEach
            fun importSample() {
                environmentId = sut.import(sample).environmentId
            }

            @Test
            @DisplayName("it does not import new environment")
            fun import_notImportNewWhenExistingEnvironment() {
                val previousCount = environmentDao.count()

                sut.import(sample)

                assertThat(environmentDao.count()).isEqualTo(previousCount)
            }

            @Test
            @DisplayName("it updates to older createdAt")
            fun import_updateToOlderCreatedAtWhenExistingEnvironment() {
                val newCreatedAt = sample.jvmStartedAtMillis - 10

                sut.import(sample.copy(jvmStartedAtMillis = newCreatedAt))

                assertThat(environmentDao.findById(environmentId))
                    .hasValueSatisfying {
                        assertThat(it.createdAt.toEpochMilli()).isEqualTo(newCreatedAt)
                    }
            }

            @Test
            @DisplayName("it ignores newer createdAt")
            fun import_ignoreNewerCreatedAtWhenExistingEnvironment() {
                val newCreatedAt = sample.jvmStartedAtMillis + 10

                sut.import(sample.copy(jvmStartedAtMillis = newCreatedAt))

                assertThat(environmentDao.findById(environmentId))
                    .hasValueSatisfying {
                        assertThat(it.createdAt.toEpochMilli()).isEqualTo(sample.jvmStartedAtMillis)
                    }
            }
        }
    }
}
