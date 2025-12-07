package com.dcapps.heyreminder.ui

import com.dcapps.heyreminder.R

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dcapps.heyreminder.data.Reminder
import com.dcapps.heyreminder.data.ReminderRepository
import com.dcapps.heyreminder.data.ReminderScheduler
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onBack: () -> Unit,
    existing: Reminder? = null
) {
    BackHandler {
        onBack()
    }
    val context = LocalContext.current
    val calendarNow = Calendar.getInstance()

    var text by remember { mutableStateOf(existing?.text ?: "") }
    var hour by remember { mutableStateOf(existing?.hour ?: calendarNow.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(existing?.minute ?: calendarNow.get(Calendar.MINUTE)) }

    // Gün seçimi için state
    val daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7) // Pazartesi-Pazar
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            existing?.days?.forEach { add(it) }
        }
    }

    // Renkleri resources.xml’den alıyoruz
    val accentGreen = colorResource(R.color.accent_color)
    val bgColor = colorResource(R.color.white_background)
    val textColor = colorResource(R.color.text_color)
    val headerColor = colorResource(R.color.header_background)

    // Set status bar (menubar) color to header_background
    SideEffect {
        val window = (context as android.app.Activity).window
        window.statusBarColor = headerColor.toArgb()
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // HEADER with centered logo (same as MainScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(colorResource(R.color.header_background)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.heyreminder_splash),
                contentDescription = "Header logo",
                modifier = Modifier.size(148.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (existing != null) "Edit reminder" else "Create a new reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
            }
        }

        // Content area under header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Main content card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Reminder text section
                    Text(
                        text = "What do you want to remember?",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Take my pills", color = textColor.copy(alpha = 0.4f)) },
                        modifier = Modifier
                            .border(
                                width = 2.dp,
                                color = textColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = bgColor,
                            focusedIndicatorColor = accentGreen,
                            unfocusedIndicatorColor = accentGreen.copy(alpha = 0.2f),
                            cursorColor = accentGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Days of week section
                    Text(
                        text = "How often do you want to receive this?",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayLabels.forEachIndexed { index, label ->
                            val dayValue = daysOfWeek[index]
                            val isSelected = selectedDays.contains(dayValue)

                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) Color.Red else Color.LightGray,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .background(
                                        color = if (isSelected) Color.Red.copy(alpha = 0.25f) else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isSelected) selectedDays.remove(dayValue)
                                        else selectedDays.add(dayValue)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time section
                    Text(
                        text = "At what time exactly?",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        hour = h
                                        minute = m
                                    },
                                    hour,
                                    minute,
                                    true
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = accentGreen.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%02d:%02d".format(hour, minute),
                                color = textColor,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Change",
                                color = textColor,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary action button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val reminder = Reminder(
                            id = existing?.id ?: 0L,
                            text = text,
                            hour = hour,
                            minute = minute,
                            days = selectedDays
                        )
                        val saved = if (existing != null)
                            ReminderRepository.updateReminder(reminder)
                        else
                            ReminderRepository.addReminder(reminder)

                        ReminderScheduler.schedule(context, saved)
                        onBack()
                    }
                },
                enabled = text.isNotBlank() && selectedDays.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.light_accent),
                    disabledContainerColor = colorResource(R.color.light_accent).copy(alpha = 0.5f),
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (existing != null) "Update reminder" else "Save reminder",
                    color = textColor,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary cancel action
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.light_accent),
                    contentColor = textColor,
                    disabledContainerColor = colorResource(R.color.light_accent).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontSize = 18.sp)
            }
        }
    }
}