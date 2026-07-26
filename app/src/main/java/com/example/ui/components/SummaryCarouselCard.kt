package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SummaryMetrics
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CarbsBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FatGreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.ProteinPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SummaryCarouselCard(
    metrics: SummaryMetrics,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            if (page == 0) {
                CaloriesCard(metrics = metrics)
            } else {
                MacrosCard(metrics = metrics)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Carousel Pagination Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                val isActive = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .width(if (isActive) 18.dp else 6.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isActive) GreenPrimary else TextMuted.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun CaloriesCard(metrics: SummaryMetrics) {
    val progress = if (metrics.targetCalories > 0) {
        (metrics.foodCalories.toFloat() / metrics.targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7F4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header (No icon)
            Text(
                text = "Calories",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar (Green)
            CustomProgressBar(
                progress = progress,
                color = GreenPrimary,
                trackColor = Color(0xFFE2ECE0),
                height = 5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Food", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${metrics.foodCalories}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "Exercise", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${metrics.exerciseCalories}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${metrics.remainingCalories}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MacrosCard(metrics: SummaryMetrics) {
    val carbsProgress = if (metrics.targetCarbsGrams > 0) (metrics.carbsGrams.toFloat() / metrics.targetCarbsGrams).coerceIn(0f, 1f) else 0f
    val proteinProgress = if (metrics.targetProteinGrams > 0) (metrics.proteinGrams.toFloat() / metrics.targetProteinGrams).coerceIn(0f, 1f) else 0f
    val fatProgress = if (metrics.targetFatGrams > 0) (metrics.fatGrams.toFloat() / metrics.targetFatGrams).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7F4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header (No icon)
            Text(
                text = "Macros",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Carbs
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Carbs", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    CustomProgressBar(
                        progress = carbsProgress,
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 4.dp,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${metrics.carbsGrams}g / ${metrics.targetCarbsGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Protein
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Protein", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    CustomProgressBar(
                        progress = proteinProgress,
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 4.dp,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${metrics.proteinGrams}g / ${metrics.targetProteinGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Fat
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Fat", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    CustomProgressBar(
                        progress = fatProgress,
                        color = GreenPrimary,
                        trackColor = Color(0xFFE2ECE0),
                        height = 4.dp,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${metrics.fatGrams}g / ${metrics.targetFatGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

