package com.geeboff.cloudjammer.model

data class Product(
    val id: Int,
    val flavor: String,
    val brand_name: String,
    val description: String,
    val nicotine_amount: String,
    val bottle_size: String,
    val categories: String,
    val image: String
    // Include other fields as necessary
)