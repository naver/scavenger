package com.example.demo.extmodel


data class MyExtensionModel(
    val id: Long,
    val name: String
) {
    fun buildModels(): String {
        return "$id-$name"
    }
}
