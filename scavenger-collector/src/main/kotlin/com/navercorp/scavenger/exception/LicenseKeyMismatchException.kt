package com.navercorp.scavenger.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class LicenseKeyMismatchException(s: String) : RuntimeException(s)
