package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FoodItem
import com.example.data.model.MealEntry
import com.example.data.model.SavedEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import com.example.data.model.ai.GeminiAiService
import com.example.data.model.ai.SmartNutritionParser
import com.example.data.repository.JournableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaysOfWeekState(
    val dates: List<DateInfo>
)

data class DateInfo(
    val dateString: String, // e.g. "28 Mar 2026"
    val dayOfWeek: String, // e.g. "Sat"
    val dayOfMonth: String, // e.g. "28"
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasMealEntries: Boolean = false
)

data class SummaryMetrics(
    val foodCalories: Int = 0,
    val exerciseCalories: Int = 0,
    val targetCalories: Int = 2523,
    val remainingCalories: Int = 2523,
    val carbsGrams: Int = 0,
    val targetCarbsGrams: Int = 316,
    val proteinGrams: Int = 0,
    val targetProteinGrams: Int = 158,
    val fatGrams: Int = 0,
    val targetFatGrams: Int = 71
)

enum class AppScreen {
    DASHBOARD,
    DAILY_GOALS,
    GOAL_CALCULATOR,
    WEEKLY_SUMMARY,
    WEIGHT_TRACKER,
    STREAK,
    ACCOUNT,
    REMINDERS,
    SETTINGS,
    SETTINGS_LIQUID_UNIT,
    SETTINGS_WEIGHT_UNIT,
    SETTINGS_FIRST_DAY,
    SETTINGS_NOTIFICATIONS,
    SETTINGS_TERMS_PRIVACY,
    WATER_TRACKER,
    LOGIN,
    ADJUST_MACROS,
    CHANGE_DATE_TIME
}

class JournableViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = JournableRepository(
        mealEntryDao = db.mealEntryDao(),
        waterLogDao = db.waterLogDao(),
        userProfileDao = db.userProfileDao(),
        savedEntryDao = db.savedEntryDao(),
        weightLogDao = db.weightLogDao()
    )
    private val geminiAiService = GeminiAiService(application)

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val dayNameFormatter = SimpleDateFormat("EEE", Locale.US)
    private val dayNumFormatter = SimpleDateFormat("dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    val todayDateStr: String = dateFormatter.format(Date())
    val yesterdayDateStr: String = dateFormatter.format(Date(System.currentTimeMillis() - 86400000L))

    // Auth & Account States
    val isLoggedIn = MutableStateFlow(false)
    val userEmail = MutableStateFlow("user@trackbite.com")

    // Current Selected Date e.g. "26 Jul 2026"
    val selectedDate = MutableStateFlow(todayDateStr)

    // User Settings
    val liquidUnit = MutableStateFlow("Litre (L)")
    val weightUnit = MutableStateFlow("Kilogram (kg)")
    val firstDayOfWeek = MutableStateFlow("Monday")
    val isWaterTrackerEnabled = MutableStateFlow(true)
    val waterGoalCups = MutableStateFlow("8")

    val weightLogs: StateFlow<List<WeightLog>> = repository.weightLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWeightLog(weightKg: Float, dateStr: String = todayDateStr) {
        viewModelScope.launch {
            repository.insertWeightLog(WeightLog(weightKg = weightKg, dateString = dateStr))
        }
    }

    fun deleteWeightLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteWeightLog(logId)
        }
    }

    fun updateTargetWeight(targetWeightKg: Float) {
        viewModelScope.launch {
            val cur = userProfile.value
            repository.saveProfile(cur.copy(targetWeightKg = targetWeightKg))
        }
    }

    val allMealEntries: StateFlow<List<MealEntry>> = repository.getAllMealEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val loggedDatesSet: StateFlow<Set<String>> = repository.getLoggedDates()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf(todayDateStr)
        )

    val displayDateTitle: StateFlow<String> = selectedDate
        .map { dateStr ->
            when (dateStr) {
                todayDateStr -> "Today"
                yesterdayDateStr -> "Yesterday"
                else -> dateStr
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Today"
        )

    val currentScreen = MutableStateFlow(AppScreen.LOGIN)

    fun updateUserProfileGoals(targetCalories: Int, targetCarbs: Int = 316, targetProtein: Int = 158, targetFat: Int = 71) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                targetCalories = targetCalories,
                targetCarbs = targetCarbs,
                targetProtein = targetProtein,
                targetFat = targetFat
            )
            repository.saveProfile(updated)
        }
    }

    // UI Overlay States
    val isCalendarExpanded = MutableStateFlow(false)
    val isSavedEntriesSheetOpen = MutableStateFlow(false)
    val activeOptionEntry = MutableStateFlow<MealEntry?>(null) // State 1: Entry Options Modal
    val confirmationEntry = MutableStateFlow<MealEntry?>(null) // State 3: Quick Confirmation Card
    val editEntryDialogTarget = MutableStateFlow<MealEntry?>(null)
    val isPremiumModalOpen = MutableStateFlow(false)
    val isDrawerOpen = MutableStateFlow(false)
    val isExportModalOpen = MutableStateFlow(false)

    // Logging Input States
    val inputText = MutableStateFlow("")
    val isProcessingAi = MutableStateFlow(false)
    val isRecordingVoice = MutableStateFlow(false)

    // Month Grid Filter in Calendar Sheet
    val selectedMonth = MutableStateFlow(SimpleDateFormat("MMM", Locale.US).format(Date()))

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            // Ensure initial user profile exists without injecting fake meals or dummy prompts
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    targetCalories = 2523,
                    targetCarbs = 316,
                    targetProtein = 158,
                    targetFat = 71,
                    streakCount = 0,
                    freeEntriesRemaining = 0,
                    isPremium = false
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val entriesForSelectedDate: StateFlow<List<MealEntry>> = selectedDate
        .flatMapLatest { dateStr -> repository.getEntriesForDate(dateStr) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val waterLogForSelectedDate: StateFlow<WaterLog> = selectedDate
        .flatMapLatest { dateStr -> repository.getWaterLogForDate(dateStr) }
        .combine(selectedDate) { log, dateStr ->
            log ?: WaterLog(date = dateStr, cups = 0, targetCups = 8)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WaterLog(todayDateStr, cups = 0, targetCups = 8)
        )

    fun loginUser(email: String) {
        userEmail.value = email.ifBlank { "user@trackbite.com" }
        isLoggedIn.value = true
        currentScreen.value = AppScreen.DASHBOARD
    }

    fun logoutUser() {
        isLoggedIn.value = false
        isDrawerOpen.value = false
        currentScreen.value = AppScreen.LOGIN
    }

    fun clearAllUserDataAndLogout() {
        viewModelScope.launch {
            repository.clearAllData()
            isLoggedIn.value = false
            isDrawerOpen.value = false
            currentScreen.value = AppScreen.DASHBOARD
            // Re-seed minimal empty profile
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    targetCalories = 2523,
                    targetCarbs = 316,
                    targetProtein = 158,
                    targetFat = 71,
                    streakCount = 0,
                    freeEntriesRemaining = 0,
                    isPremium = false
                )
            )
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .combine(MutableStateFlow(Unit)) { profile, _ ->
            profile ?: UserProfile(id = 1, targetCalories = 2523)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(id = 1, targetCalories = 2523)
        )

    val savedEntriesList: StateFlow<List<SavedEntry>> = repository.savedEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentEntriesList: StateFlow<List<SavedEntry>> = repository.recentEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val summaryMetrics: StateFlow<SummaryMetrics> = combine(
        entriesForSelectedDate,
        userProfile
    ) { entries, profile ->
        val confirmed = entries.filter { it.isConfirmed && !it.isAnalyzing }
        val foodCals = confirmed.filter { it.totalCalories > 0 }.sumOf { it.totalCalories }
        val exerciseCals = confirmed.filter { it.totalCalories < 0 }.sumOf { Math.abs(it.totalCalories) }

        val carbs = confirmed.sumOf { it.totalCarbs }
        val protein = confirmed.sumOf { it.totalProtein }
        val fat = confirmed.sumOf { it.totalFat }

        val remaining = (profile.targetCalories + exerciseCals) - foodCals

        SummaryMetrics(
            foodCalories = foodCals,
            exerciseCalories = exerciseCals,
            targetCalories = profile.targetCalories,
            remainingCalories = remaining,
            carbsGrams = carbs,
            targetCarbsGrams = profile.targetCarbs,
            proteinGrams = protein,
            targetProteinGrams = profile.targetProtein,
            fatGrams = fat,
            targetFatGrams = profile.targetFat
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SummaryMetrics()
    )

    // Generates 7 days strip ending at Today (Today is index 6, rightmost!)
    val weekDaysStrip: StateFlow<DaysOfWeekState> = combine(
        selectedDate,
        loggedDatesSet
    ) { dateStr, loggedSet ->
        val list = mutableListOf<DateInfo>()
        val cal = Calendar.getInstance()
        try {
            val d = dateFormatter.parse(todayDateStr)
            if (d != null) cal.time = d
        } catch (e: Exception) {
            // fallback
        }

        cal.add(Calendar.DAY_OF_MONTH, -6)
        for (i in 0..6) {
            val curStr = dateFormatter.format(cal.time)
            val dayOfWeek = dayNameFormatter.format(cal.time)
            val dayNum = dayNumFormatter.format(cal.time)
            val isSelected = (curStr == dateStr)
            val isToday = (curStr == todayDateStr)
            val hasMeal = loggedSet.contains(curStr)

            list.add(
                DateInfo(
                    dateString = curStr,
                    dayOfWeek = dayOfWeek,
                    dayOfMonth = dayNum,
                    isToday = isToday,
                    isSelected = isSelected,
                    hasMealEntries = hasMeal
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        DaysOfWeekState(list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DaysOfWeekState(emptyList())
    )

    fun selectDate(dateStr: String) {
        selectedDate.value = dateStr
    }

    fun resetToToday() {
        selectedDate.value = todayDateStr
        selectedMonth.value = SimpleDateFormat("MMM", Locale.US).format(Date())
        isCalendarExpanded.value = false
    }

    fun incrementWater() {
        viewModelScope.launch {
            val cur = waterLogForSelectedDate.value
            val updated = cur.copy(cups = cur.cups + 1)
            repository.updateWaterLog(updated)
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            val cur = waterLogForSelectedDate.value
            if (cur.cups > 0) {
                val updated = cur.copy(cups = cur.cups - 1)
                repository.updateWaterLog(updated)
            }
        }
    }

    fun processInputAndLog(promptOverride: String? = null) {
        val textToProcess = promptOverride ?: inputText.value.trim()
        if (textToProcess.isBlank()) return

        val nowTime = timeFormatter.format(Date())
        val promptText = textToProcess.ifBlank { "Scanned Meal" }

        inputText.value = ""
        isProcessingAi.value = true

        viewModelScope.launch {
            // Step 1: Insert pending meal entry with isAnalyzing = true so it displays in list immediately
            val pendingEntry = MealEntry(
                date = selectedDate.value,
                originalPrompt = promptText,
                formattedTime = nowTime,
                itemsJson = "",
                totalCalories = 0,
                totalCarbs = 0,
                totalProtein = 0,
                totalFat = 0,
                isSaved = false,
                isConfirmed = true,
                isAnalyzing = true
            )

            val pendingId = repository.insertMealEntry(pendingEntry)

            val startTime = System.currentTimeMillis()

            // Step 2: Analyze meal with Gemini AI
            val result = geminiAiService.processMultiModalInput(
                prompt = textToProcess
            )

            // Ensure a minimum delay of 1.8 seconds for natural processing perception
            val elapsedTime = System.currentTimeMillis() - startTime
            val minProcessingTimeMs = 1800L
            if (elapsedTime < minProcessingTimeMs) {
                delay(minProcessingTimeMs - elapsedTime)
            }

            val json = SmartNutritionParser.toJson(result.items)

            // Step 3: Update entry with parsed breakdown and totals
            val analyzedEntry = pendingEntry.copy(
                id = pendingId,
                itemsJson = json,
                totalCalories = result.totalCalories,
                totalCarbs = result.totalCarbs,
                totalProtein = result.totalProtein,
                totalFat = result.totalFat,
                isAnalyzing = false
            )

            repository.updateMealEntry(analyzedEntry)
            isProcessingAi.value = false
        }
    }

    fun confirmQuickEntry(entry: MealEntry) {
        viewModelScope.launch {
            val updated = entry.copy(isConfirmed = true)
            repository.updateMealEntry(updated)
            confirmationEntry.value = null
        }
    }

    fun cancelQuickEntry(entry: MealEntry) {
        viewModelScope.launch {
            repository.deleteMealEntry(entry.id)
            confirmationEntry.value = null
        }
    }

    fun openEntryOptions(entry: MealEntry) {
        activeOptionEntry.value = entry
    }

    fun closeEntryOptions() {
        activeOptionEntry.value = null
    }

    fun deleteActiveOptionEntry() {
        val target = activeOptionEntry.value ?: return
        viewModelScope.launch {
            repository.deleteMealEntry(target.id)
            activeOptionEntry.value = null
        }
    }

    fun saveActiveOptionEntryToFavorites() {
        val target = activeOptionEntry.value ?: return
        viewModelScope.launch {
            repository.addSavedEntry(target.originalPrompt)
            val updated = target.copy(isSaved = true)
            repository.updateMealEntry(updated)
            activeOptionEntry.value = null
        }
    }

    fun openEditEntryDialog(entry: MealEntry) {
        editEntryDialogTarget.value = entry
        activeOptionEntry.value = null
    }

    fun saveEditedEntry(entry: MealEntry, newPrompt: String, newCals: Int, newCarbs: Int, newProtein: Int, newFat: Int) {
        viewModelScope.launch {
            val result = SmartNutritionParser.parsePrompt(newPrompt)
            val json = SmartNutritionParser.toJson(result.items)
            val updated = entry.copy(
                originalPrompt = newPrompt,
                itemsJson = json,
                totalCalories = newCals,
                totalCarbs = newCarbs,
                totalProtein = newProtein,
                totalFat = newFat
            )
            repository.updateMealEntry(updated)
            editEntryDialogTarget.value = null
        }
    }

    fun logSavedOrRecentEntry(promptText: String) {
        isSavedEntriesSheetOpen.value = false
        processInputAndLog(promptText)
    }

    val selectedMealEntryForEdit = MutableStateFlow<MealEntry?>(null)
    val showFeedbackDialog = MutableStateFlow(false)

    fun reanalyzeMealEntry(entry: MealEntry, newPrompt: String) {
        if (newPrompt.trim() == entry.originalPrompt.trim()) return

        viewModelScope.launch {
            // Set analyzing state
            val analyzingEntry = entry.copy(
                originalPrompt = newPrompt,
                isAnalyzing = true
            )
            repository.updateMealEntry(analyzingEntry)

            val startTime = System.currentTimeMillis()
            val result = geminiAiService.processMultiModalInput(prompt = newPrompt)

            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 1500L) {
                delay(1500L - elapsedTime)
            }

            val json = SmartNutritionParser.toJson(result.items)
            val updatedEntry = analyzingEntry.copy(
                itemsJson = json,
                totalCalories = result.totalCalories,
                totalCarbs = result.totalCarbs,
                totalProtein = result.totalProtein,
                totalFat = result.totalFat,
                isAnalyzing = false
            )

            repository.updateMealEntry(updatedEntry)
        }
    }

    fun updateMealMacros(entry: MealEntry, isFood: Boolean, calories: Int, carbs: Int, protein: Int, fat: Int) {
        viewModelScope.launch {
            val updated = entry.copy(
                totalCalories = calories,
                totalCarbs = carbs,
                totalProtein = protein,
                totalFat = fat
            )
            repository.updateMealEntry(updated)
            selectedMealEntryForEdit.value = null
        }
    }

    fun updateMealDateTime(entry: MealEntry, newDate: String, newTime: String) {
        viewModelScope.launch {
            val updated = entry.copy(
                date = newDate,
                formattedTime = newTime
            )
            repository.updateMealEntry(updated)
            selectedMealEntryForEdit.value = null
        }
    }

    fun updateGoals(targetCals: Int, carbs: Int, protein: Int, fat: Int) {
        viewModelScope.launch {
            val cur = userProfile.value
            repository.saveProfile(
                cur.copy(
                    targetCalories = targetCals,
                    targetCarbs = carbs,
                    targetProtein = protein,
                    targetFat = fat
                )
            )
        }
    }
}
