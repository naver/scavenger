package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CodeBaseImportDto
import com.navercorp.scavenger.entity.CodeBaseFingerprintEntity
import com.navercorp.scavenger.entity.MethodEntity
import com.navercorp.scavenger.repository.CodeBaseFingerprintDao
import com.navercorp.scavenger.repository.InvocationDao
import com.navercorp.scavenger.repository.MethodDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@Nested
@SpringBootTest
@DisplayName("CodeBaseImportService class")
class CodeBaseImportServiceTest {
    @Autowired
    lateinit var sut: CodeBaseImportService

    @SpyBean
    lateinit var methodDao: MethodDao

    @SpyBean
    lateinit var invocationDao: InvocationDao

    @Autowired
    lateinit var codeBaseFingerprintDao: CodeBaseFingerprintDao

    private val codeBaseImportDto = CodeBaseImportDto(
        customerId = 1,
        applicationId = 1,
        environmentId = 1,
        publishedAtMillis = Instant.now().toEpochMilli(),
        codeBaseFingerprint = "codeBaseFingerprint",
        entries = listOf(
            createCodeBaseEntry(1),
            createCodeBaseEntry(2),
            createCodeBaseEntry(3)
        )
    )

    private val customerId = codeBaseImportDto.customerId
    private val applicationId = codeBaseImportDto.applicationId
    private val codeBaseFingerprint = codeBaseImportDto.codeBaseFingerprint
    private val publishedAt = Instant.ofEpochMilli(codeBaseImportDto.publishedAtMillis)
    private val entries = codeBaseImportDto.entries

    private final fun createCodeBaseEntry(number: Int) =
        CodeBaseImportDto.CodeBaseEntry(
            declaringType = "declaringType$number",
            visibility = "visibility$number",
            signature = "signature$number",
            methodName = "methodName$number",
            modifiers = "modifiers$number",
            packageName = "packageName$number",
            parameterTypes = "parameterTypes$number",
            signatureHash = "signatureHash$number",
        )

    @Nested
    @DisplayName("import method")
    inner class ImportMethod {

        @Nested
        @DisplayName("if same codebase is tried to be imported multiple times")
        inner class AlreadyImported {

            @BeforeEach
            fun importCodeBaseThreeTimes() {
                methodDao.deleteAll()

                sut.import(codeBaseImportDto)
                sut.import(codeBaseImportDto)
                sut.import(codeBaseImportDto)
            }

            @Test
            @DisplayName("it imports only once")
            fun import_importOnlyOnceWhenAlreadyImported() {
                verify(methodDao, times(1)).batchUpsert(any())
            }
        }

        @Nested
        @DisplayName("if some invocations are missing for already imported codebase")
        inner class MissingInvocation {

            @BeforeEach
            fun importCodeBaseFingerprintWithoutInvocations() {
                sut.import(codeBaseImportDto)
            }

            @Test
            @DisplayName("it upserts methods")
            fun import_upsertMethodsWhenMissingInvocation() {
                verify(methodDao).batchUpsert(any())
            }

            @Test
            @DisplayName("it reinserts invocations")
            fun import_reinsertInvocationWhenMissingInvocation() {
                verify(invocationDao).batchUpsertLastSeen(any())
            }
        }
    }

