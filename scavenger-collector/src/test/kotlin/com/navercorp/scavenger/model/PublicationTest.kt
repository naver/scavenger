package com.navercorp.scavenger.model

import com.navercorp.scavenger.dto.CommonImportResultDto
import com.navercorp.scavenger.model.CodeBasePublication.CodeBaseEntry
import com.navercorp.scavenger.util.HashGenerator.DefaultHash
import com.navercorp.scavenger.util.SamplePublications
import io.codekvast.javaagent.model.v4.CodeBaseEntry4
import io.codekvast.javaagent.model.v4.CodeBasePublication4
import io.codekvast.javaagent.model.v4.CommonPublicationData4
import io.codekvast.javaagent.model.v4.InvocationDataPublication4
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
@DisplayName("Publication class")
class PublicationTest {

    @Nested
    @DisplayName("LegacyPublication class")
    inner class LegacyPublicationTest {
        private val commonImportResultDto = CommonImportResultDto(1, 1, 1, 1, 0)
        private val targetSignature = "TestClass.method()"
        private val cglibSignature = "TestClass..EnhancerBySpringCGLIB..hash.method()"

        @Nested
        @DisplayName("if codebase contains SpringCGLIB generated method")
        inner class Codebase {

            @Test
            @DisplayName("it ignores it")
            fun getCodeBaseImportDto_ignoreCglib() {
                assertThat(
                    LegacyPublication.CodeBase(
                        CodeBasePublication4.builder()
                            .commonData(CommonPublicationData4.sampleCommonPublicationData())
                            .entries(
                                listOf(
                                    CodeBaseEntry4.sampleCodeBaseEntry(),
                                    CodeBaseEntry4.builder()
                                        .signature(cglibSignature)
                                        .visibility("visibility")
                                        .build()
                                )
                            ).build()
                    ).getCodeBaseImportDto(commonImportResultDto).entries
                )
                    .noneMatch { it.signature == cglibSignature }
                    .hasSize(1)
            }

            @Test
            @DisplayName("it should be sorted by signatureHash")
            fun getCodeBaseImportDto_sortedBySignatureHash() {
                assertThat(
                    LegacyPublication.CodeBase(
                        CodeBasePublication4.builder()
                            .commonData(CommonPublicationData4.sampleCommonPublicationData())
                            .entries(
                                listOf(
                                    CodeBaseEntry4.sampleCodeBaseEntry(),
                                    CodeBaseEntry4.builder()
                                        .signature(cglibSignature)
                                        .visibility("visibility")
                                        .build(),
                                    CodeBaseEntry4.sampleCodeBaseEntry().toBuilder()
                                        .signature(targetSignature)
                                        .build()
                                )
                            ).build()
                    ).getCodeBaseImportDto(commonImportResultDto).entries
                ).hasSize(2)
                    .extracting("signatureHash")
                    .isEqualTo(listOf(DefaultHash.from(targetSignature), DefaultHash.from(CodeBaseEntry4.sampleCodeBaseEntry().signature)).sorted())
            }
        }

        @Nested
        @DisplayName("if invocation data contains SpringCGLIB generated method")
        inner class Invocation {
            private val cglibHash = DefaultHash.from(cglibSignature)

            @Test
            @DisplayName("it ignores it")
            fun getInvocationImportDto_ignoreCglib() {
                assertThat(
                    LegacyPublication.InvocationData(
                        InvocationDataPublication4.builder()
                            .commonData(CommonPublicationData4.sampleCommonPublicationData())
                            .invocations(
                                setOf(
                                    "signature()",
                                    cglibSignature
                                )
                            )
                            .build()
                    ).getInvocationImportDto(commonImportResultDto).invocations
                )
                    .noneMatch { it == cglibHash }
                    .hasSize(1)
            }

            @Test
            @DisplayName("it should be sorted by signatureHash")
            fun getInvocationImportDto_sortedBySignatureHash() {
                assertThat(
                    LegacyPublication.InvocationData(
                        InvocationDataPublication4.builder()
                            .commonData(CommonPublicationData4.sampleCommonPublicationData())
                            .invocations(
                                setOf(
                                    "signature()",
                                    cglibSignature,
                                    targetSignature
                                )
                            )
                            .build()
                    ).getInvocationImportDto(commonImportResultDto).invocations
                ).isEqualTo(listOf(DefaultHash.from(targetSignature), DefaultHash.from("signature()")).sorted())
            }
        }
    }

    @Nested
    @DisplayName("ProtoPublication class")
    inner class ProtoPublicationTest {
        private val commonImportResultDto = CommonImportResultDto(1, 1, 1, 1, 0)

        private val codeBaseEntries = listOf(
            CodeBaseEntry.newBuilder()
                .setSignatureHash(DefaultHash.from("TestClass.method()"))
                .build(),
            CodeBaseEntry.newBuilder()
                .setSignatureHash(DefaultHash.from("signature()"))
                .build()
        )

        @Test
        @DisplayName("it should be sorted by signatureHash")
        fun getCodeBaseImportDto_sortedBySignatureHash() {
            assertThat(
                ProtoPublication.CodeBase(
                    CodeBasePublication.newBuilder()
                        .setCommonData(SamplePublications.commonPublicationData)
                        .addAllEntry(codeBaseEntries)
                        .build()
                )
                    .getCodeBaseImportDto(commonImportResultDto).entries
            )
                .extracting("signatureHash")
                .isEqualTo(listOf(DefaultHash.from("TestClass.method()"), DefaultHash.from("signature()")).sorted())
        }

        @Test
        @DisplayName("it should be sorted by signatureHash")
        fun getInvocationImportDto_sortedBySignatureHash() {
            assertThat(
                ProtoPublication.InvocationData(
                    InvocationDataPublication.newBuilder()
                        .setCommonData(SamplePublications.commonPublicationData)
                        .addEntry(
                            InvocationDataPublication.InvocationDataEntry.newBuilder()
                                .setHash(DefaultHash.from("signature()"))
                        )
                        .addEntry(
                            InvocationDataPublication.InvocationDataEntry.newBuilder()
                                .setHash(DefaultHash.from("TestClass.method()"))
                        )
                        .setRecordingIntervalStartedAtMillis(0)
                        .build()
                ).getInvocationImportDto(commonImportResultDto).invocations
            )
                .isEqualTo(listOf(DefaultHash.from("TestClass.method()"), DefaultHash.from("signature()")).sorted())
        }
    }
}
