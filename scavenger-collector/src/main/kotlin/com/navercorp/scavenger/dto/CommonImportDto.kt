package com.navercorp.scavenger.dto

data class CommonImportDto(
    val jvmStartedAtMillis: Long,
    val customerId: Long,
    val appName: String,
    val appVersion: String,
    val environment: String,
    val jvmUuid: String,
    val codeBaseFingerprint: String,
    val publishedAtMillis: Long,
    val hostname: String,
) {
    override fun toString(): String =
        "CommonImportDto(customerId=$customerId, appName=$appName, appVersion=$appVersion, environment=$environment, " +
            "jvmUuid=$jvmUuid, codeBaseFingerprint=$codeBaseFingerprint, hostname=$hostname, " +
            "jvmStartedAtMillis=$jvmStartedAtMillis, publishedAtMillis=$publishedAtMillis)"
}
