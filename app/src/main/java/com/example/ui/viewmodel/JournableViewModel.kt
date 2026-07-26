package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FoodItem
import com.example.data.model.MealEntry
import com.example.data.model.SavedEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.ai.GeminiAiService
import com.example.data.model.ai.SmartNutritionParser
import com.example.data.repository.JournableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class JournableViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = JournableRepository(
        mealEntryDao = db.mealEntryDao(),
        waterLogDao = db.waterLogDao(),
        userProfileDao = db.userProfileDao(),
        savedEntryDao = db.savedEntryDao()
    )
    private val geminiAiService = GeminiAiService(application)

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val dayNameFormatter = SimpleDateFormat("EEE", Locale.US)
    private val dayNumFormatter = SimpleDateFormat("dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    val todayDateStr: String = "28 Mar 2026"
    val yesterdayDateStr: String = "27 Mar 2026"

    // Current Selected Date e.g. "28 Mar 2026"
    val selectedDate = MutableStateFlow("28 Mar 2026")

    val loggedDatesSet: StateFlow<Set<String>> = repository.getLoggedDates()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf("28 Mar 2026")
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

    // UI Overlay States
    val isCalendarExpanded = MutableStateFlow(false)
    val isSavedEntriesSheetOpen = MutableStateFlow(false)
    val activeOptionEntry = MutableStateFlow<MealEntry?>(null) // State 1: Entry Options Modal
    val confirmationEntry = MutableStateFlow<MealEntry?>(null) // State 3: Quick Confirmation Card
    val editEntryDialogTarget = MutableStateFlow<MealEntry?>(null)
    val isPremiumModalOpen = MutableStateFlow(false)
    val isDrawerOpen = MutableStateFlow(false)

    // Logging Input States
    val inputText = MutableStateFlow("")
    val isProcessingAi = MutableStateFlow(false)
    val selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val isRecordingVoice = MutableStateFlow(false)

    // Month Grid Filter in Calendar Sheet
    val selectedMonth = MutableStateFlow("Mar")

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            val date = "28 Mar 2026"

            // Ensure profile exists
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

            // Seed initial water log for 28 Mar 2026
            repository.updateWaterLog(
                WaterLog(
                    date = date,
                    cups = 4,
                    targetCups = 8
                )
            )

            // Seed initial recent entries
            val recents = listOf(
                "2 chicken paratha with raita and 1 cup chai",
                "2 parathe with full fry and 1 chicken shami",
                "1 chocolate sunfae",
                "double patty beef burger with fries"
            )
            for (p in recents) {
                repository.addSavedEntry(p)
            }

            // Seed initial logged meals for 28 Mar 2026 if empty
            val item1 = listOf(
                FoodItem("Paratha", "1 paratha", 200, 30, 5, 7),
                FoodItem("Daal", "1 plate", 180, 25, 12, 3),
                FoodItem("Tea", "1 cup", 40, 5, 1, 1)
            )
            val json1 = SmartNutritionParser.toJson(item1)

            val entry1 = MealEntry(
                date = date,
                originalPrompt = "1 paratha 1 plate daal with tea",
                formattedTime = "17:12",
                itemsJson = json1,
                totalCalories = 420,
                totalCarbs = 60,
                totalProtein = 18,
                totalFat = 11,
                isSaved = false,
                isConfirmed = true
            )

            val item2 = listOf(
                FoodItem("Tea Cake", "2 tea cakes", 300, 50, 6, 10)
            )
            val json2 = SmartNutritionParser.toJson(item2)

            val entry2 = MealEntry(
                date = date,
                originalPrompt = "i eat 2 tea Cake",
                formattedTime = "02:14",
                itemsJson = json2,
                totalCalories = 300,
                totalCarbs = 50,
                totalProtein = 6,
                totalFat = 10,
                isSaved = false,
                isConfirmed = true
            )

            repository.insertMealEntry(entry1)
            repository.insertMealEntry(entry2)
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
            initialValue = WaterLog("28 Mar 2026", cups = 4, targetCups = 8)
        )

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
        val confirmed = entries.filter { it.isConfirmed }
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
        selectedMonth.value = "Mar"
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
        if (textToProcess.isBlank() && selectedImageBitmap.value == null) return

        isProcessingAi.value = true
        viewModelScope.launch {
            val result = geminiAiService.processMultiModalInput(
                prompt = textToProcess,
                imageBitmap = selectedImageBitmap.value
            )

            val json = SmartNutritionParser.toJson(result.items)
            val nowTime = timeFormatter.format(Date())

            val entry = MealEntry(
                date = selectedDate.value,
                originalPrompt = textToProcess.ifBlank { "Scanned Meal" },
                formattedTime = nowTime,
                itemsJson = json,
                totalCalories = result.totalCalories,
                totalCarbs = result.totalCarbs,
                totalProtein = result.totalProtein,
                totalFat = result.totalFat,
                isSaved = false,
                isConfirmed = false // Opens State 3: Quick Confirmation Card
            )

            val newId = repository.insertMealEntry(entry)
            val insertedEntry = entry.copy(id = newId)

            confirmationEntry.value = insertedEntry
            inputText.value = ""
            selectedImageBitmap.value = null
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
