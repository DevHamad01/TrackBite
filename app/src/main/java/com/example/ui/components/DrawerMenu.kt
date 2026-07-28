package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DrawerMenu(
    userProfile: UserProfile,
    onCloseDrawer: () -> Unit,
    onUpgradeClick: () -> Unit,
    onNavigateToDailyGoals: () -> Unit = {},
    onNavigateToWeeklySummary: () -> Unit = {},
    onNavigateToWeightTracker: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStreak: () -> Unit = {},
    onNavigateToWaterTracker: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    isLoggedIn: Boolean = true,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 28.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            // App Header (TrackBite Logo + Title)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Green TrackBite Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "TrackBite Logo",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "TrackBite",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items matching Journable Reference
            DrawerMenuItem(
                icon = Icons.Default.Flag,
                title = "Daily Goals",
                onClick = {
                    onCloseDrawer()
                    onNavigateToDailyGoals()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.BarChart,
                title = "Weekly Summary",
                onClick = {
                    onCloseDrawer()
                    onNavigateToWeeklySummary()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.MonitorWeight,
                title = "Weight Tracker",
                onClick = {
                    onCloseDrawer()
                    onNavigateToWeightTracker()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Notifications,
                title = "Reminders",
                onClick = {
                    onCloseDrawer()
                    onNavigateToReminders()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.WaterDrop,
                title = "Water Tracker",
                onClick = {
                    onCloseDrawer()
                    onNavigateToWaterTracker()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "Account",
                onClick = {
                    onCloseDrawer()
                    onNavigateToAccount()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Feedback,
                title = "Feedback & Support",
                onClick = {
                    onCloseDrawer()
                    onFeedbackClick()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = {
                    onCloseDrawer()
                    onNavigateToSettings()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.ExitToApp,
                title = if (isLoggedIn) "Logout" else "Login",
                onClick = {
                    onCloseDrawer()
                    onLogoutClick()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "TrackBite v1.0.0",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary
        )
    }
}
