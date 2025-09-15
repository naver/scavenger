package com.navercorp.scavenger.entity

data class CallStackSignatureDbRow(
    val calleeSignature: String,
    val callerSignature: String
)
