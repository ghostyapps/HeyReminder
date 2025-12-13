package com.dcapps.heyreminder.ui

import java.util.Calendar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Surface
import com.dcapps.heyreminder.R
import com.dcapps.heyreminder.data.ReminderRepository
import com.dcapps.heyreminder.data.Reminder
import com.dcapps.heyreminder.data.ReminderScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    // States
    var showCreate by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var toBeDeleted by remember { mutableStateOf<Reminder?>(null) }

    val reminders by ReminderRepository.getAll().collectAsState(initial = emptyList())

    // -- TIME LOGIC --
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning."
            in 12..16 -> "Good afternoon."
            in 17..20 -> "Good evening."
            else -> "Good night."
        }
    }

    // -- DARK MODE VE RENK AYARLARI --
    val isDark = isSystemInDarkTheme()

    val headerBackgroundColor = if (isDark) Color.Black else colorResource(R.color.accent_color)
    val contentCardBackground = if (isDark) Color(0xFF1C1C1E) else colorResource(R.color.white_background)

    val textColor = if (isDark) Color.White else colorResource(R.color.text_color)
    val accentGray = if (isDark) Color.Gray else colorResource(R.color.accent_color)

    // --- SYSTEM BARS AYARI ---
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as android.app.Activity).window

            // Status Bar -> Header Rengi
            window.statusBarColor = headerBackgroundColor.toArgb()

            // Navigation Bar -> Body (Kart) Rengi
            window.navigationBarColor = contentCardBackground.toArgb()

            // İkon Renkleri
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    if (showCreate) {
        CreateReminderScreen(
            onBack = {
                showCreate = false
                selectedReminder = null
            },
            existing = selectedReminder
        )
    } else {
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
                        contentDescription = "Header Art",
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = greetingText,
                        fontSize = 36.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val count = reminders.size
                    val suffix = if (count == 1) "reminder" else "reminders"
                    val subtitleText = if (count == 0) "You have no reminders." else "You have $count $suffix."

                    Text(
                        text = subtitleText,
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 2. BODY SECTION (KART)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = contentCardBackground,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 0.dp
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(reminders, key = { _, item -> item.id }) { index, reminder ->
                            ReminderItemWithSwipe(
                                reminder = reminder,
                                textColor = textColor,
                                cardColor = if (isDark) Color(0xFF2C2C2E) else Color.White,
                                accentGray = accentGray,
                                onEdit = {
                                    selectedReminder = reminder
                                    showCreate = true
                                },
                                onDeleteRequest = {
                                    toBeDeleted = reminder
                                    showDeleteConfirm = true
                                },
                                onLongClick = {
                                    selectedReminder = reminder
                                    showOptions = true
                                }
                            )
                        }
                    }
                }
            }

            // 3. FAB
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = textColor,
                contentColor = if(isDark) Color.Black else Color.White,
                elevation = FloatingActionButtonDefaults.elevation(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reminder"
                )
            }

            // --- DIALOGS ---
            if (showDeleteConfirm && toBeDeleted != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    containerColor = contentCardBackground,
                    title = { Text("Delete Confirmation", color = textColor) },
                    text = { Text("Are you sure you want to delete '${toBeDeleted!!.text}'?", color = textColor) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    ReminderScheduler.cancel(context, toBeDeleted!!)
                                    ReminderRepository.deleteReminder(toBeDeleted!!)
                                    showDeleteConfirm = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirm = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = textColor.copy(0.6f))
                        ) { Text("Cancel") }
                    }
                )
            }

            if (showOptions && selectedReminder != null) {
                AlertDialog(
                    onDismissRequest = { showOptions = false },
                    containerColor = contentCardBackground,
                    title = { Text("Options", color = textColor) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCreate = true
                                showOptions = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = textColor)
                        ) { Text("Edit") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    ReminderScheduler.cancel(context, selectedReminder!!)
                                    ReminderRepository.deleteReminder(selectedReminder!!)
                                    showOptions = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) { Text("Delete") }
                    }
                )
            }
        }
    }
}

// --- HELPER COMPOSABLE ---

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderItemWithSwipe(
    reminder: Reminder,
    textColor: Color,
    cardColor: Color,
    accentGray: Color,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onLongClick: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDeleteRequest()
                    false
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        dismissThresholds = { direction ->
            FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.25f else 0.25f)
        },
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color = if (direction == DismissDirection.StartToEnd) accentGray else Color.Red
            val icon = if (direction == DismissDirection.StartToEnd) Icons.Default.Edit else Icons.Default.Delete
            val alignment = if (direction == DismissDirection.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        },
        dismissContent = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = cardColor,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if(isSystemInDarkTheme()) Color.Transparent else Color.LightGray.copy(alpha = 0.3f)
                ),
                shadowElevation = if(isSystemInDarkTheme()) 0.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { /* Detay yok */ },
                            onLongClick = onLongClick
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFE57373), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = reminder.text,
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val daysLabels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                    val dayText = if (reminder.days.size == 7) "Everyday"
                    else reminder.days.sorted().joinToString(", ") { daysLabels[it - 1] }

                    Text(
                        text = "%02d:%02d - $dayText".format(reminder.hour, reminder.minute),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }
            }
        }
    )
}