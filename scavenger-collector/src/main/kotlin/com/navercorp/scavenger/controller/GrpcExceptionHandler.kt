package com.navercorp.scavenger.controller

import com.navercorp.scavenger.exception.LicenseKeyNotFoundException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import mu.KotlinLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionHandler {
    val logger = KotlinLogging.logger {}

    @GrpcExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): StatusRuntimeException {
        return Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e).asRuntimeException()
    }

    @GrpcExceptionHandler(LicenseKeyNotFoundException::class)
    fun handleLicenseKeyNotFoundException(e: LicenseKeyNotFoundException): StatusRuntimeException {
        return Status.UNAUTHENTICATED.withCause(e).asRuntimeException()
    }
}
