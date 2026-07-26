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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
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
import com.example.ui.theme.CardBorder
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

data class WeightEntryItem(
    val id: Long = System.currentTimeMillis(),
    val weightKg: Float,
    val dateTime: LocalDateTime
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBlue = Color(0xFF2B5B84)
    val targetGreen = Color(0xFF388E3C)
    val chartGridGray = Color(0xFFE5E7EB)

    // Initial weight entry list matching user screenshots
    val weightEntries = remember {
        mutableStateListOf(
            WeightEntryItem(
                id = 1,
                weightKg = 50f,
                dateTime = LocalDateTime.of(2026, 7, 25, 23, 24)
            ),
            WeightEntryItem(
                id = 2,
                weightKg = 50f,
                dateTime = LocalDateTime.of(2026, 3, 28, 2, 12)
            ),
            WeightEntryItem(
                id = 3,
                weightKg = 50f,
                dateTime = LocalDateTime.of(2026, 3, 28, 2, 11)
            )
        )
    }

    var targetWeightKg by remember { mutableIntStateOf(75) }
    var selectedFilterTab by remember { mutableStateOf("Week") } // "Week", "Month", "Year", "All time"
    val filterTabs = listOf("Week", "Month", "Year", "All time")

    // Current Weight is the latest entry
    val currentWeightKg = weightEntries.firstOrNull()?.weightKg ?: 50f

    // Dialog & Picker States
    var isAddWeightDialogOpen by remember { mutableStateOf(false) }
    var inputWeightText by remember { mutableStateOf("55") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(1, 45)) }

    var isDatePickerDialogOpen by remember { mutableStateOf(false) }
    var isTimePickerDialogOfOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weight Tracker",
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
                            text = "${currentWeightKg.toInt()} kg",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Target Weight Card
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
                            text = "Target Weight",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$targetWeightKg kg",
                            fontSize = 26.sp,
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

            // Weight Graph Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Canvas Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height

                            val paddingLeft = 40.dp.toPx()
                            val paddingBottom = 30.dp.toPx()
                            val paddingTop = 20.dp.toPx()
                            val paddingRight = 10.dp.toPx()

                            val chartWidth = width - paddingLeft - paddingRight
                            val chartHeight = height - paddingTop - paddingBottom

                            // Y Axis Values: 45, 55, 65, 75
                            val yValues = listOf(45, 55, 65, 75)
                            val minY = 45f
                            val maxY = 75f

                            // Draw Y Grid lines and Y Labels
                            yValues.forEachIndexed { idx, value ->
                                val yRatio = (value - minY) / (maxY - minY)
                                val yPos = height - paddingBottom - (yRatio * chartHeight)

                                // Horizontal grid line
                                drawLine(
                                    color = chartGridGray,
                                    start = Offset(paddingLeft, yPos),
                                    end = Offset(width - paddingRight, yPos),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Target weight dashed green line at 75 kg
                            val targetYPos = height - paddingBottom - chartHeight // at 75
                            drawLine(
                                color = targetGreen,
                                start = Offset(paddingLeft, targetYPos),
                                end = Offset(width - paddingRight, targetYPos),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )

                            // Actual Weight Solid Blue Line at 50 kg
                            val currentYPos = height - paddingBottom - ((50f - minY) / (maxY - minY) * chartHeight)
                            drawLine(
                                color = buttonBlue,
                                start = Offset(paddingLeft, currentYPos),
                                end = Offset(width - paddingRight, currentYPos),
                                strokeWidth = 2.5.dp.toPx()
                            )

                            // End point node dot on solid blue line
                            val endX = width - paddingRight
                            drawCircle(
                                color = buttonBlue,
                                radius = 5.dp.toPx(),
                                center = Offset(endX, currentYPos)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(endX, currentYPos)
                            )

                            // Vertical grid lines
                            val numXLines = 4
                            for (i in 0 until numXLines) {
                                val xPos = paddingLeft + (i.toFloat() / (numXLines - 1)) * chartWidth
                                drawLine(
                                    color = chartGridGray,
                                    start = Offset(xPos, paddingTop),
                                    end = Offset(xPos, height - paddingBottom),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        // Y-Axis Labels Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 30.dp, top = 10.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(75, 65, 55, 45).forEach { labelVal ->
                                Text(
                                    text = "$labelVal",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.width(32.dp)
                                )
                            }
                        }

                        // X-Axis Labels Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(start = 40.dp, end = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Jul 18", "20", "22", "24").forEach { dateLabel ->
                                Text(
                                    text = dateLabel,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
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

            // Weight Entries List
            weightEntries.forEach { entry ->
                WeightEntryRow(
                    entry = entry,
                    onClick = {
                        inputWeightText = entry.weightKg.toInt().toString()
                        selectedDate = entry.dateTime.toLocalDate()
                        selectedTime = entry.dateTime.toLocalTime()
                        isAddWeightDialogOpen = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add / Edit Weight Entry Dialog (Matching User Reference Screenshot 3)
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
                        text = "Weight",
                        fontSize = 13.sp,
                        color = buttonBlue,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputWeightText,
                            onValueChange = { inputWeightText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "kg",
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    HorizontalDivider(color = buttonBlue, thickness = 2.dp)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Date Selector Field
                    Text(
                        text = "Date",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDatePickerDialogOpen = true }
                            .padding(vertical = 8.dp)
                    ) {
                        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
                        Text(
                            text = selectedDate.format(dateFormatter),
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }
                    HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Selector Field
                    Text(
                        text = "Time",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTimePickerDialogOfOpen = true }
                            .padding(vertical = 8.dp)
                    ) {
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
                        Text(
                            text = selectedTime.format(timeFormatter),
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }
                    HorizontalDivider(color = Color(0xFFD0D0D0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons (Cancel, Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isAddWeightDialogOpen = false }) {
                            Text(text = "Cancel", fontSize = 15.sp, color = buttonBlue, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val weightVal = inputWeightText.toFloatOrNull() ?: 50f
                                val newEntry = WeightEntryItem(
                                    weightKg = weightVal,
                                    dateTime = LocalDateTime.of(selectedDate, selectedTime)
                                )
                                weightEntries.add(0, newEntry)
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

    // Material 3 Date Picker Dialog (Matching User Reference Screenshots 4 & 5)
    if (isDatePickerDialogOpen) {
        var isTextInputMode by remember { mutableStateOf(false) }

        if (isTextInputMode) {
            // Text Input Mode Date Dialog (Matching Screenshot 5)
            var textDateInput by remember { mutableStateOf("") }
            Dialog(onDismissRequest = { isDatePickerDialogOpen = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF2F4F8),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Select date", fontSize = 14.sp, color = TextMuted)
                            IconButton(onClick = { isTextInputMode = false }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Switch to Calendar",
                                    tint = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = textDateInput,
                            onValueChange = { textDateInput = it },
                            label = { Text("Enter date") },
                            placeholder = { Text("dd/mm/yyyy") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = buttonBlue,
                                unfocusedBorderColor = TextMuted
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isDatePickerDialogOpen = false }) {
                                Text("Cancel", color = buttonBlue)
                            }
                            TextButton(onClick = {
                                // Parse date if format dd/MM/yyyy
                                try {
                                    val parts = textDateInput.split("/")
                                    if (parts.size == 3) {
                                        val day = parts[0].toInt()
                                        val month = parts[1].toInt()
                                        val year = parts[2].toInt()
                                        selectedDate = LocalDate.of(year, month, day)
                                    }
                                } catch (_: Exception) {}
                                isDatePickerDialogOpen = false
                            }) {
                                Text("OK", color = buttonBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Calendar Graphical Mode Date Dialog (Matching Screenshot 4)
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { isDatePickerDialogOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        isDatePickerDialogOpen = false
                    }) {
                        Text("OK", color = buttonBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isDatePickerDialogOpen = false }) {
                        Text("Cancel", color = buttonBlue)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = Color.White)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true
                )
            }
        }
    }

    // Material 3 Time Picker Dialog (Matching User Reference Screenshots 2 & 6)
    if (isTimePickerDialogOfOpen) {
        var isKeyboardMode by remember { mutableStateOf(false) }
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        Dialog(onDismissRequest = { isTimePickerDialogOfOpen = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFEEF2F6),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isKeyboardMode) "Enter time" else "Select time",
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 16.dp)
                    )

                    if (isKeyboardMode) {
                        TimeInput(state = timePickerState)
                    } else {
                        TimePicker(state = timePickerState)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isKeyboardMode = !isKeyboardMode }) {
                            Icon(
                                imageVector = if (isKeyboardMode) Icons.Default.Schedule else Icons.Default.Keyboard,
                                contentDescription = "Toggle Input Mode",
                                tint = TextPrimary
                            )
                        }

                        Row {
                            TextButton(onClick = { isTimePickerDialogOfOpen = false }) {
                                Text("Cancel", color = buttonBlue)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                isTimePickerDialogOfOpen = false
                            }) {
                                Text("OK", color = buttonBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightEntryRow(
    entry: WeightEntryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy HH:mm", Locale.ENGLISH)
    val dateString = entry.dateTime.format(formatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "${entry.weightKg.toInt()} kg",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateString,
            fontSize = 14.sp,
            color = TextMuted
        )
    }
    HorizontalDivider(color = Color(0xFFF0F0F0))
}
