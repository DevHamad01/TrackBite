package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.DailyGoalCalculatorScreen
import com.example.ui.screens.DailyGoalsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FirstDayOfWeekScreen
import com.example.ui.screens.LiquidUnitScreen
import com.example.ui.screens.NotificationsSettingsScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreakScreen
import com.example.ui.screens.TermsAndPrivacyScreen
import com.example.ui.screens.WaterTrackerScreen
import com.example.ui.screens.WeeklySummaryScreen
import com.example.ui.screens.WeightTrackerScreen
import com.example.ui.screens.WeightUnitScreen
import com.example.ui.theme.JournableTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JournableViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JournableTheme {
                val viewModel: JournableViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()

                val liquidUnit by viewModel.liquidUnit.collectAsState()
                val weightUnit by viewModel.weightUnit.collectAsState()
                val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsState()
                val isWaterTrackerEnabled by viewModel.isWaterTrackerEnabled.collectAsState()
                val waterGoalCups by viewModel.waterGoalCups.collectAsState()

                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.DAILY_GOALS -> {
                        DailyGoalsScreen(
                            userProfile = userProfile,
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            onOpenCalculatorClick = { viewModel.currentScreen.value = AppScreen.GOAL_CALCULATOR },
                            onSaveGoals = { calories, carbs, protein, fat ->
                                viewModel.updateUserProfileGoals(calories, carbs, protein, fat)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.GOAL_CALCULATOR -> {
                        DailyGoalCalculatorScreen(
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DAILY_GOALS },
                            onFinish = { calculatedCalories ->
                                viewModel.updateUserProfileGoals(calculatedCalories)
                                viewModel.currentScreen.value = AppScreen.DAILY_GOALS
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.WEEKLY_SUMMARY -> {
                        WeeklySummaryScreen(
                            userProfile = userProfile,
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.WEIGHT_TRACKER -> {
                        WeightTrackerScreen(
                            userProfile = userProfile,
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.STREAK -> {
                        StreakScreen(
                            userProfile = userProfile,
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            onNavigateToWeeklySummary = { viewModel.currentScreen.value = AppScreen.WEEKLY_SUMMARY },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.ACCOUNT -> {
                        AccountScreen(
                            userProfile = userProfile,
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.REMINDERS -> {
                        RemindersScreen(
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            onNavigateToLiquidUnit = { viewModel.currentScreen.value = AppScreen.SETTINGS_LIQUID_UNIT },
                            onNavigateToWeightUnit = { viewModel.currentScreen.value = AppScreen.SETTINGS_WEIGHT_UNIT },
                            onNavigateToFirstDay = { viewModel.currentScreen.value = AppScreen.SETTINGS_FIRST_DAY },
                            onNavigateToNotifications = { viewModel.currentScreen.value = AppScreen.SETTINGS_NOTIFICATIONS },
                            onNavigateToTermsPrivacy = { viewModel.currentScreen.value = AppScreen.SETTINGS_TERMS_PRIVACY },
                            liquidUnit = liquidUnit,
                            weightUnit = weightUnit,
                            firstDayOfWeek = firstDayOfWeek,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS_LIQUID_UNIT -> {
                        LiquidUnitScreen(
                            currentUnit = liquidUnit,
                            onUnitSelected = {
                                viewModel.liquidUnit.value = it
                                viewModel.currentScreen.value = AppScreen.SETTINGS
                            },
                            onBackClick = { viewModel.currentScreen.value = AppScreen.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS_WEIGHT_UNIT -> {
                        WeightUnitScreen(
                            currentUnit = weightUnit,
                            onUnitSelected = {
                                viewModel.weightUnit.value = it
                                viewModel.currentScreen.value = AppScreen.SETTINGS
                            },
                            onBackClick = { viewModel.currentScreen.value = AppScreen.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS_FIRST_DAY -> {
                        FirstDayOfWeekScreen(
                            currentDay = firstDayOfWeek,
                            onDaySelected = {
                                viewModel.firstDayOfWeek.value = it
                                viewModel.currentScreen.value = AppScreen.SETTINGS
                            },
                            onBackClick = { viewModel.currentScreen.value = AppScreen.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS_NOTIFICATIONS -> {
                        NotificationsSettingsScreen(
                            onBackClick = { viewModel.currentScreen.value = AppScreen.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SETTINGS_TERMS_PRIVACY -> {
                        TermsAndPrivacyScreen(
                            onBackClick = { viewModel.currentScreen.value = AppScreen.SETTINGS },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.WATER_TRACKER -> {
                        WaterTrackerScreen(
                            isEnabled = isWaterTrackerEnabled,
                            waterGoalCups = waterGoalCups,
                            onEnabledChange = { viewModel.isWaterTrackerEnabled.value = it },
                            onGoalCupsChange = { viewModel.waterGoalCups.value = it },
                            onBackClick = { viewModel.currentScreen.value = AppScreen.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

