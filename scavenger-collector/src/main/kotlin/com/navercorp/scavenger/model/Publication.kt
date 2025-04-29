package com.navercorp.scavenger.model

import com.navercorp.scavenger.dto.CodeBaseImportDto
import com.navercorp.scavenger.dto.CommonImportDto
import com.navercorp.scavenger.dto.CommonImportResultDto
import com.navercorp.scavenger.dto.InvocationImportDto
import com.navercorp.scavenger.exception.UnknownPublicationException
import com.navercorp.scavenger.util.HashGenerator.DefaultHash
import io.codekvast.javaagent.model.v4.CodeBasePublication4
import io.codekvast.javaagent.model.v4.CommonPublicationData4
import io.codekvast.javaagent.model.v4.InvocationDataPublication4
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.regex.Pattern

sealed interface Publication {
    fun getCommonImportDto(customerId: Long): CommonImportDto
}

sealed interface CodeBaseImportable {
    fun getCodeBaseImportDto(commonImportResultDto: CommonImportResultDto): CodeBaseImportDto
}

sealed interface InvocationImportable {
    fun getInvocationImportDto(commonImportResultDto: CommonImportResultDto): InvocationImportDto
}

sealed class ProtoPublication private constructor(val commonData: CommonPublicationData) : Publication {
    override fun getCommonImportDto(customerId: Long): CommonImportDto =
        with(commonData) {
            CommonImportDto(
                jvmStartedAtMillis = jvmStartedAtMillis,
                customerId = customerId,
                appName = appName,
                environment = environment,
                appVersion = appVersion,
                jvmUuid = jvmUuid,
                codeBaseFingerprint = codeBaseFingerprint,
                publishedAtMillis = publishedAtMillis,
                hostname = hostname,
            )
        }

    data class CodeBase(val pub: CodeBasePublication) : ProtoPublication(pub.commonData), CodeBaseImportable {
        override fun getCodeBaseImportDto(commonImportResultDto: CommonImportResultDto): CodeBaseImportDto =
            with(commonImportResultDto) {
                val entries = pub.entryList.map {
                    CodeBaseImportDto.CodeBaseEntry(
                        declaringType = it.declaringType,
                        visibility = it.visibility,
                        signature = it.signature,
                        methodName = it.methodName,
                        modifiers = it.modifiers,
                        packageName = it.packageName,
                        parameterTypes = it.parameterTypes,
                        signatureHash = it.signatureHash
                    )
                }.sortedBy { it.signatureHash }

                CodeBaseImportDto(
                    customerId = customerId,
                    applicationId = applicationId,
                    environmentId = environmentId,
                    publishedAtMillis = publishedAtMillis,
                    codeBaseFingerprint = commonData.codeBaseFingerprint,
                    entries = entries
                )
            }
    }

    data class InvocationData(val pub: InvocationDataPublication) : ProtoPublication(pub.commonData), InvocationImportable {
        override fun getInvocationImportDto(commonImportResultDto: CommonImportResultDto): InvocationImportDto =
            with(commonImportResultDto) {
                InvocationImportDto(
                    customerId = customerId,
                    applicationId = applicationId,
                    environmentId = environmentId,
                    invocations = pub.entryList.map { it.hash }.sorted(),
                    invokedAtMillis = pub.recordingIntervalStartedAtMillis
                )
            }
    }

    companion object {
        fun from(pub: CodeBasePublication) = CodeBase(pub)
        fun from(pub: InvocationDataPublication) = InvocationData(pub)
    }
}

sealed class LegacyPublication private constructor(val commonData: CommonPublicationData4) : Publication {
    override fun getCommonImportDto(customerId: Long): CommonImportDto =
        with(commonData) {
            CommonImportDto(
                jvmStartedAtMillis = jvmStartedAtMillis,
                customerId = customerId,
                appName = appName,
                environment = environment,
                appVersion = appVersion,
                jvmUuid = jvmUuid,
                codeBaseFingerprint = codeBaseFingerprint,
                publishedAtMillis = publishedAtMillis,
                hostname = hostname,
            )
        }

    data class CodeBase(val pub: CodeBasePublication4) : LegacyPublication(pub.commonData), CodeBaseImportable {
        override fun getCodeBaseImportDto(commonImportResultDto: CommonImportResultDto): CodeBaseImportDto =
            with(commonImportResultDto) {
                val entries = pub.entries
                    .filterNot { syntheticSignaturePattern.matcher(it.signature).matches() }
                    .map {
                        CodeBaseImportDto.CodeBaseEntry(
                            declaringType = it.methodSignature.declaringType,
                            visibility = it.visibility,
                            signature = it.signature,
                            methodName = it.methodSignature.methodName,
                            modifiers = it.methodSignature.modifiers,
                            packageName = it.methodSignature.packageName,
                            parameterTypes = it.methodSignature.parameterTypes,
                            signatureHash = DefaultHash.from(it.signature)
                        )
                    }.sortedBy { it.signatureHash }

                CodeBaseImportDto(
                    customerId = customerId,
                    applicationId = applicationId,
                    environmentId = environmentId,
                    publishedAtMillis = publishedAtMillis,
                    codeBaseFingerprint = commonData.codeBaseFingerprint,
                    entries = entries
                )
            }
    }

    data class InvocationData(val pub: InvocationDataPublication4) : LegacyPublication(pub.commonData), InvocationImportable {
        override fun getInvocationImportDto(commonImportResultDto: CommonImportResultDto): InvocationImportDto =
            with(commonImportResultDto) {
                InvocationImportDto(
                    customerId = customerId,
                    applicationId = applicationId,
                    environmentId = environmentId,
                    invocations = pub.invocations
                        .filterNot { syntheticSignaturePattern.matcher(it).matches() }
                        .map { DefaultHash.from(it) }
                        .sorted(),
                    invokedAtMillis = pub.recordingIntervalStartedAtMillis,
                )
            }
    }

    companion object {
        val syntheticSignaturePattern: Pattern = Pattern.compile(".*\\.\\.(Enhancer|FastClass)BySpringCGLIB\\.\\..*")
    }
}

fun InputStream.toLegacyPublication(): LegacyPublication {
    ObjectInputStream(this).use {
        return when (val obj = it.readObject()) {
            is CodeBasePublication4 -> LegacyPublication.CodeBase(obj)
            is InvocationDataPublication4 -> LegacyPublication.InvocationData(obj)

            else -> throw UnknownPublicationException()
        }
    }
}
