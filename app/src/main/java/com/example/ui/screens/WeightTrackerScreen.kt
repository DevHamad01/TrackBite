package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    userProfile: UserProfile,
    weightLogs: List<WeightLog>,
    onAddWeightLog: (Float, String) -> Unit,
    onDeleteWeightLog: (Long) -> Unit,
    onUpdateTargetWeight: (Float) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBlue = GreenPrimary
    val targetGreen = GreenPrimary
    val chartGridGray = Color(0xFFE5E7EB)

    val currentWeightKg = weightLogs.firstOrNull()?.weightKg ?: 70f
    var targetWeightKg by remember(userProfile) { mutableIntStateOf(75) }
    var isEditTargetDialogOpen by remember { mutableStateOf(false) }
    var targetInputText by remember { mutableStateOf(targetWeightKg.toString()) }

    var selectedFilterTab by remember { mutableStateOf("Week") } // "Week", "Month", "Year", "All time"
    val filterTabs = listOf("Week", "Month", "Year", "All time")

    // Dialog & Picker States
    var isAddWeightDialogOpen by remember { mutableStateOf(false) }
    var inputWeightText by remember { mutableStateOf(currentWeightKg.toInt().toString()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    var isDatePickerDialogOpen by remember { mutableStateOf(false) }
    var isTimePickerDialogOfOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weight",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                        inputWeightText = currentWeightKg.toInt().toString()
                        selectedDate = LocalDate.now()
                        selectedTime = LocalTime.now()
                        isAddWeightDialogOpen = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Weight Entry",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.White,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cards Row: Current Weight & Target Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Weight Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Current Weight",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${"%.1f".format(currentWeightKg)} kg",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Target Weight Card (Clickable to edit target)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            targetInputText = targetWeightKg.toString()
                            isEditTargetDialogOpen = true
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Target Weight",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Target",
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$targetWeightKg kg",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Filter Tabs (Week, Month, Year, All time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(6.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterTabs.forEachIndexed { index, tabName ->
                    val isSelected = selectedFilterTab == tabName
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                color = if (isSelected) buttonBlue else Color.Transparent,
                                shape = when (index) {
                                    0 -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
                                    filterTabs.size - 1 -> RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                            )
                            .clickable { selectedFilterTab = tabName }
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else buttonBlue
                        )
                    }
                }
            }

            // Weight Graph Card - Dynamic Canvas Chart
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val logsAsc = remember(weightLogs) { weightLogs.sortedBy { it.timestamp } }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (logsAsc.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No weight logs recorded yet.\nTap '+' to log your weight.",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val minWeight = remember(logsAsc, targetWeightKg) {
                                (minOf(logsAsc.minOf { it.weightKg }, targetWeightKg.toFloat()) - 5f).coerceAtLeast(0f)
                            }
                            val maxWeight = remember(logsAsc, targetWeightKg) {
                                (maxOf(logsAsc.maxOf { it.weightKg }, targetWeightKg.toFloat()) + 5f)
                            }

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                val paddingLeft = 45.dp.toPx()
                                val paddingBottom = 30.dp.toPx()
                                val paddingTop = 20.dp.toPx()
                                val paddingRight = 20.dp.toPx()

                                val chartWidth = width - paddingLeft - paddingRight
                                val chartHeight = height - paddingTop - paddingBottom

                                // Draw horizontal grid lines
                                val gridSteps = 4
                                for (i in 0..gridSteps) {
                                    val yRatio = i.toFloat() / gridSteps
                                    val yPos = height - paddingBottom - (yRatio * chartHeight)
                                    drawLine(
                                        color = chartGridGray,
                                        start = Offset(paddingLeft, yPos),
                                        end = Offset(width - paddingRight, yPos),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Target weight line
                                val targetYRatio = ((targetWeightKg - minWeight) / (maxWeight - minWeight)).coerceIn(0f, 1f)
                                val targetYPos = height - paddingBottom - (targetYRatio * chartHeight)
                                drawLine(
                                    color = targetGreen,
                                    start = Offset(paddingLeft, targetYPos),
                                    end = Offset(width - paddingRight, targetYPos),
                                    strokeWidth = 2.dp.toPx()
                                )

                                // Plot solid line connecting all user logged weight points
                                if (logsAsc.isNotEmpty()) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    val points = mutableListOf<Offset>()

                                    logsAsc.forEachIndexed { index, log ->
                                        val xPos = if (logsAsc.size == 1) {
                                            paddingLeft + chartWidth / 2f
                                        } else {
                                            paddingLeft + (index.toFloat() / (logsAsc.size - 1)) * chartWidth
                                        }
                                        val wRatio = ((log.weightKg - minWeight) / (maxWeight - minWeight)).coerceIn(0f, 1f)
                                        val yPos = height - paddingBottom - (wRatio * chartHeight)

                                        val pt = Offset(xPos, yPos)
                                        points.add(pt)
                                        if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                                    }

                                    // Draw connecting line
                                    drawPath(
                                        path = path,
                                        color = buttonBlue,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Draw node dots
                                    points.forEach { pt ->
                                        drawCircle(
                                            color = buttonBlue,
                                            radius = 5.5.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 2.5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            // Dynamic Y-Axis Labels
                            val stepVal = (maxWeight - minWeight) / 4f
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 28.dp, top = 16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                (4 downTo 0).forEach { i ->
                                    val labelVal = (minWeight + i * stepVal).toInt()
                                    Text(
                                        text = "$labelVal",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.width(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weight Entries Title
            Text(
                text = "Weight Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Weight Entries List from Room DB
            if (weightLogs.isEmpty()) {
                Text(
                    text = "No entries yet. Add your weight above!",
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                weightLogs.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAF8))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${"%.1f".format(entry.weightKg)} kg",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = entry.dateString,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(onClick = { onDeleteWeightLog(entry.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete weight entry",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Target Weight Dialog
    if (isEditTargetDialogOpen) {
        Dialog(onDismissRequest = { isEditTargetDialogOpen = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Set Target Weight (kg)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = targetInputText,
                        onValueChange = { targetInputText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditTargetDialogOpen = false }) {
                            Text(text = "Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val newTarget = targetInputText.toFloatOrNull() ?: 75f
                                targetWeightKg = newTarget.toInt()
                                onUpdateTargetWeight(newTarget)
                                isEditTargetDialogOpen = false
                            }
                        ) {
                            Text(text = "Save", color = buttonBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Weight Entry Dialog
    if (isAddWeightDialogOpen) {
        Dialog(onDismissRequest = { isAddWeightDialogOpen = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "New Weight",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inputWeightText,
                        onValueChange = { inputWeightText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Weight in kg") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDatePickerDialogOpen = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Date", fontSize = 14.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date",
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                    // Time Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTimePickerDialogOfOpen = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Time", fontSize = 14.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select Time",
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAddWeightDialogOpen = false }) {
                            Text(text = "Cancel", fontSize = 15.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val weightVal = inputWeightText.toFloatOrNull() ?: 70f
                                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                                onAddWeightLog(weightVal, selectedDate.format(dateFormatter))
                                isAddWeightDialogOpen = false
                            }
                        ) {
                            Text(text = "Save", fontSize = 15.sp, color = buttonBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog for Weight Log
    if (isDatePickerDialogOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { isDatePickerDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        isDatePickerDialogOpen = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog for Weight Log
    if (isTimePickerDialogOfOpen) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        Dialog(onDismissRequest = { isTimePickerDialogOfOpen = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Select Time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isTimePickerDialogOfOpen = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                isTimePickerDialogOfOpen = false
                            }
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
