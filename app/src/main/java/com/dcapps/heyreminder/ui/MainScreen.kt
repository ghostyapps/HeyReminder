package com.dcapps.heyreminder.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.dcapps.heyreminder.R
import com.dcapps.heyreminder.data.ReminderRepository
import com.dcapps.heyreminder.data.Reminder
import com.dcapps.heyreminder.data.ReminderScheduler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Surface
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
// RectangleShape import removed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var toBeDeleted by remember { mutableStateOf<Reminder?>(null) }
    val reminders by ReminderRepository.getAll().collectAsState(initial = emptyList())

    // Renkler
    val backgroundColor = colorResource(R.color.white_background)
    val accentGray    = colorResource(R.color.accent_color)
    val textColor       = colorResource(R.color.text_color)

    if (showCreate) {
        CreateReminderScreen(
            onBack = { showCreate = false; selectedReminder = null },
            existing = selectedReminder
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreate = true },
                    containerColor = accentGray
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reminder",
                        tint = textColor
                    )
                }
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(backgroundColor)
                ) {
                    // HEADER with centered logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(accentGray),
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
                            val count = reminders.size
                            val reminderWord = if (count == 1) "reminder" else "reminders"
                            Text(
                                text = "You have $count $reminderWord.",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = textColor
                            )
                        }
                    }



                    // LİSTE
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        itemsIndexed(reminders) { index, reminder ->
                            // 1. Create a DismissState and handle swipe actions
                            val dismissState = rememberDismissState(
                                confirmStateChange = { dismissValue ->
                                    when (dismissValue) {
                                        DismissValue.DismissedToStart -> {
                                            toBeDeleted = reminder
                                            showDeleteConfirm = true
                                            false
                                        }
                                        DismissValue.DismissedToEnd -> {
                                            // Sağa kaydırınca: düzenle
                                            selectedReminder = reminder
                                            showCreate = true
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
                                    if (direction == DismissDirection.EndToStart) FractionalThreshold(0.3f)
                                    else FractionalThreshold(0.25f)
                                },
                                background = {
                                    val direction = dismissState.dismissDirection
                                    if (direction != null) {
                                        val color = when (direction) {
                                            DismissDirection.StartToEnd -> accentGray
                                            DismissDirection.EndToStart -> Color.Red
                                            else -> Color.Transparent
                                        }
                                        val icon = if (direction == DismissDirection.StartToEnd)
                                            Icons.Default.Edit else Icons.Default.Delete
                                        BoxWithConstraints {
                                            val halfWidth = maxWidth / 2
                                            val innerPadding = 4.dp
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(vertical = innerPadding)
                                                    .background(color)
                                                    // For delete (EndToStart): pad left half; for edit (StartToEnd): pad right half
                                                    .padding(
                                                        start = if (dismissState.dismissDirection == DismissDirection.EndToStart) halfWidth else 0.dp,
                                                        end = if (dismissState.dismissDirection == DismissDirection.StartToEnd) halfWidth else 0.dp
                                                    )
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = if (dismissState.dismissDirection == DismissDirection.StartToEnd)
                                                    Alignment.CenterStart else Alignment.CenterEnd
                                            ) {
                                                Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                    }
                                },
                                dismissContent = {
                                    // Each item gets its own card-like background
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        color = colorResource(R.color.white_background),
                                        // tonalElevation = 2.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { /* no-op */ },
                                                    onLongClick = {
                                                        selectedReminder = reminder
                                                        showOptions = true
                                                    }
                                                )
                                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(Color.Red, shape = CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = reminder.text,
                                                    color = textColor,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            val daysLabels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                                            val dayText = if (reminder.days.size == 7) {
                                                "Everyday"
                                            } else {
                                                reminder.days.sorted().joinToString(", ") { daysLabels[it - 1] }
                                            }
                                            Text(
                                                text = "%02d:%02d - $dayText".format(reminder.hour, reminder.minute),
                                                color = textColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (showDeleteConfirm && toBeDeleted != null) {
                        AlertDialog(
                            onDismissRequest = {
                                showDeleteConfirm = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            title = { Text("Delete Confirmation") },
                            text = { Text("Are you sure you want to delete the '${toBeDeleted!!.text}' reminder?") },
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
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showOptions && selectedReminder != null) {
                        AlertDialog(
                            onDismissRequest = { showOptions = false },
                            title = { Text("Reminder Options") },
                            text = { Text("What do you want to do?") },
                            confirmButton = {
                                Button(onClick = {
                                    showCreate = true
                                    showOptions = false
                                }) {
                                    Text("Edit")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            // cancel scheduled notification
                                            ReminderScheduler.cancel(context, selectedReminder!!)
                                            // delete from repository
                                            ReminderRepository.deleteReminder(selectedReminder!!)
                                            showOptions = false
                                        }
                                    }
                                ) {
                                    Text("Delete")
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}