package com.example.demo.controller

import com.example.demo.extmodel.MyExtensionModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ExtensionController {
    @GetMapping("/run-extension")
    fun runExtension(): String {
        return MyExtensionModel(10, "hello").buildModels()
    }
}
