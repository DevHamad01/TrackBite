package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntry
import com.example.data.model.ai.SmartNutritionParser
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealCard(
    mealEntry: MealEntry,
    onEditClick: (MealEntry) -> Unit,
    onOptionsClick: (MealEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val foodItems = SmartNutritionParser.fromJson(mealEntry.itemsJson)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Original Prompt Bar
            if (mealEntry.originalPrompt.isNotBlank()) {
                Text(
                    text = mealEntry.originalPrompt,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // AI Breakdown List for multi-item meal
            foodItems.forEachIndexed { index, item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${item.name} (${item.serving})",
                        fontSize = 14.8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Macro Badges Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MacroPill(label = "Calories", value = "${item.calories}")
                        MacroPill(label = "Carbs", value = "${item.carbsGrams}g")
                        MacroPill(label = "Protein", value = "${item.proteinGrams}g")
                        MacroPill(label = "Fat", value = "${item.fatGrams}g")
                    }
                }

                if (index < foodItems.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Meal Total Summary Row with Progress Bars & Percentage Breakdown
            val calsPct = (mealEntry.totalCalories.toFloat() / 2523f * 100).toInt().coerceIn(0, 100)
            val carbsPct = (mealEntry.totalCarbs.toFloat() / 316f * 100).toInt().coerceIn(0, 100)
            val proteinPct = (mealEntry.totalProtein.toFloat() / 158f * 100).toInt().coerceIn(0, 100)
            val fatPct = (mealEntry.totalFat.toFloat() / 71f * 100).toInt().coerceIn(0, 100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Calories
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Calories", fontSize = 10.5.sp, color = TextSecondary)
                    Text(text = "${mealEntry.totalCalories}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomProgressBar(
                        progress = (mealEntry.totalCalories.toFloat() / 2523f).coerceIn(0f, 1f),
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 3.5.dp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$calsPct%", fontSize = 9.5.sp, color = TextMuted)
                }

                // Carbs
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Carbs", fontSize = 10.5.sp, color = TextSecondary)
                    Text(text = "${mealEntry.totalCarbs}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomProgressBar(
                        progress = (mealEntry.totalCarbs.toFloat() / 316f).coerceIn(0f, 1f),
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 3.5.dp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$carbsPct%", fontSize = 9.5.sp, color = TextMuted)
                }

                // Protein
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Protein", fontSize = 10.5.sp, color = TextSecondary)
                    Text(text = "${mealEntry.totalProtein}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomProgressBar(
                        progress = (mealEntry.totalProtein.toFloat() / 158f).coerceIn(0f, 1f),
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 3.5.dp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$proteinPct%", fontSize = 9.5.sp, color = TextMuted)
                }

                // Fat
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Fat", fontSize = 10.5.sp, color = TextSecondary)
                    Text(text = "${mealEntry.totalFat}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomProgressBar(
                        progress = (mealEntry.totalFat.toFloat() / 71f).coerceIn(0f, 1f),
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 3.5.dp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$fatPct%", fontSize = 9.5.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealEntry.formattedTime,
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onEditClick(mealEntry) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Entry",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { onOptionsClick(mealEntry) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Entry Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFF4F8F5))
            .padding(horizontal = 6.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = "$label: $value",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            maxLines = 1
        )
    }
}


