package com.navercorp.scavenger.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class DisabledCustomerException(msg: String) : RuntimeException(msg)
