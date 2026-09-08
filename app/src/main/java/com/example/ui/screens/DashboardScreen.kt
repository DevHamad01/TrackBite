package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.ui.theme.GreenPrimary
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BottomInputBar
import com.example.ui.components.CalendarOverlaySheet
import com.example.ui.components.DateSelectorStrip
import com.example.ui.components.DrawerMenu
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.EntryOptionsBottomSheet
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.MealCard
import com.example.ui.components.QuickConfirmationCard
import com.example.ui.components.SavedEntriesBottomSheet
import com.example.ui.components.SummaryCarouselCard
import com.example.ui.components.TopAppBarZone
import com.example.ui.components.WaterTrackingCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JournableViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: JournableViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val selectedDate by viewModel.selectedDate.collectAsState()
    val displayDateTitle by viewModel.displayDateTitle.collectAsState()
    val loggedDatesSet by viewModel.loggedDatesSet.collectAsState()
    val weekDaysState by viewModel.weekDaysStrip.collectAsState()
    val isCalendarExpanded by viewModel.isCalendarExpanded.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val entries by viewModel.entriesForSelectedDate.collectAsState()
    val waterLog by viewModel.waterLogForSelectedDate.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val summaryMetrics by viewModel.summaryMetrics.collectAsState()
    val isWaterTrackerEnabled by viewModel.isWaterTrackerEnabled.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val allMealEntries by viewModel.allMealEntries.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()

    val showFeedbackDialog by viewModel.showFeedbackDialog.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = if (isLoggedIn) "Logout Confirmation" else "Login Confirmation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isLoggedIn) "Are you sure you want to log out? Your recorded entries will remain saved locally." else "Log back into TrackBite to view and manage your logged entries?",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.isLoggedIn.value = !isLoggedIn
                        Toast.makeText(
                            context,
                            if (isLoggedIn) "Logged out successfully." else "Logged in successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(if (isLoggedIn) "Yes, Logout" else "Log In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = Color.White
        )
    }

    val savedEntries by viewModel.savedEntriesList.collectAsState()
    val recentEntries by viewModel.recentEntriesList.collectAsState()

    val activeOptionEntry by viewModel.activeOptionEntry.collectAsState()
    val confirmationEntry by viewModel.confirmationEntry.collectAsState()
    val editEntryDialogTarget by viewModel.editEntryDialogTarget.collectAsState()
    val isSavedEntriesSheetOpen by viewModel.isSavedEntriesSheetOpen.collectAsState()

    val inputText by viewModel.inputText.collectAsState()
    val isProcessingAi by viewModel.isProcessingAi.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                userProfile = userProfile,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                onUpgradeClick = {
                    coroutineScope.launch { drawerState.close() }
                    Toast.makeText(context, "TrackBite Premium feature activated!", Toast.LENGTH_SHORT).show()
                },
                onNavigateToDailyGoals = {
                    viewModel.currentScreen.value = AppScreen.DAILY_GOALS
                },
                onNavigateToWeeklySummary = {
                    viewModel.currentScreen.value = AppScreen.WEEKLY_SUMMARY
                },
                onNavigateToWeightTracker = {
                    viewModel.currentScreen.value = AppScreen.WEIGHT_TRACKER
                },
                onNavigateToReminders = {
                    viewModel.currentScreen.value = AppScreen.REMINDERS
                },
                onNavigateToAccount = {
                    viewModel.currentScreen.value = AppScreen.ACCOUNT
                },
                onNavigateToSettings = {
                    viewModel.currentScreen.value = AppScreen.SETTINGS
                },
                onNavigateToStreak = {
                    viewModel.currentScreen.value = AppScreen.STREAK
                },
                onNavigateToWaterTracker = {
                    viewModel.currentScreen.value = AppScreen.WATER_TRACKER
                },
                onFeedbackClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.showFeedbackDialog.value = true
                },
                onExportClick = {
                    coroutineScope.launch { drawerState.close() }
                    showExportDialog = true
                },
                isLoggedIn = isLoggedIn,
                onLogoutClick = {
                    coroutineScope.launch { drawerState.close() }
                    showLogoutDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CleanBackground)
                        .statusBarsPadding()
                ) {
                    TopAppBarZone(
                        selectedDate = displayDateTitle,
                        isCalendarExpanded = isCalendarExpanded,
                        streakCount = userProfile.streakCount,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onDateToggleClick = { viewModel.isCalendarExpanded.value = !isCalendarExpanded },
                        onResetTodayClick = { viewModel.resetToToday() },
                        onShareClick = { showExportDialog = true },
                        onStreakClick = { viewModel.currentScreen.value = AppScreen.STREAK }
                    )

                    // Calendar Overlay Dropdown Sheet
                    CalendarOverlaySheet(
                        isVisible = isCalendarExpanded,
                        selectedDateStr = selectedDate,
                        selectedMonthStr = selectedMonth,
                        todayDateStr = viewModel.todayDateStr,
                        loggedDatesSet = loggedDatesSet,
                        onMonthSelect = { viewModel.selectedMonth.value = it },
                        onDaySelect = { date ->
                            viewModel.selectDate(date)
                            viewModel.isCalendarExpanded.value = false
                        }
                    )

                    // 7-day Horizontal Selector Strip
                    DateSelectorStrip(
                        dates = weekDaysState.dates,
                        onDateSelect = { viewModel.selectDate(it) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sticky Summary Carousel Card (Calories & Macros)
                    SummaryCarouselCard(metrics = summaryMetrics)

                    Spacer(modifier = Modifier.height(4.dp))
                }
            },
            bottomBar = {
                BottomInputBar(
                    inputText = inputText,
                    isProcessingAi = isProcessingAi,
                    freeEntriesRemaining = userProfile.freeEntriesRemaining,
                    onInputTextChange = { viewModel.inputText.value = it },
                    onSubmit = { viewModel.processInputAndLog() },
                    onSavedEntriesClick = { viewModel.isSavedEntriesSheetOpen.value = true },
                    onUpgradeClick = { Toast.makeText(context, "TrackBite Premium unlocked!", Toast.LENGTH_SHORT).show() }
                )
            },
            containerColor = CleanBackground,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Water Tracker Card
                if (isWaterTrackerEnabled) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        WaterTrackingCard(
                            waterLog = waterLog,
                            onIncrement = { viewModel.incrementWater() },
                            onDecrement = { viewModel.decrementWater() }
                        )
                    }
                }

                // Zone 3: Pending Quick Confirmation Card (State 3 - Image 4)
                if (confirmationEntry != null) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        QuickConfirmationCard(
                            mealEntry = confirmationEntry!!,
                            onConfirm = { viewModel.confirmQuickEntry(it) },
                            onCancel = { viewModel.cancelQuickEntry(it) }
                        )
                    }
                }

                // Zone 3: Logged Daily Feed
                val confirmedEntries = entries.filter { it.isConfirmed }

                if (confirmedEntries.isEmpty() && confirmationEntry == null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🥗 No entries for $selectedDate",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add your meals or workouts below using text or saved templates to stay on track.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(confirmedEntries, key = { it.id }) { mealEntry ->
                        MealCard(
                            mealEntry = mealEntry,
                            onEditClick = { viewModel.openEditEntryDialog(it) },
                            onOptionsClick = { viewModel.openEntryOptions(it) },
                            onSaveInlineEdit = { entry, newText ->
                                viewModel.reanalyzeMealEntry(entry, newText)
                            }
                        )
                    }
                }
            }
        }
    }

    // State 1: Entry Options Bottom Sheet (Image 1)
    EntryOptionsBottomSheet(
        entry = activeOptionEntry,
        onDismiss = { viewModel.closeEntryOptions() },
        onEditEntry = { viewModel.openEditEntryDialog(it) },
        onAdjustMacros = { entry ->
            viewModel.selectedMealEntryForEdit.value = entry
            viewModel.closeEntryOptions()
            viewModel.currentScreen.value = AppScreen.ADJUST_MACROS
        },
        onChangeDateTime = { entry ->
            viewModel.selectedMealEntryForEdit.value = entry
            viewModel.closeEntryOptions()
            viewModel.currentScreen.value = AppScreen.CHANGE_DATE_TIME
        },
        onAddToSaved = { viewModel.saveActiveOptionEntryToFavorites() },
        onDeleteEntry = { viewModel.deleteActiveOptionEntry() }
    )

    // State 2: Saved / Recent Entries Bottom Sheet (Image 7)
    SavedEntriesBottomSheet(
        isOpen = isSavedEntriesSheetOpen,
        savedEntries = savedEntries,
        recentEntries = recentEntries,
        onDismiss = { viewModel.isSavedEntriesSheetOpen.value = false },
        onSelectEntryPrompt = { prompt -> viewModel.logSavedOrRecentEntry(prompt) }
    )

    // Edit Entry Dialog
    EditEntryDialog(
        targetEntry = editEntryDialogTarget,
        onDismiss = { viewModel.editEntryDialogTarget.value = null },
        onSave = { entry, prompt, cals, carbs, protein, fat ->
            viewModel.saveEditedEntry(entry, prompt, cals, carbs, protein, fat)
        }
    )

    // Export Report Modal Dialog
    if (showExportDialog) {
        ExportReportDialog(
            userProfile = userProfile,
            mealEntries = allMealEntries,
            weightLogs = weightLogs,
            onDismiss = { showExportDialog = false }
        )
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        FeedbackDialog(
            initialEmail = userEmail,
            onDismiss = { viewModel.showFeedbackDialog.value = false }
        )
    }
}
