package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DateInfo
import com.example.ui.theme.GreenActivePill
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DateSelectorStrip(
    dates: List<DateInfo>,
    onDateSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dates.forEach { dateItem ->
            val isSelected = dateItem.isSelected

            val backgroundColor = if (isSelected) GreenActivePill else Color.Transparent
            val borderColor = if (isSelected) GreenPrimary else Color.Transparent
            val textColor = if (isSelected) GreenPrimary else TextSecondary

            Column(
                modifier = Modifier
                    .width(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onDateSelect(dateItem.dateString) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateItem.dayOfWeek,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = dateItem.dayOfMonth,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

