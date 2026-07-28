package com.example.data.model.ai

import com.example.data.model.FoodItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object SmartNutritionParser {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    data class ParsedMealResult(
        val items: List<FoodItem>,
        val totalCalories: Int,
        val totalCarbs: Int,
        val totalProtein: Int,
        val totalFat: Int
    )

    fun parsePrompt(prompt: String): ParsedMealResult {
        val normalized = prompt.lowercase().trim()
        val items = mutableListOf<FoodItem>()

        // Split prompt by line breaks (\n), commas, and common conjunctions like "with", "and", "+", "plus"
        val rawSegments = prompt.split(Regex("(\r?\n|,|\\s+and\\s+|\\s+with\\s+|\\s*\\+\\s*)"))

        for (seg in rawSegments) {
            val segment = seg.trim()
            if (segment.isEmpty()) continue

            val item = estimateItemFromSegment(segment)
            items.add(item)
        }

        if (items.isEmpty()) {
            items.add(FoodItem(
                name = prompt.ifBlank { "Logged Meal" },
                serving = "1 serving",
                calories = 250,
                carbsGrams = 30,
                proteinGrams = 10,
                fatGrams = 8
            ))
        }

        val totalCals = items.sumOf { it.calories }
        val totalCarbs = items.sumOf { it.carbsGrams }
        val totalProtein = items.sumOf { it.proteinGrams }
        val totalFat = items.sumOf { it.fatGrams }

        return ParsedMealResult(
            items = items,
            totalCalories = totalCals,
            totalCarbs = totalCarbs,
            totalProtein = totalProtein,
            totalFat = totalFat
        )
    }

    private fun estimateItemFromSegment(segment: String): FoodItem {
        val qtyMatch = Regex("^(\\d+)\\s*").find(segment)
        val qty = qtyMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

        val text = segment.replace(Regex("^\\d+\\s*"), "").trim().lowercase()

        return when {
            text.contains("roti") || text.contains("chapati") || text.contains("phulka") -> {
                FoodItem("Roti", "$qty serving", 150 * qty, 20 * qty, 5 * qty, 4 * qty)
            }
            text.contains("paratha") || text.contains("parathe") -> {
                if (text.contains("chicken")) {
                    FoodItem("Chicken Paratha", "$qty paratha", 260 * qty, 28 * qty, 14 * qty, 10 * qty)
                } else if (text.contains("aloo")) {
                    FoodItem("Aloo Paratha", "$qty paratha", 240 * qty, 35 * qty, 6 * qty, 9 * qty)
                } else {
                    FoodItem("Paratha", "$qty paratha", 200 * qty, 30 * qty, 5 * qty, 7 * qty)
                }
            }
            text.contains("arbi") || text.contains("salan") -> {
                FoodItem("Arbi Ka Salan", "$qty serving", 180 * qty, 15 * qty, 4 * qty, 8 * qty)
            }
            text.contains("mango") -> {
                FoodItem("Mango", "$qty piece", 120 * qty, 28 * qty, 1 * qty, 0 * qty)
            }
            text.contains("lassi") -> {
                FoodItem("Lassi", "$qty glass", 150 * qty, 18 * qty, 4 * qty, 6 * qty)
            }
            text.contains("coke") || text.contains("cola") || text.contains("pepsi") || text.contains("soda") -> {
                FoodItem("Coke Cola", "$qty glass", 140 * qty, 35 * qty, 0 * qty, 0 * qty)
            }
            text.contains("nimko") -> {
                FoodItem("Nimko", "$qty serving", 120 * qty, 12 * qty, 2 * qty, 7 * qty)
            }
            text.contains("daal") || text.contains("dal") || text.contains("lentils") -> {
                FoodItem("Daal", "$qty plate", 180 * qty, 25 * qty, 12 * qty, 3 * qty)
            }
            text.contains("tea cake") || text.contains("cake") -> {
                if (text.contains("rusk") || text.contains("rus")) {
                    FoodItem("Cake Rusk", "$qty pieces", 60 * qty, 10 * qty, 1 * qty, 2 * qty)
                } else {
                    FoodItem("Tea Cake", "$qty tea cakes", 150 * qty, 25 * qty, 3 * qty, 5 * qty)
                }
            }
            text.contains("tea") || text.contains("chai") -> {
                FoodItem("Tea", "$qty cup", 40 * qty, 5 * qty, 1 * qty, 1 * qty)
            }
            text.contains("coffee") || text.contains("latte") -> {
                FoodItem("Coffee", "$qty cup", 60 * qty, 8 * qty, 2 * qty, 2 * qty)
            }
            text.contains("bread") || text.contains("toast") || text.contains("slice") -> {
                FoodItem("Bread", "$qty slice", 70 * qty, 13 * qty, 3 * qty, 1 * qty)
            }
            text.contains("fry") || text.contains("fried egg") || text.contains("egg") -> {
                if (text.contains("full fry") || text.contains("fry")) {
                    FoodItem("Fried Egg", "$qty egg", 90 * qty, 1 * qty, 6 * qty, 7 * qty)
                } else {
                    FoodItem("Egg", "$qty egg", 75 * qty, 1 * qty, 6 * qty, 5 * qty)
                }
            }
            text.contains("shami") || text.contains("kabab") || text.contains("kebab") -> {
                FoodItem("Chicken Shami Kabab", "$qty piece", 140 * qty, 8 * qty, 12 * qty, 6 * qty)
            }
            text.contains("raita") || text.contains("yogurt") -> {
                FoodItem("Raita", "$qty serving", 50 * qty, 4 * qty, 2 * qty, 2 * qty)
            }
            text.contains("burger") -> {
                FoodItem("Double Patty Beef Burger", "$qty burger", 580 * qty, 42 * qty, 32 * qty, 28 * qty)
            }
            text.contains("sundae") || text.contains("sunfae") || text.contains("ice cream") -> {
                FoodItem("Chocolate Sundae", "$qty cup", 280 * qty, 42 * qty, 4 * qty, 11 * qty)
            }
            text.contains("biryani") || text.contains("rice") -> {
                FoodItem("Chicken Biryani", "$qty plate", 450 * qty, 55 * qty, 22 * qty, 14 * qty)
            }
            text.contains("pizza") -> {
                FoodItem("Pizza", "$qty slice", 280 * qty, 32 * qty, 12 * qty, 11 * qty)
            }
            text.contains("apple") || text.contains("fruit") -> {
                FoodItem("Apple", "$qty piece", 80 * qty, 20 * qty, 0 * qty, 0 * qty)
            }
            text.contains("oats") || text.contains("oatmeal") -> {
                FoodItem("Oatmeal", "$qty bowl", 180 * qty, 32 * qty, 6 * qty, 3 * qty)
            }
            text.contains("run") || text.contains("jog") || text.contains("exercise") || text.contains("workout") -> {
                FoodItem("Cardio Exercise", "$qty session", -150 * qty, 0, 0, 0)
            }
            else -> {
                val capitalized = segment.trim().replaceFirstChar { it.uppercase() }
                FoodItem(
                    name = capitalized.ifBlank { "Food Item" },
                    serving = "$qty serving",
                    calories = 150 * qty,
                    carbsGrams = 20 * qty,
                    proteinGrams = 5 * qty,
                    fatGrams = 4 * qty
                )
            }
        }
    }

    fun toJson(items: List<FoodItem>): String {
        val type = Types.newParameterizedType(List::class.java, FoodItem::class.java)
        val adapter = moshi.adapter<List<FoodItem>>(type)
        return adapter.toJson(items)
    }

    fun fromJson(json: String): List<FoodItem> {
        return try {
            val type = Types.newParameterizedType(List::class.java, FoodItem::class.java)
            val adapter = moshi.adapter<List<FoodItem>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
