package com.navercorp.scavenger.mcp

object McpQueryLimits {
    const val MAX_SIGNATURE_LENGTH = 1000

    // declaringType is VARCHAR(256) — a longer prefix can never match
    const val MAX_PREFIX_LENGTH = 256
    const val MAX_BULK_SIGNATURES = 200
    const val MAX_PAGE_SIZE = 100
    const val DEFAULT_PAGE_SIZE = 50

    // MCP shares the DB pool with the UI — runaway agent queries must fail fast
    const val QUERY_TIMEOUT_SECONDS = 10

    // not-found must never be read as "dead" — the method may simply not be instrumented
    const val METHOD_NOT_FOUND_HINT = "Signatures must match the stored format exactly, e.g. " +
        "com.example.Foo.bar(java.lang.String). A missing method may also be outside the agent's " +
        "instrumented packages — absence is not evidence that the method is dead."

    fun validateSignature(signature: String) {
        if (signature.isBlank()) {
            throw McpException.invalidArgument("signature must not be blank.")
        }
        if (signature.length > MAX_SIGNATURE_LENGTH) {
            throw McpException.invalidArgument(
                "signature length ${signature.length} exceeds the maximum of $MAX_SIGNATURE_LENGTH."
            )
        }
    }
}
