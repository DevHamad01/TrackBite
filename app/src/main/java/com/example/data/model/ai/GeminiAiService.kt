package com.example.data.model.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiAiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun processMultiModalInput(
        prompt: String,
        imageBitmap: Bitmap? = null
    ): SmartNutritionParser.ParsedMealResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback to local smart parser
            return@withContext SmartNutritionParser.parsePrompt(prompt)
        }

        try {
            val jsonPrompt = """
                Analyze the following meal input (text and/or image): "${prompt.ifBlank { "What is in this meal image?" }}"
                Return ONLY a valid JSON array of objects representing each food item in the meal.
                Each object must have exactly these keys:
                - "name": string (e.g. "Paratha")
                - "serving": string (e.g. "1 paratha")
                - "calories": integer (e.g. 200)
                - "carbsGrams": integer (e.g. 30)
                - "proteinGrams": integer (e.g. 5)
                - "fatGrams": integer (e.g. 7)
                
                No extra explanations, no markdown backticks, only pure JSON array.
            """.trimIndent()

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", jsonPrompt))

            if (imageBitmap != null) {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val base64Data = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                val inlineDataObj = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64Data)
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            val contentsArray = JSONArray()
            contentsArray.put(JSONObject().put("parts", partsArray))

            val requestBodyJson = JSONObject()
                .put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (!response.isSuccessful || responseText.isBlank()) {
                return@withContext SmartNutritionParser.parsePrompt(prompt)
            }

            val parsedItems = parseGeminiResponse(responseText)
            if (parsedItems.isEmpty()) {
                return@withContext SmartNutritionParser.parsePrompt(prompt)
            }

            val totalCals = parsedItems.sumOf { it.calories }
            val totalCarbs = parsedItems.sumOf { it.carbsGrams }
            val totalProtein = parsedItems.sumOf { it.proteinGrams }
            val totalFat = parsedItems.sumOf { it.fatGrams }

            SmartNutritionParser.ParsedMealResult(
                items = parsedItems,
                totalCalories = totalCals,
                totalCarbs = totalCarbs,
                totalProtein = totalProtein,
                totalFat = totalFat
            )
        } catch (e: Exception) {
            SmartNutritionParser.parsePrompt(prompt)
        }
    }

    private fun parseGeminiResponse(responseText: String): List<FoodItem> {
        return try {
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val candidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = candidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val rawText = parts.optJSONObject(0)?.optString("text") ?: ""

            val cleanJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            val list = mutableListOf<FoodItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    FoodItem(
                        name = obj.optString("name", "Food Item"),
                        serving = obj.optString("serving", "1 serving"),
                        calories = obj.optInt("calories", 150),
                        carbsGrams = obj.optInt("carbsGrams", 20),
                        proteinGrams = obj.optInt("proteinGrams", 5),
                        fatGrams = obj.optInt("fatGrams", 5)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