    @Nested
    @DisplayName("importCodeBaseFingerprint method")
    inner class ImportCodeBaseFingerprintMethod {

        @Nested
        @DisplayName("if codebase fingerprint already exists")
        inner class AlreadyImported {

            @BeforeEach
            fun importCodeBaseFingerprint() {
                codeBaseFingerprintDao.insert(
                    CodeBaseFingerprintEntity(
                        customerId = customerId,
                        applicationId = applicationId,
                        codeBaseFingerprint = codeBaseFingerprint,
                        publishedAt = publishedAt,
                        createdAt = publishedAt
                    )
                )
            }

            @Test
            @DisplayName("it returns false")
            fun importCodeBaseFingerprint_returnFalseWhenAlreadyImported() {
                assertThat(
                    sut.importCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        publishedAt,
                        codeBaseFingerprint
                    )
                ).isFalse
            }

            @ParameterizedTest
            @ValueSource(longs = [-10, 10])
            @DisplayName("it updates publishedAt")
            fun importCodeBaseFingerprint_updatePublishedAtWhenAlreadyImported(offset: Long) {
                val newPublishedAt = publishedAt.plusMillis(offset)

                assertThat(
                    sut.importCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        newPublishedAt,
                        codeBaseFingerprint
                    )
                ).isFalse

                assertThat(
                    codeBaseFingerprintDao.findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        codeBaseFingerprint
                    )
                ).satisfies {
                    assertThat(it?.publishedAt).isEqualTo(newPublishedAt)
                }
            }
        }

        @Nested
        @DisplayName("if codebase fingerprint is new")
        inner class NewFingerprint {

            @BeforeEach
            fun ensureCodeBaseFingerprintIsNew() {
                assertThat(
                    codeBaseFingerprintDao.findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        codeBaseFingerprint
                    )
                ).isNull()
            }

            @Test
            @DisplayName("it returns true")
            fun importCodeBaseFingerprint_returnTrueWhenNewFingerprint() {
                assertThat(
                    sut.importCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        publishedAt,
                        codeBaseFingerprint
                    )
                ).isTrue
            }

            @Test
            @DisplayName("it inserts codebase fingerprint")
            fun importCodeBaseFingerprint_insertWhenNewFingerprint() {
                assertThat(
                    sut.importCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        publishedAt,
                        codeBaseFingerprint
                    )
                ).isTrue

                assertThat(
                    codeBaseFingerprintDao.findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
                        customerId,
                        applicationId,
                        codeBaseFingerprint
                    )
                ).isNotNull
            }
        }
    }

    @Nested
    @DisplayName("upsertMethods method")
    inner class UpsertMethodsMethod {

        fun getInsertedMethods() =
            entries
                .mapNotNull { methodDao.findByCustomerIdAndSignatureHash(customerId, it.signatureHash) }

        @Nested
        @DisplayName("if method with same hash already exists")
        inner class AlreadyImported {
            private val lastSeenAtMillis = Instant.now().toEpochMilli()

            @BeforeEach
            fun insertEntries() {
                sut.upsertMethods(customerId, publishedAt, lastSeenAtMillis, entries)

                assertThat(getInsertedMethods()).hasSize(entries.size)
            }

            @Test
            @DisplayName("it updates to older createdAt")
            fun upsertMethods_updateToOlderCreatedAtWhenAlreadyImported() {
                val newCreatedAt = publishedAt.minusMillis(10)

                sut.upsertMethods(customerId, newCreatedAt, lastSeenAtMillis, entries)

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.createdAt).isEqualTo(newCreatedAt)
                    }
            }

            @Test
            @DisplayName("it ignores newer createdAt")
            fun upsertMethods_ignoreNewerCreatedAtWhenAlreadyImported() {
                val newCreatedAt = publishedAt.plusMillis(10)

                sut.upsertMethods(customerId, newCreatedAt, lastSeenAtMillis, entries)

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.createdAt).isEqualTo(publishedAt)
                    }
            }

            @Test
            @DisplayName("it updates to newer lastSeenAtMillis")
            fun upsertMethods_updateToNewerLastSeenAtMillisWhenAlreadyImported() {
                val newLastSeenAtMillis = lastSeenAtMillis + 10

                sut.upsertMethods(customerId, publishedAt, newLastSeenAtMillis, entries)

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.lastSeenAtMillis).isEqualTo(newLastSeenAtMillis)
                    }
            }

            @Test
            @DisplayName("it ignores older lastSeenAtMillis")
            fun upsertMethods_ignoreOlderLastSeenAtMillisWhenAlreadyImported() {
                val newLastSeenAtMillis = lastSeenAtMillis - 10

                sut.upsertMethods(customerId, publishedAt, newLastSeenAtMillis, entries)

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.lastSeenAtMillis).isEqualTo(lastSeenAtMillis)
                    }
            }

            @Test
            @DisplayName("it updates fields")
            fun upsertMethods_updateFieldWhenAlreadyImported() {

                sut.upsertMethods(
                    customerId,
                    publishedAt,
                    lastSeenAtMillis + 10,
                    entries
                )

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.lastSeenAtMillis).isEqualTo(lastSeenAtMillis + 10)
                    }

                sut.upsertMethods(
                    customerId,
                    publishedAt,
                    lastSeenAtMillis + 10,
                    entries
                )

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.lastSeenAtMillis).isEqualTo(lastSeenAtMillis + 10)
                    }
            }
        }

        @Nested
        @DisplayName("if incomplete method exists")
        inner class IncompleteMethod {

            @BeforeEach
            fun insertIncompleteEntries() {
                entries
                    .map {
                        MethodEntity(
                            customerId = customerId,
                            createdAt = publishedAt,
                            garbage = false,
                            signatureHash = it.signatureHash
                        )
                    }
                    .forEach(methodDao::insert)

                assertThat(getInsertedMethods())
                    .hasSize(entries.size)
                    .allSatisfy {
                        assertThat(it.createdAt).isEqualTo(publishedAt)
                        assertThat(it.lastSeenAtMillis).isNull()
                        assertThat(it.signature).isNull()
                        assertThat(it.visibility).isNull()
                    }
            }

            @Test
            @DisplayName("it fills in empty fields of incomplete method")
            fun upsertMethods_fillInEmptyFieldWhenIncompleteMethod() {
                sut.upsertMethods(
                    customerId,
                    publishedAt,
                    Instant.now().toEpochMilli(),
                    entries
                )

                val inserted = getInsertedMethods()
                assertThat(inserted)
                    .allSatisfy {
                        assertThat(it.createdAt).isEqualTo(publishedAt)
                        assertThat(it.lastSeenAtMillis).isNotNull
                        assertThat(it.signature).isNotNull
                        assertThat(it.visibility).isNotNull
                    }
            }

            @Test
            @DisplayName("it updates null lastSeenAtMillis with new value")
            fun upsertMethods_updateNullLastSeenAtMillisWhenIncompleteMethod() {
                val lastSeenAtMillis = Instant.now().toEpochMilli()

                sut.upsertMethods(customerId, publishedAt, lastSeenAtMillis, entries)

                assertThat(getInsertedMethods())
                    .allSatisfy {
                        assertThat(it.lastSeenAtMillis).isEqualTo(lastSeenAtMillis)
                    }
            }
        }

        @Nested
        @DisplayName("if method that has same signature and different hash is imported")
        inner class SameSignatureDifferentHash {

            @BeforeEach
            fun insertMethods() {
                sut.upsertMethods(
                    customerId,
                    publishedAt,
                    Instant.now().toEpochMilli(),
                    entries
                )
            }

            @Test
            @DisplayName("it creates new record")
            fun upsertMethods_createNewRecordWhenSameSignatureDifferentHash() {
                val previousCount = methodDao.count()
                val sameSignatureMethodEntries = entries.map { it.copy(signatureHash = "new-${it.signatureHash}") }

                sut.upsertMethods(
                    customerId,
                    publishedAt,
                    Instant.now().toEpochMilli(),
                    sameSignatureMethodEntries
                )

                assertThat(methodDao.count()).isEqualTo(previousCount + sameSignatureMethodEntries.size)
            }
        }
    }

    @Nested
    @DisplayName("ensureInitialInvocations method")
    inner class EnsureInitialInvocationsMethod {

        @Nested
        @DisplayName("if method without invocation exists")
        inner class MethodWithoutInvocation {

            @BeforeEach
            fun insertNewMethodWithoutInvocation() {
                methodDao.insertAll(
                    entries.map {
                        MethodEntity(customerId = customerId, garbage = false, signatureHash = it.signatureHash)
                    }.toMutableList()
                )
            }

            @Test
            @DisplayName("it makes new invocation")
            fun ensureInitialInvocations_makeNewWhenMethodWithoutInvocation() {
                val previousCount = invocationDao.count()
                sut.ensureInitialInvocations(codeBaseImportDto, Instant.now().toEpochMilli())
                assertThat(invocationDao.count()).isEqualTo(previousCount + entries.size)
            }
        }
    }
}
