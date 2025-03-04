package com.wogoo.tracker.ui.activities

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.wogoo.tracker.model.entity.Reminder
import com.wogoo.tracker.reminder.AlarmReceiver
import com.wogoo.tracker.reminder.ReminderViewModel
import com.wogoo.tracker.ui.screen.AlarmScreen
import com.wogoo.tracker.ui.screen.components.ReminderDialog
import com.wogoo.tracker.ui.screen.components.ReportDialog
import com.wogoo.tracker.ui.theme.TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val alarmRequestCode = 123
    private val reminderViewModel: ReminderViewModel by viewModels()
    private var currentUserId: Int = -1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        } else {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: "Android"
        val userId = intent.getIntExtra("USER_ID", -1)
        currentUserId = userId

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            TrackerTheme {
                val reminders by reminderViewModel.allReminders.collectAsState(initial = emptyList())
                var showCreateDialog by remember { mutableStateOf(false) }
                var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
                var showReportScreen by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(
                        modifier = Modifier.padding(innerPadding),
                        userName = userName,
                        reminders = reminders,
                        onCreateReminderClicked = { showCreateDialog = true },
                        onEditReminderClicked = { reminder -> reminderToEdit = reminder },
                        onDeleteReminderClicked = { reminder ->
                            deleteExactAlarm()
                            reminderViewModel.deleteReminder(reminder)
                        },
                        onReportClick = {
                            showReportScreen = true
                        }
                    )
                }

                if (showCreateDialog) {
                    ReminderDialog(
                        onDismiss = { showCreateDialog = false },
                        onConfirm = { medicineName, timeMillis, repeatForever, repeatDays, repeatIntervalMode, intervalHours, numIntervals ->
                            if (userId != -1) {
                                if (repeatIntervalMode) {
                                    reminderViewModel.addReminderWithIntervals(
                                        medicineName = medicineName,
                                        initialTime = timeMillis,
                                        repeatForever = repeatForever,
                                        repeatDays = repeatDays,
                                        repeatIntervalMode = repeatIntervalMode,
                                        intervalHours = intervalHours,
                                        numIntervals = numIntervals,
                                        userId = userId
                                    )
                                } else {
                                    val newReminder = Reminder(
                                        nomeRemedio = medicineName,
                                        horario = timeMillis,
                                        ativo = true,
                                        userId = userId,
                                        repeatForever = repeatForever,
                                        repeatDays = if (!repeatForever) repeatDays else 0
                                    )
                                    reminderViewModel.addReminder(newReminder)
                                }
                            }
                        }
                    )
                }

                if (reminderToEdit != null) {
                    ReminderDialog(
                        initialMedicineName = reminderToEdit!!.nomeRemedio,
                        initialTimeMillis = reminderToEdit!!.horario,
                        initialRepeatForever = reminderToEdit!!.repeatForever,
                        initialRepeatDays = reminderToEdit!!.repeatDays,
                        onDismiss = { reminderToEdit = null },
                        onConfirm = { medicineName, timeMillis, repeatForever, repeatDays, repeatIntervalMode, intervalHours, numIntervals ->
                            val delay = timeMillis - System.currentTimeMillis()
                            if (delay > 0) {
                                createExactAlarm(delay)
                            }
                            val updatedReminder = reminderToEdit!!.copy(
                                nomeRemedio = medicineName,
                                horario = timeMillis,
                                repeatForever = repeatForever,
                                repeatDays = if (!repeatForever) repeatDays else 0
                            )
                            reminderViewModel.updateReminder(updatedReminder)
                            reminderToEdit = null
                        }
                    )
                }
                if (showReportScreen) {
                    ReportDialog(
                        viewModel = reminderViewModel,
                        userId = userId,
                        onDismiss = { showReportScreen = false }
                    )
                }
            }
        }
    }

    private fun createExactAlarm(delayInMillis: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val triggerTime = System.currentTimeMillis() + delayInMillis

        intent.putExtra("userId", currentUserId)
        intent.putExtra("horario", triggerTime)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            try {
                val triggerTime = System.currentTimeMillis() + delayInMillis
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Toast.makeText(this, "Alarme agendado para ${delayInMillis / 1000} segundos a partir de agora", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Permissão para alarmes exatos não concedida", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Não é possível agendar alarmes exatos. Verifique as configurações.", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteExactAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Alarme cancelado", Toast.LENGTH_SHORT).show()
    }
}
