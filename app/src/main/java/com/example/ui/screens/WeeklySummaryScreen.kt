package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabItem by remember { mutableIntStateOf(0) } // 0 = This week, 1 = Last week, 2 = 2 weeks ago, 3 = 3 weeks ago
    val tabs = listOf("This week", "Last week", "2 weeks ago", "3 weeks ago")

    val buttonBlue = Color(0xFF2B5B84)
    val successGreen = Color(0xFF388E3C)

    // Calculate start and end dates for selected tab week
    val today = LocalDate.now()
    val currentMonday = today.with(DayOfWeek.MONDAY)
    val selectedMonday = currentMonday.minusWeeks(selectedTabItem.toLong())
    val selectedSunday = selectedMonday.plusDays(6)

    val dateRangeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
    val dateRangeText = "${selectedMonday.format(dateRangeFormatter)} - ${selectedSunday.format(dateRangeFormatter)}"

    val daysOfWeek = (0..6).map { dayIndex ->
        val date = selectedMonday.plusDays(dayIndex.toLong())
        Triple(
            date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
            date.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)),
            date
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weekly Summary",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Weekly Summary - TrackBite")
                            putExtra(Intent.EXTRA_TEXT, "My Weekly Summary ($dateRangeText):\nDaily Calorie Goal: ${userProfile.targetCalories} kcal\nTracked with TrackBite.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Weekly Summary"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color(0xFFF9FAFC),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Week Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabItem,
                containerColor = Color.White,
                contentColor = buttonBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabItem < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabItem]),
                            height = 3.dp,
                            color = buttonBlue
                        )
                    }
                },
                divider = { HorizontalDivider(color = Color(0xFFEAEAEA)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabItem == index,
                        onClick = { selectedTabItem = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTabItem == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabItem == index) buttonBlue else TextMuted
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = dateRangeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "0 calories under budget this week",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = successGreen
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "0 out of 7 days tracked this week",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }

                // Calories Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Calories",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1.3f))
                            Text(text = "Food", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Exercise", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Remaining", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0))

                        // Daily Rows
                        daysOfWeek.forEach { (dayName, dateStr, _) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1.3f)) {
                                    Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = dateStr, fontSize = 14.sp, color = TextSecondary)
                                }
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = Color(0xFFF7F7F7))
                        }

                        // Total Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1.3f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        // Versus last week
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Versus last week", fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1.3f))
                            Text(text = "-", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "* Based on a daily goal of ${userProfile.targetCalories} calories",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                // Macronutrients Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Macronutrients",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1.3f))
                            Text(text = "Carbs", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Protein", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Fat", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0))

                        // Daily Rows
                        daysOfWeek.forEach { (dayName, dateStr, _) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1.3f)) {
                                    Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = dateStr, fontSize = 14.sp, color = TextSecondary)
                                }
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = Color(0xFFF7F7F7))
                        }

                        // Total Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1.3f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "* Based on a daily macronutrient distribution of 50% carbohydrates, 25% protein, and 25% fat",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Weight Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Weight",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        HorizontalDivider(color = Color(0xFFF0F0F0))

                        daysOfWeek.forEach { (dayName, dateStr, dateObj) ->
                            val isSat = dayName == "Sat"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1.3f)) {
                                    Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = dateStr, fontSize = 14.sp, color = TextSecondary)
                                }
                                Text(
                                    text = if (isSat) "23:24" else "-",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (isSat) "50 kg" else "-",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF7F7F7))
                        }
                    }
                }

                // Water Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Water",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1.3f))
                            Text(text = "Cups", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Volume", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text(text = "Remaining Cups", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0))

                        // Daily Rows
                        daysOfWeek.forEach { (dayName, dateStr, _) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1.3f)) {
                                    Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = dateStr, fontSize = 14.sp, color = TextSecondary)
                                }
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(text = "-", fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = Color(0xFFF7F7F7))
                        }
                    }
                }

                // Consistency Stats Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Consistency Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Food entries", fontSize = 15.sp, color = TextPrimary)
                            Text(text = "0", fontSize = 15.sp, color = TextPrimary)
                        }

                        HorizontalDivider(color = Color(0xFFF7F7F7))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Exercise entries", fontSize = 15.sp, color = TextPrimary)
                            Text(text = "0", fontSize = 15.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
