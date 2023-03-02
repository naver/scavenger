package com.navercorp.scavenger.service

import com.navercorp.scavenger.util.SamplePublications
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.transaction.annotation.Transactional
import kotlin.properties.Delegates

@Transactional
@Nested
@SpringBootTest
@DisplayName("PublicationImportService class")
class PublicationImportServiceTest {
    @Autowired
    lateinit var sut: PublicationImportService

    @SpyBean
    lateinit var commonImportService: CommonImportService

    @SpyBean
    lateinit var codeBaseImportService: CodeBaseImportService

    @SpyBean
    lateinit var invocationImportService: InvocationImportService

    @Nested
    @DisplayName("import method")
    inner class ImportMethod {

        @Nested
        @DisplayName("if proto codebase publication is given")
        inner class ProtoCodebase {
            private val pub = SamplePublications.protoCodeBasePublication
            private var returnValue by Delegates.notNull<Boolean>()

            @BeforeEach
            fun import() {
                returnValue = sut.import(1, pub).join()
            }

            @Test
            @DisplayName("it invokes common import")
            fun import_invokeCommonImportWhenProtoCodebase() {
                verify(commonImportService).import(any())
            }

            @Test
            @DisplayName("it invokes invocation import")
            fun import_invokeInvocationImportWhenProtoCodebase() {
                verify(codeBaseImportService).import(any())
            }

            @Test
            @DisplayName("it returns true")
            fun import_returnTrueWhenProtoCodebase() {
                assertThat(returnValue).isTrue
            }
        }

        @Nested
        @DisplayName("if legacy codebase publication is given")
        inner class LegacyCodebase {
            private val pub = SamplePublications.legacyCodeBasePublication
            private var returnValue by Delegates.notNull<Boolean>()

            @BeforeEach
            fun import() {
                returnValue = sut.import(1, pub).join()
            }

            @Test
            @DisplayName("it invokes common import")
            fun import_invokeCommonImportWhenLegacyCodebase() {
                verify(commonImportService).import(any())
            }

            @Test
            @DisplayName("it invokes invocation import")
            fun import_invokeInvocationImportWhenLegacyCodebase() {
                verify(codeBaseImportService).import(any())
            }

            @Test
            @DisplayName("it returns true")
            fun import_returnTrueWhenLegacyCodebase() {
                assertThat(returnValue).isTrue
            }
        }

        @Nested
        @DisplayName("if proto invocation publication is given")
        inner class ProtoInvocation {
            private val pub = SamplePublications.protoInvocationPublication
            private var returnValue by Delegates.notNull<Boolean>()

            @BeforeEach
            fun import() {
                returnValue = sut.import(1, pub).join()
            }

            @Test
            @DisplayName("it invokes common import")
            fun import_invokeCommonImportWhenProtoInvocation() {
                verify(commonImportService).import(any())
            }

            @Test
            @DisplayName("it invokes invocation import")
            fun import_invokeInvocationImportWhenProtoInvocation() {
                verify(invocationImportService).import(any())
            }

            @Test
            @DisplayName("it returns true")
            fun import_returnTrueWhenProtoInvocation() {
                assertThat(returnValue).isTrue
            }
        }

        @Nested
        @DisplayName("if legacy invocation publication is given")
        inner class LegacyInvocation {
            private val pub = SamplePublications.legacyInvocationPublication
            private var returnValue by Delegates.notNull<Boolean>()

            @BeforeEach
            fun import() {
                returnValue = sut.import(1, pub).join()
            }

            @Test
            @DisplayName("it invokes common import")
            fun import_invokeCommonImportWhenLegacyInvocation() {
                verify(commonImportService).import(any())
            }

            @Test
            @DisplayName("it invokes invocation import")
            fun import_invokeInvocationImportWhenLegacyInvocation() {
                verify(invocationImportService).import(any())
            }

            @Test
            @DisplayName("it returns true")
            fun import_returnTrueWhenLegacyInvocation() {
                assertThat(returnValue).isTrue
            }
        }

        @Nested
        @DisplayName("if import fails")
        inner class ImportFail {
            private val pub = SamplePublications.protoCodeBasePublication
            private lateinit var throwable: Throwable

            @BeforeEach
            fun import() {
                doThrow(RuntimeException())
                    .whenever(commonImportService)
                    .import(any())

                throwable = catchThrowable {
                    sut.import(1, pub).join()
                }
            }

            @Test
            @DisplayName("it rethrows exception")
            fun import_rethrowExceptionWhenImportFail() {
                assertThat(throwable).isInstanceOf(Exception::class.java)
            }
        }
    }
}
