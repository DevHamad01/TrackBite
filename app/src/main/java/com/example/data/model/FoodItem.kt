package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoodItem(
    val name: String,
    val serving: String,
    val calories: Int,
    val carbsGrams: Int,
    val proteinGrams: Int,
    val fatGrams: Int
)
