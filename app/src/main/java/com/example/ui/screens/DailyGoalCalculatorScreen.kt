package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalCalculatorScreen(
    onBackClick: () -> Unit,
    onFinish: (targetCalories: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // User responses
    var selectedGoal by remember { mutableStateOf("Gain Weight") } // Lose Weight, Maintain Weight, Gain Weight
    var currentWeightKg by remember { mutableFloatStateOf(50f) }
    var targetWeightKg by remember { mutableFloatStateOf(75f) }
    var selectedGender by remember { mutableStateOf("Male") } // Male, Female
    var heightFeet by remember { mutableIntStateOf(5) }
    var heightInches by remember { mutableIntStateOf(8) }
    var ageYears by remember { mutableIntStateOf(22) }
    var weeklyRateKg by remember { mutableFloatStateOf(0.5f) } // 0.25, 0.5, 1.0

    val totalSteps = 8
    val progress = step.toFloat() / totalSteps.toFloat()

    // Calculate BMR & TDEE based on Harris-Benedict formula
    val heightCm = (heightFeet * 30.48f) + (heightInches * 2.54f)
    val bmr = if (selectedGender == "Male") {
        (10 * currentWeightKg) + (6.25f * heightCm) - (5 * ageYears) + 5
    } else {
        (10 * currentWeightKg) + (6.25f * heightCm) - (5 * ageYears) - 161
    }

    val tdee = bmr * 1.375f // Lightly active
    val rateCalorieDelta = weeklyRateKg * 1000f
    val calculatedCalories = when (selectedGoal) {
        "Lose Weight" -> (tdee - rateCalorieDelta).roundToInt().coerceAtLeast(1200)
        "Gain Weight" -> (tdee + rateCalorieDelta).roundToInt().coerceAtLeast(1800)
        else -> tdee.roundToInt()
    }

    val buttonBlue = Color(0xFF2B5B84)

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            if (step > 1) {
                                step--
                            } else {
                                onBackClick()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = buttonBlue,
                    trackColor = Color(0xFFE3EBF3)
                )
            }
        },
        containerColor = Color.White,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            when (step) {
                1 -> {
                    // Step 1: What's your goal?
                    Text(
                        text = "What's your goal?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    val goals = listOf("Lose Weight", "Maintain Weight", "Gain Weight")
                    goals.forEach { goalOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedGoal == goalOption),
                                    onClick = { selectedGoal = goalOption }
                                )
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = (selectedGoal == goalOption),
                                onClick = { selectedGoal = goalOption },
                                colors = RadioButtonDefaults.colors(selectedColor = buttonBlue)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = goalOption,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 2 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                2 -> {
                    // Step 2: What's your weight?
                    Text(
                        text = "What's your weight?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${currentWeightKg.roundToInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "kg",
                            fontSize = 18.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = currentWeightKg,
                        onValueChange = { currentWeightKg = it },
                        valueRange = 30f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = buttonBlue,
                            activeTrackColor = buttonBlue,
                            inactiveTrackColor = Color(0xFFE3EBF3)
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 3 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                3 -> {
                    // Step 3: What is your target weight?
                    Text(
                        text = "What is your target weight?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    Text(
                        text = "Recommended range: 61-74 kg",
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${targetWeightKg.roundToInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "kg",
                            fontSize = 18.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = targetWeightKg,
                        onValueChange = { targetWeightKg = it },
                        valueRange = 30f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = buttonBlue,
                            activeTrackColor = buttonBlue,
                            inactiveTrackColor = Color(0xFFE3EBF3)
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 4 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                4 -> {
                    // Step 4: What's your gender?
                    Text(
                        text = "What's your gender?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    val genders = listOf("Male", "Female")
                    genders.forEach { genderOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedGender == genderOption),
                                    onClick = { selectedGender = genderOption }
                                )
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = (selectedGender == genderOption),
                                onClick = { selectedGender = genderOption },
                                colors = RadioButtonDefaults.colors(selectedColor = buttonBlue)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = genderOption,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 5 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                5 -> {
                    // Step 5: How tall are you?
                    Text(
                        text = "How tall are you?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${heightFeet}'  ${heightInches}\"",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "ft",
                            fontSize = 18.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Feet", fontSize = 14.sp, color = TextMuted)
                            Slider(
                                value = heightFeet.toFloat(),
                                onValueChange = { heightFeet = it.roundToInt() },
                                valueRange = 4f..7f,
                                steps = 2,
                                colors = SliderDefaults.colors(thumbColor = buttonBlue, activeTrackColor = buttonBlue),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Inches", fontSize = 14.sp, color = TextMuted)
                            Slider(
                                value = heightInches.toFloat(),
                                onValueChange = { heightInches = it.roundToInt() },
                                valueRange = 0f..11f,
                                steps = 10,
                                colors = SliderDefaults.colors(thumbColor = buttonBlue, activeTrackColor = buttonBlue),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 6 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                6 -> {
                    // Step 6: How old are you?
                    Text(
                        text = "How old are you?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$ageYears",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "years",
                            fontSize = 18.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = ageYears.toFloat(),
                        onValueChange = { ageYears = it.roundToInt() },
                        valueRange = 12f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = buttonBlue,
                            activeTrackColor = buttonBlue,
                            inactiveTrackColor = Color(0xFFE3EBF3)
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 7 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                7 -> {
                    // Step 7: How quickly do you want to gain/lose weight? (Matching reference screenshot)
                    val rateTitle = when (selectedGoal) {
                        "Gain Weight" -> "How quickly do you want to gain weight?"
                        "Lose Weight" -> "How quickly do you want to lose weight?"
                        else -> "What is your target pace?"
                    }

                    Text(
                        text = rateTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
                    )

                    val actionType = if (selectedGoal == "Lose Weight") "Loss" else "Gain"
                    val rates = listOf(
                        Triple(0.25f, "Mild Weight $actionType (0.25 kg/week)", formatGoalDate(currentWeightKg, targetWeightKg, 0.25f)),
                        Triple(0.5f, "Weight $actionType (0.5 kg/week)", formatGoalDate(currentWeightKg, targetWeightKg, 0.5f)),
                        Triple(1.0f, "Fast Weight $actionType (1 kg/week)", formatGoalDate(currentWeightKg, targetWeightKg, 1.0f))
                    )

                    rates.forEach { (rateValue, label, dateStr) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (weeklyRateKg == rateValue),
                                    onClick = { weeklyRateKg = rateValue }
                                )
                                .padding(vertical = 14.dp)
                        ) {
                            RadioButton(
                                selected = (weeklyRateKg == rateValue),
                                onClick = { weeklyRateKg = rateValue },
                                colors = RadioButtonDefaults.colors(selectedColor = buttonBlue)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = label,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Reach your goal by $dateStr",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 8 },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                8 -> {
                    // Step 8: Final Calculated Daily Goal (Matching reference screenshot)
                    Text(
                        text = "Daily Calorie Goal",
                        fontSize = 16.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(
                        text = "$calculatedCalories kcal",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    val actionText = when (selectedGoal) {
                        "Gain Weight" -> "gain $weeklyRateKg kg per week"
                        "Lose Weight" -> "lose $weeklyRateKg kg per week"
                        else -> "maintain your weight"
                    }

                    Text(
                        text = "This is the number of calories you would need to consume to $actionText",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = Color(0xFFEAEAEA), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Good luck on your health journey! 🌿",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You're in good hands. Over 1 million people use TrackBite to reach their goals.",
                        fontSize = 14.sp,
                        color = TextMuted,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Social Proof Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "4.8",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "100K+ reviews",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp)
                                    .background(Color(0xFFE3EBF3))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "1M+",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "downloads",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onFinish(calculatedCalories) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(text = "Finish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatGoalDate(currentKg: Float, targetKg: Float, rateKgPerWeek: Float): String {
    val diff = kotlin.math.abs(targetKg - currentKg)
    val weeksNeeded = if (rateKgPerWeek > 0f) (diff / rateKgPerWeek).toLong() else 0L
    val targetDate = java.time.LocalDate.now().plusWeeks(weeksNeeded)
    val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH)
    return targetDate.format(formatter)
}
