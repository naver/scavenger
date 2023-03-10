package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CodeBaseImportDto
import com.navercorp.scavenger.dto.InvocationImportDto
import com.navercorp.scavenger.entity.MethodEntity
import com.navercorp.scavenger.repository.InvocationDao
import com.navercorp.scavenger.repository.MethodDao
import io.codekvast.javaagent.model.v4.SignatureStatus4
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
@Nested
@SpringBootTest
@DisplayName("InvocationImportService class")
class InvocationImportServiceTest {
    @Autowired
    lateinit var sut: InvocationImportService

    @Autowired
    lateinit var methodDao: MethodDao

    @Autowired
    lateinit var invocationDao: InvocationDao

    @Autowired
    lateinit var codeBaseImportService: CodeBaseImportService

    private val sample = InvocationImportDto(
        customerId = 1,
        applicationId = 1,
        environmentId = 1,
        invocations = listOf("hash"),
        invokedAtMillis = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli()
    )

    private val codeBaseImportDto = CodeBaseImportDto(
        customerId = sample.customerId,
        applicationId = sample.applicationId,
        environmentId = sample.environmentId,
        publishedAtMillis = sample.invokedAtMillis,
        codeBaseFingerprint = "codeBaseFingerprint",
        entries = sample.invocations.map {
            CodeBaseImportDto.CodeBaseEntry(
                declaringType = "declaringType",
                visibility = "visibility",
                signature = "signature",
                methodName = "methodName",
                modifiers = "modifiers",
                packageName = "packageName",
                parameterTypes = "parameterTypes",
                signatureHash = it,
            )
        }
    )

    private fun getAffectedMethods(): List<MethodEntity> {
        val methods = methodDao.findAllByCustomerIdAndSignatureHashIn(sample.customerId, sample.invocations)
        println(methods)
        return methods
    }

    private fun getAffectedInvocations() =
        invocationDao.findAllByCustomerIdAndSignatureHashIn(sample.customerId, getAffectedMethods().map { it.signatureHash })

    @Nested
    @DisplayName("import method")
    inner class ImportMethod {

        @Nested
        @DisplayName("if method exists and not invoked yet")
        inner class NotInvokedYet {

            @BeforeEach
            fun insertMethods() {
                codeBaseImportService.import(codeBaseImportDto)

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.invokedAtMillis).isEqualTo(0)
                        assertThat(it.status).isEqualTo(SignatureStatus4.NOT_INVOKED.name)
                    }
            }

            @Test
            @DisplayName("it does not insert new method")
            fun import_notInsertNewMethodWhenNotInvokedYet() {
                val previousCount = methodDao.count()

                sut.import(sample)

                assertThat(methodDao.count()).isEqualTo(previousCount)
            }

            @Test
            @DisplayName("it updates invokedAtMillis")
            fun import_updateInvokedAtMillisWhenNotInvokedYet() {
                sut.import(sample)

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.invokedAtMillis).isEqualTo(sample.invokedAtMillis)
                    }
            }

            @Test
            @DisplayName("it flags method as invoked")
            fun import_flagMethodAsInvokedWhenNotInvokedYet() {
                sut.import(sample)

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.status).isEqualTo(SignatureStatus4.INVOKED.name)
                    }
            }
        }

        @Nested
        @DisplayName("if method exists and invoked")
        inner class AlreadyInvoked {

            @BeforeEach
            fun insertMethodsAndInvoke() {
                codeBaseImportService.import(codeBaseImportDto)
                sut.import(sample)

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.status).isEqualTo(SignatureStatus4.INVOKED.name)
                    }
            }

            @Test
            @DisplayName("it does not insert new method")
            fun import_notInsertNewMethodWhenAlreadyInvoked() {
                val previousCount = methodDao.count()

                sut.import(sample)

                assertThat(methodDao.count()).isEqualTo(previousCount)
            }

            @Test
            @DisplayName("it updates to newer invokedAtMillis")
            fun import_updateToNewerInvokedAtMillisWhenAlreadyInvoked() {
                val newInvokedAtMillis = sample.invokedAtMillis + 60000

                sut.import(sample.copy(invokedAtMillis = newInvokedAtMillis))

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.invokedAtMillis).isEqualTo(newInvokedAtMillis)
                    }
            }

            @Test
            @DisplayName("it ignores older invokedAtMillis")
            fun import_ignoreOlderInvokedAtMillisWhenAlreadyInvoked() {
                val newInvokedAtMillis = sample.invokedAtMillis - 10

                sut.import(sample.copy(invokedAtMillis = newInvokedAtMillis))

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.invokedAtMillis).isEqualTo(sample.invokedAtMillis)
                    }
            }

            @Test
            @DisplayName("it keeps method as invoked")
            fun import_keepMethodAsInvokedWhenAlreadyInvoked() {
                sut.import(sample)

                assertThat(getAffectedInvocations())
                    .allSatisfy {
                        assertThat(it.status).isEqualTo(SignatureStatus4.INVOKED.name)
                    }
            }
        }
    }
}
