package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.GreenActivePill
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

sealed class MonthBarEntry {
    data class Month(val name: String, val year: Int) : MonthBarEntry()
    data class YearLabel(val year: String) : MonthBarEntry()
}

@Composable
fun CalendarOverlaySheet(
    isVisible: Boolean,
    selectedDateStr: String,
    selectedMonthStr: String,
    loggedDatesSet: Set<String> = emptySet(),
    onMonthSelect: (String) -> Unit,
    onDaySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeYear by remember { mutableIntStateOf(2026) }
    val scrollState = rememberScrollState()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(CleanBackground)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Days of week header: M T W T F S S
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month Calendar Days Grid
            val daysMatrix = remember(selectedMonthStr, activeYear) {
                generateDaysMatrix(selectedMonthStr, activeYear)
            }

            daysMatrix.forEach { weekRow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekRow.forEach { dayNum ->
                        if (dayNum.isEmpty()) {
                            Box(modifier = Modifier.size(36.dp))
                        } else {
                            val dayInt = dayNum.toIntOrNull() ?: 1
                            val dateFormatted = "$dayNum $selectedMonthStr $activeYear"
                            val isFuture = isFutureDate(dayInt, selectedMonthStr, activeYear, "28 Mar 2026")
                            val isSelected = selectedDateStr == dateFormatted
                            val hasMeal = loggedDatesSet.contains(dateFormatted)
                            val isHighlighted = !isFuture && (isSelected || hasMeal)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isHighlighted) GreenActivePill else Color.Transparent)
                                    .border(
                                        width = if (isHighlighted) 1.dp else 0.dp,
                                        color = if (isSelected) GreenPrimary else if (hasMeal) Color(0xFFC4E5C6) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .then(
                                        if (!isFuture) {
                                            Modifier.clickable { onDaySelect(dateFormatted) }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum,
                                    fontSize = 14.5.sp,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isFuture -> TextMuted.copy(alpha = 0.35f)
                                        isHighlighted -> GreenPrimary
                                        else -> TextPrimary
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Month Selector Bar (Past Months Up To Current Month with Year Breaks)
            val monthEntries = remember {
                val list = mutableListOf<MonthBarEntry>()
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

                // Historical years 2023..2025
                for (year in 2023..2025) {
                    if (year > 2023) {
                        list.add(MonthBarEntry.YearLabel(year.toString()))
                    }
                    for (m in months) {
                        list.add(MonthBarEntry.Month(m, year))
                    }
                }

                // Current Year 2026 up to current month (Mar 2026)
                list.add(MonthBarEntry.YearLabel("2026"))
                for (m in listOf("Jan", "Feb", "Mar")) {
                    list.add(MonthBarEntry.Month(m, 2026))
                }

                list
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                monthEntries.forEach { entry ->
                    when (entry) {
                        is MonthBarEntry.YearLabel -> {
                            Text(
                                text = entry.year,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                        is MonthBarEntry.Month -> {
                            val isMonthActive = (selectedMonthStr == entry.name && activeYear == entry.year)
                            val activeBg = Color(0xFFD6EBF8) // Light blue pill

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isMonthActive) activeBg else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isMonthActive) Color.Transparent else CardBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        activeYear = entry.year
                                        onMonthSelect(entry.name)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = entry.name,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isMonthActive) FontWeight.Bold else FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
        }
    }
}

private fun isFutureDate(day: Int, monthName: String, year: Int, todayStr: String = "28 Mar 2026"): Boolean {
    val monthMap = mapOf(
        "Jan" to 0, "Feb" to 1, "Mar" to 2, "Apr" to 3,
        "May" to 4, "Jun" to 5, "Jul" to 6, "Aug" to 7,
        "Sep" to 8, "Oct" to 9, "Nov" to 10, "Dec" to 11
    )
    val curMonthIdx = monthMap[monthName] ?: 0

    val parts = todayStr.split(" ")
    val todayDay = parts.getOrNull(0)?.toIntOrNull() ?: 28
    val todayMonthName = parts.getOrNull(1) ?: "Mar"
    val todayYear = parts.getOrNull(2)?.toIntOrNull() ?: 2026
    val todayMonthIdx = monthMap[todayMonthName] ?: 2

    if (year > todayYear) return true
    if (year < todayYear) return false

    if (curMonthIdx > todayMonthIdx) return true
    if (curMonthIdx < todayMonthIdx) return false

    return day > todayDay
}

private fun generateDaysMatrix(monthName: String, year: Int): List<List<String>> {
    val monthMap = mapOf(
        "Jan" to 0, "Feb" to 1, "Mar" to 2, "Apr" to 3,
        "May" to 4, "Jun" to 5, "Jul" to 6, "Aug" to 7,
        "Sep" to 8, "Oct" to 9, "Nov" to 10, "Dec" to 11
    )
    val monthIdx = monthMap[monthName] ?: 2
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, monthIdx)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2

    val matrix = mutableListOf<List<String>>()
    var currentWeek = mutableListOf<String>()

    for (i in 0 until offset) {
        currentWeek.add("")
    }

    for (day in 1..maxDays) {
        currentWeek.add(day.toString())
        if (currentWeek.size == 7) {
            matrix.add(currentWeek)
            currentWeek = mutableListOf()
        }
    }

    if (currentWeek.isNotEmpty()) {
        while (currentWeek.size < 7) {
            currentWeek.add("")
        }
        matrix.add(currentWeek)
    }

    return matrix
}
