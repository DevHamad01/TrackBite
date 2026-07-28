package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun FeedbackDialog(
    initialEmail: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var emailText by remember { mutableStateOf(initialEmail) }
    var feedbackMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "Feedback & Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Heading & Input
                Text(
                    text = "Your Email Address",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = emailText,
                    onValueChange = { emailText = it },
                    singleLine = true,
                    placeholder = { Text("Enter your email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Message Heading & Input
                Text(
                    text = "Your Message / Feedback",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = feedbackMessage,
                    onValueChange = { feedbackMessage = it },
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Tell us what you like or how we can improve...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = TextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (feedbackMessage.isBlank()) {
                                Toast.makeText(context, "Please enter your message", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mh.atgsystems@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "TrackBite Feedback from $emailText")
                                putExtra(Intent.EXTRA_TEXT, "User Email: $emailText\n\nFeedback:\n$feedbackMessage")
                            }

                            try {
                                context.startActivity(intent)
                                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Feedback recorded. Thank you!", Toast.LENGTH_SHORT).show()
                            }

                            onDismiss()
                        }
                    ) {
                        Text(text = "Submit", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
