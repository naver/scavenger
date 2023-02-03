package com.navercorp.scavenger.dto

data class CodeBaseImportDto(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,
    val publishedAtMillis: Long,
    val codeBaseFingerprint: String,

    val entries: List<CodeBaseEntry>
) {
    data class CodeBaseEntry(
        val declaringType: String,
        val visibility: String,
        val signature: String,
        val methodName: String,
        val modifiers: String,
        val packageName: String,
        val parameterTypes: String,
        val signatureHash: String
    )

    override fun toString(): String =
        "CodeBaseImportDto(customerId=$customerId, applicationId=$applicationId, " +
            "environmentId=$environmentId, codeBaseFingerprint=$codeBaseFingerprint)"
}
