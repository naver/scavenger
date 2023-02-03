package com.navercorp.scavenger.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class LayoutController {

    @RequestMapping(value = ["", "/", "/customers"])
    fun customer(): String {
        return "customer"
    }

    @GetMapping(value = ["/customers/{customer}", "/customers/{customer}/snapshots/**", "/customers/{customer}/manage"])
    fun basicLayout(@PathVariable customer: String): String {
        return "index"
    }
}
