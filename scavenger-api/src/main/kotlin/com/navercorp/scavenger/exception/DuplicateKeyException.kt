package com.navercorp.scavenger.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.CONFLICT)
class DuplicateKeyException : RuntimeException()
