package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun QuickConfirmationCard(
    mealEntry: MealEntry,
    onConfirm: (MealEntry) -> Unit,
    onCancel: (MealEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val foodItems = SmartNutritionParser.fromJson(mealEntry.itemsJson)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item names list
                Column(modifier = Modifier.weight(1f)) {
                    foodItems.forEach { item ->
                        Text(
                            text = "${item.name} (${item.serving})",
                            fontSize = 14.8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Fast Check & Cancel Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onConfirm(mealEntry) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm Entry",
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCancel(mealEntry) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Entry",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Summary Metrics
            val calsPct = (mealEntry.totalCalories.toFloat() / 2523f * 100).toInt().coerceIn(0, 100)
            val carbsPct = (mealEntry.totalCarbs.toFloat() / 316f * 100).toInt().coerceIn(0, 100)
            val proteinPct = (mealEntry.totalProtein.toFloat() / 158f * 100).toInt().coerceIn(0, 100)
            val fatPct = (mealEntry.totalFat.toFloat() / 71f * 100).toInt().coerceIn(0, 100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
        }
    }
}


