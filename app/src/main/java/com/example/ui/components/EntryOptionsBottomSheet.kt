package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntry
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryOptionsBottomSheet(
    entry: MealEntry?,
    onDismiss: () -> Unit,
    onEditEntry: (MealEntry) -> Unit,
    onAdjustMacros: (MealEntry) -> Unit,
    onChangeDateTime: (MealEntry) -> Unit,
    onAddToSaved: (MealEntry) -> Unit,
    onDeleteEntry: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    if (entry == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ✏️ Edit Entry
            OptionRow(
                icon = Icons.Default.Edit,
                title = "Edit Entry",
                onClick = { onEditEntry(entry) }
            )

            HorizontalDivider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // ⚖️ Adjust Calories & Macros
            OptionRow(
                icon = Icons.Default.Tune,
                title = "Adjust Calories & Macros",
                onClick = { onAdjustMacros(entry) }
            )

            HorizontalDivider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // 🕒 Change Date & Time
            OptionRow(
                icon = Icons.Default.AccessTime,
                title = "Change Date & Time",
                onClick = { onChangeDateTime(entry) }
            )

            HorizontalDivider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // 🔖 Add to Saved Entries
            OptionRow(
                icon = Icons.Default.BookmarkBorder,
                title = "Add to Saved Entries",
                onClick = { onAddToSaved(entry) }
            )

            HorizontalDivider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

            // 🗑️ Delete (Red)
            OptionRow(
                icon = Icons.Default.Delete,
                title = "Delete",
                tint = Color(0xFFD32F2F),
                onClick = onDeleteEntry
            )

            Spacer(modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
