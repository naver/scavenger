package com.navercorp.scavenger.controller

import com.navercorp.scavenger.entity.Jvm
import com.navercorp.scavenger.repository.JvmDao
import com.navercorp.scavenger.service.CodeBaseImportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/operation")
@RestController
class OperationController(
    val codeBaseImportService: CodeBaseImportService,
    val jvmDao: JvmDao
) {

    @GetMapping("/reimport-codebases")
    fun reimportCodeBase(fingerprint: String): String {
        try {
            codeBaseImportService.reimport(fingerprint)
            return "OK"
        } catch (e: Exception) {
            return e.message.orEmpty()
        }
    }

    @GetMapping("/jvms")
    fun findJvms(customerId: Long): List<Jvm> {
        return jvmDao.findAllByCustomerId(customerId)
    }
}
