package com.dcapps.heyreminder.ui

import com.dcapps.heyreminder.R
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb // Import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView // Import
import androidx.core.view.WindowCompat // Import
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dcapps.heyreminder.data.Reminder
import com.dcapps.heyreminder.data.ReminderRepository
import com.dcapps.heyreminder.data.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onBack: () -> Unit,
    existing: Reminder? = null
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val view = LocalView.current // View erişimi
    val coroutineScope = rememberCoroutineScope()
    val calendarNow = Calendar.getInstance()

    // --- STATE ---
    var text by remember { mutableStateOf(existing?.text ?: "") }
    var hour by remember { mutableStateOf(existing?.hour ?: calendarNow.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(existing?.minute ?: calendarNow.get(Calendar.MINUTE)) }

    val daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            if (existing != null) {
                existing.days.forEach { add(it) }
            }
        }
    }

    // --- DARK MODE VE RENKLER ---
    val isDark = isSystemInDarkTheme()

    val headerBackgroundColor = if(isDark) Color.Black else colorResource(R.color.accent_color)
    val contentCardBackground = if(isDark) Color(0xFF1C1C1E) else colorResource(R.color.white_background)
    val textColor = if(isDark) Color.White else colorResource(R.color.text_color)

    // --- SYSTEM BARS AYARI ---
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as android.app.Activity).window

            // Status Bar -> Header
            window.statusBarColor = headerBackgroundColor.toArgb()

            // Navigation Bar -> Body
            window.navigationBarColor = contentCardBackground.toArgb()

            // İkon Renkleri
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    // --- UI STRUCTURE ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerBackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. HEADER SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 30.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.reminder_simple),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (existing != null) "Edit Reminder" else "Create Reminder",
                    fontSize = 36.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set your task details below.",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. BODY SECTION
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = contentCardBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, start = 24.dp, end = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {

                    // A. TASK INPUT
                    Text(
                        text = "WHAT TO DO?",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                "e.g. Take pills",
                                color = textColor.copy(alpha = 0.3f),
                                fontSize = 24.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = textColor,
                            unfocusedIndicatorColor = textColor.copy(alpha = 0.2f),
                            cursorColor = textColor
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // B. TIME PICKER
                    Text(
                        text = "WHEN?",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val timeBoxBorderColor = textColor.copy(alpha = 0.3f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .border(1.dp, timeBoxBorderColor, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
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
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "%02d:%02d".format(hour, minute),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Select Time",
                            tint = textColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // C. DAYS SELECTOR
                    Text(
                        text = "REPEAT",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayLabels.forEachIndexed { index, label ->
                            val dayValue = daysOfWeek[index]
                            val isSelected = selectedDays.contains(dayValue)

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) textColor else Color.Transparent
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) textColor else textColor.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (isSelected) selectedDays.remove(dayValue)
                                        else selectedDays.add(dayValue)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) contentCardBackground else textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // D. ACTION BUTTONS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
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

                                    if (existing != null) {
                                        ReminderRepository.updateReminder(reminder)
                                    } else {
                                        ReminderRepository.addReminder(reminder)
                                    }

                                    ReminderScheduler.schedule(context, reminder)
                                    onBack()
                                }
                            },
                            enabled = text.isNotBlank() && selectedDays.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = textColor,
                                contentColor = contentCardBackground,
                                disabledContainerColor = textColor.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text(
                                text = if (existing != null) "Update Reminder" else "Save Reminder",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { onBack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}