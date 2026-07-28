package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalsScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit,
    onOpenCalculatorClick: () -> Unit,
    onSaveGoals: (calories: Int, carbsGrams: Int, proteinGrams: Int, fatGrams: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var caloriesStr by remember(userProfile.targetCalories) { mutableStateOf(userProfile.targetCalories.toString()) }
    var carbsPctStr by remember { mutableStateOf("50") }
    var proteinPctStr by remember { mutableStateOf("25") }
    var fatPctStr by remember { mutableStateOf("25") }

    val calories = caloriesStr.toIntOrNull() ?: 2000
    val carbsPct = carbsPctStr.toIntOrNull() ?: 50
    val proteinPct = proteinPctStr.toIntOrNull() ?: 25
    val fatPct = fatPctStr.toIntOrNull() ?: 25

    val carbsGrams = (calories * (carbsPct / 100f) / 4f).toInt()
    val proteinGrams = (calories * (proteinPct / 100f) / 4f).toInt()
    val fatGrams = (calories * (fatPct / 100f) / 9f).toInt()

    LaunchedEffect(caloriesStr, carbsPctStr, proteinPctStr, fatPctStr) {
        onSaveGoals(calories, carbsGrams, proteinGrams, fatGrams)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Goals",
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Calories section
            Text(
                text = "Calories",
                fontSize = 14.sp,
                color = TextMuted
            )

            TextField(
                value = caloriesStr,
                onValueChange = { caloriesStr = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.DarkGray,
                    unfocusedIndicatorColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Use daily calorie goal calculator link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onOpenCalculatorClick() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Use daily calorie goal calculator",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Carbohydrates section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Carbohydrates ${carbsGrams}g",
                    fontSize = 15.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = carbsPctStr,
                    onValueChange = { carbsPctStr = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.DarkGray,
                        unfocusedIndicatorColor = CardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%",
                    fontSize = 18.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Protein section
            Text(
                text = "Protein ${proteinGrams}g",
                fontSize = 15.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = proteinPctStr,
                    onValueChange = { proteinPctStr = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.DarkGray,
                        unfocusedIndicatorColor = CardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%",
                    fontSize = 18.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fat section
            Text(
                text = "Fat ${fatGrams}g",
                fontSize = 15.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = fatPctStr,
                    onValueChange = { fatPctStr = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.DarkGray,
                        unfocusedIndicatorColor = CardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%",
                    fontSize = 18.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
