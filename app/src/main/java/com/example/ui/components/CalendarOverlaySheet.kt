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

data class CalendarDay(
    val dayNum: String,
    val dateFormatted: String,
    val isCurrentMonth: Boolean
)

@Composable
fun CalendarOverlaySheet(
    isVisible: Boolean,
    selectedDateStr: String,
    selectedMonthStr: String,
    todayDateStr: String = "26 Jul 2026",
    loggedDatesSet: Set<String> = emptySet(),
    onMonthSelect: (String) -> Unit,
    onDaySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthMap = mapOf(
        "Jan" to 0, "Feb" to 1, "Mar" to 2, "Apr" to 3,
        "May" to 4, "Jun" to 5, "Jul" to 6, "Aug" to 7,
        "Sep" to 8, "Oct" to 9, "Nov" to 10, "Dec" to 11
    )

    val parts = todayDateStr.split(" ")
    val todayMonthName = parts.getOrNull(1) ?: "Jul"
    val todayYear = parts.getOrNull(2)?.toIntOrNull() ?: 2026
    val todayMonthIdx = monthMap[todayMonthName] ?: 6

    var activeYear by remember(todayYear) { mutableIntStateOf(todayYear) }
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
                    weekRow.forEach { dayInfo ->
                        val dayNum = dayInfo.dayNum
                        val dateFormatted = dayInfo.dateFormatted
                        val isSelected = selectedDateStr == dateFormatted
                        val hasMeal = loggedDatesSet.contains(dateFormatted)
                        val isHighlighted = dayInfo.isCurrentMonth && (isSelected || hasMeal)

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
                                .clickable { onDaySelect(dateFormatted) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum,
                                fontSize = 14.5.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    !dayInfo.isCurrentMonth -> TextMuted.copy(alpha = 0.35f)
                                    isHighlighted -> GreenPrimary
                                    else -> TextPrimary
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Month Selector Bar (Past Months Up To Current Month with Year Breaks)
            val monthEntries = remember(todayDateStr) {
                val list = mutableListOf<MonthBarEntry>()
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

                // Historical years 2023..(todayYear - 1)
                for (year in 2023 until todayYear) {
                    list.add(MonthBarEntry.YearLabel(year.toString()))
                    for (m in months) {
                        list.add(MonthBarEntry.Month(m, year))
                    }
                }

                // Current Year up to current month
                list.add(MonthBarEntry.YearLabel(todayYear.toString()))
                for (i in 0..todayMonthIdx) {
                    list.add(MonthBarEntry.Month(months[i], todayYear))
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

private fun generateDaysMatrix(monthName: String, year: Int): List<List<CalendarDay>> {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthIdx = months.indexOf(monthName).takeIf { it >= 0 } ?: 2
    
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, monthIdx)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2

    // previous month
    val prevCal = Calendar.getInstance().apply { time = cal.time }
    prevCal.add(Calendar.MONTH, -1)
    val prevMaxDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val prevMonthName = months[prevCal.get(Calendar.MONTH)]
    val prevYear = prevCal.get(Calendar.YEAR)

    // next month
    val nextCal = Calendar.getInstance().apply { time = cal.time }
    nextCal.add(Calendar.MONTH, 1)
    val nextMonthName = months[nextCal.get(Calendar.MONTH)]
    val nextYear = nextCal.get(Calendar.YEAR)

    val matrix = mutableListOf<List<CalendarDay>>()
    var currentWeek = mutableListOf<CalendarDay>()

    for (i in offset - 1 downTo 0) {
        val d = prevMaxDays - i
        currentWeek.add(CalendarDay(d.toString(), "$d $prevMonthName $prevYear", false))
    }

    for (day in 1..maxDays) {
        currentWeek.add(CalendarDay(day.toString(), "$day $monthName $year", true))
        if (currentWeek.size == 7) {
            matrix.add(currentWeek)
            currentWeek = mutableListOf()
        }
    }

    if (currentWeek.isNotEmpty()) {
        var nextDay = 1
        while (currentWeek.size < 7) {
            currentWeek.add(CalendarDay(nextDay.toString(), "$nextDay $nextMonthName $nextYear", false))
            nextDay++
        }
        matrix.add(currentWeek)
    }

    return matrix
}
