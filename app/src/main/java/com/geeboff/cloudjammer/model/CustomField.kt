package com.geeboff.cloudjammer.model

data class CustomField(
    val name: String = "",
    val type: String = "",
    val options: List<String>? = null,
)