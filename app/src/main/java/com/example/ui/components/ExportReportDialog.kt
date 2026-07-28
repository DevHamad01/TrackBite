package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntry
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportDialog(
    userProfile: UserProfile,
    mealEntries: List<MealEntry> = emptyList(),
    weightLogs: List<WeightLog> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var datePreset by remember { mutableStateOf("This Week") } // "This Week", "Last 30 Days", "This Month", "Custom"
    
    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(today.minusDays(7)) }
    var endDate by remember { mutableStateOf(today) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var includeFood by remember { mutableStateOf(true) }
    var includeExercise by remember { mutableStateOf(true) }
    var includeWeight by remember { mutableStateOf(true) }
    var includeNutrientsSummary by remember { mutableStateOf(true) }

    var selectedFormat by remember { mutableStateOf("PDF") } // "PDF" or "CSV"
    var isGenerating by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        modifier = modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        dismissButton = {},
        text = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF Report",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Export Report",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Generate downloadable summary",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, enabled = !isGenerating) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Date Range Presets
                    Text(
                        text = "Date Range",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf("This Week", "Last 30 Days", "This Month", "Custom")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = datePreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GreenPrimary else Color(0xFFF2F4F7))
                                    .clickable {
                                        datePreset = preset
                                        when (preset) {
                                            "This Week" -> {
                                                startDate = today.minusDays(7)
                                                endDate = today
                                            }
                                            "Last 30 Days" -> {
                                                startDate = today.minusDays(30)
                                                endDate = today
                                            }
                                            "This Month" -> {
                                                startDate = today.withDayOfMonth(1)
                                                endDate = today
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date range picker input display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start date
                        OutlinedTextField(
                            value = startDate.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("From", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    datePreset = "Custom"
                                    showStartDatePicker = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select Start Date",
                                        tint = GreenPrimary
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    datePreset = "Custom"
                                    showStartDatePicker = true
                                }
                        )

                        // End date
                        OutlinedTextField(
                            value = endDate.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    datePreset = "Custom"
                                    showEndDatePicker = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Select End Date",
                                        tint = GreenPrimary
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    datePreset = "Custom"
                                    showEndDatePicker = true
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Data Types Selection
                    Text(
                        text = "Data to Include",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        ExportCheckboxRow(
                            title = "Food Entries & Calories",
                            subtitle = "Logged meals, dishes, and nutrient breakdown",
                            checked = includeFood,
                            onCheckedChange = { includeFood = it }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        ExportCheckboxRow(
                            title = "Exercise & Workouts",
                            subtitle = "Calorie burn activities and duration",
                            checked = includeExercise,
                            onCheckedChange = { includeExercise = it }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        ExportCheckboxRow(
                            title = "Weight Tracking Logs",
                            subtitle = "Weight progression history & goals",
                            checked = includeWeight,
                            onCheckedChange = { includeWeight = it }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        ExportCheckboxRow(
                            title = "Nutrient & Macro Summary",
                            subtitle = "Carbs, Protein, and Fat statistics",
                            checked = includeNutrientsSummary,
                            onCheckedChange = { includeNutrientsSummary = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Format Selection
                    Text(
                        text = "Export Format",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selectedFormat == "PDF") 2.dp else 1.dp,
                                    color = if (selectedFormat == "PDF") GreenPrimary else CardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedFormat = "PDF" }
                                .padding(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedFormat == "PDF",
                                onClick = { selectedFormat = "PDF" },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("PDF Document", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Printable report", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selectedFormat == "CSV") 2.dp else 1.dp,
                                    color = if (selectedFormat == "CSV") GreenPrimary else CardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedFormat = "CSV" }
                                .padding(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedFormat == "CSV",
                                onClick = { selectedFormat = "CSV" },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("CSV Spreadsheet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Excel data", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isGenerating,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextMuted, fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                isGenerating = true
                                coroutineScope.launch {
                                    val file = generateReportFile(
                                        context = context,
                                        userProfile = userProfile,
                                        mealEntries = mealEntries,
                                        weightLogs = weightLogs,
                                        startDate = startDate,
                                        endDate = endDate,
                                        includeFood = includeFood,
                                        includeExercise = includeExercise,
                                        includeWeight = includeWeight,
                                        includeNutrientsSummary = includeNutrientsSummary,
                                        isPdf = selectedFormat == "PDF"
                                    )
                                    isGenerating = false

                                    if (file != null) {
                                        shareOrOpenFile(context, file, selectedFormat)
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Error generating report file", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isGenerating && (includeFood || includeExercise || includeWeight || includeNutrientsSummary),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...", fontSize = 14.sp, color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export $selectedFormat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    )

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) {
                    Text("Select", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndDatePicker = false
                }) {
                    Text("Select", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ExportCheckboxRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

private suspend fun generateReportFile(
    context: Context,
    userProfile: UserProfile,
    mealEntries: List<MealEntry>,
    weightLogs: List<WeightLog>,
    startDate: LocalDate,
    endDate: LocalDate,
    includeFood: Boolean,
    includeExercise: Boolean,
    includeWeight: Boolean,
    includeNutrientsSummary: Boolean,
    isPdf: Boolean
): File? = withContext(Dispatchers.IO) {
    try {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) exportsDir.mkdirs()

        val filename = "TrackBite_Report_${startDate}_to_${endDate}.${if (isPdf) "pdf" else "csv"}"
        val outputFile = File(exportsDir, filename)

        if (isPdf) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.parseColor("#128C7E")
                textSize = 22f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 12f
            }

            val sectionPaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 15f
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 11f
            }

            val linePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 1f
            }

            var y = 50f

            // Header
            canvas.drawText("TrackBite Nutrition & Health Report", 40f, y, titlePaint)
            y += 20f
            canvas.drawText("Date Range: $startDate to $endDate  |  Generated for: User", 40f, y, subtitlePaint)
            y += 15f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            // Summary box
            canvas.drawText("Profile Summary:", 40f, y, sectionPaint)
            y += 18f
            canvas.drawText("Daily Target: ${userProfile.targetCalories} kcal  |  Carbs: ${userProfile.targetCarbs}g  |  Protein: ${userProfile.targetProtein}g  |  Fat: ${userProfile.targetFat}g", 40f, y, textPaint)
            y += 25f

            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

            // Meals section
            if (includeFood || includeExercise) {
                canvas.drawText("Logged Entries (Meals & Workouts):", 40f, y, sectionPaint)
                y += 18f

                // Table Header
                canvas.drawText("Date", 40f, y, sectionPaint.apply { textSize = 11f })
                canvas.drawText("Name / Food", 120f, y, sectionPaint)
                canvas.drawText("Calories", 320f, y, sectionPaint)
                canvas.drawText("Carbs", 400f, y, sectionPaint)
                canvas.drawText("Protein", 460f, y, sectionPaint)
                canvas.drawText("Fat", 510f, y, sectionPaint)
                y += 8f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 16f

                val filteredMeals = mealEntries.filter { meal ->
                    try {
                        val parsedDate = LocalDate.parse(meal.date, dateFormatter)
                        !parsedDate.isBefore(startDate) && !parsedDate.isAfter(endDate)
                    } catch (e: Exception) {
                        true
                    }
                }

                if (filteredMeals.isEmpty()) {
                    canvas.drawText("No entries recorded for this date range.", 40f, y, textPaint)
                    y += 20f
                } else {
                    filteredMeals.take(20).forEach { meal ->
                        if (y > 780f) return@forEach // Basic safety limit for page 1
                        canvas.drawText(meal.date, 40f, y, textPaint)
                        canvas.drawText(meal.originalPrompt.take(22), 120f, y, textPaint)
                        canvas.drawText("${meal.totalCalories} kcal", 320f, y, textPaint)
                        canvas.drawText("${meal.totalCarbs}g", 400f, y, textPaint)
                        canvas.drawText("${meal.totalProtein}g", 460f, y, textPaint)
                        canvas.drawText("${meal.totalFat}g", 510f, y, textPaint)
                        y += 16f
                    }
                }
                y += 20f
            }

            // Weight Logs section
            if (includeWeight) {
                canvas.drawText("Weight Logs:", 40f, y, sectionPaint.apply { textSize = 15f })
                y += 18f

                val filteredWeights = weightLogs.filter { log ->
                    try {
                        val parsedDate = LocalDate.parse(log.dateString, dateFormatter)
                        !parsedDate.isBefore(startDate) && !parsedDate.isAfter(endDate)
                    } catch (e: Exception) {
                        true
                    }
                }

                if (filteredWeights.isEmpty()) {
                    canvas.drawText("No weight records in this date range.", 40f, y, textPaint)
                    y += 20f
                } else {
                    filteredWeights.take(10).forEach { log ->
                        canvas.drawText("${log.dateString}: ${log.weightKg} kg", 40f, y, textPaint)
                        y += 16f
                    }
                }
            }

            // Footer
            canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
            canvas.drawText("TrackBite Smart Nutrition Assistant - Report generated on ${LocalDate.now()}", 40f, 818f, subtitlePaint)

            pdfDocument.finishPage(page)

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            outputFile
        } else {
            // CSV Export
            FileOutputStream(outputFile).use { out ->
                val writer = out.bufferedWriter()
                writer.write("Type,Date,Name,Calories,Carbs(g),Protein(g),Fat(g)\n")

                if (includeFood || includeExercise) {
                    mealEntries.forEach { meal ->
                        writer.write("Meal,${meal.date},\"${meal.originalPrompt}\",${meal.totalCalories},${meal.totalCarbs},${meal.totalProtein},${meal.totalFat}\n")
                    }
                }

                if (includeWeight) {
                    weightLogs.forEach { log ->
                        writer.write("Weight,${log.dateString},Weight Record,${log.weightKg},,, \n")
                    }
                }
                writer.flush()
            }
            outputFile
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun shareOrOpenFile(context: Context, file: File, format: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (format == "PDF") "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "TrackBite Health & Nutrition Export")
            putExtra(Intent.EXTRA_TEXT, "Attached is your exported TrackBite report.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share TrackBite Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Exported file saved to: ${file.name}", Toast.LENGTH_LONG).show()
    }
}
