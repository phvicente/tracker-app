package com.wogoo.tracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.wogoo.tracker.ui.activities.AlarmAlertActivity
import com.wogoo.tracker.R
import com.wogoo.tracker.model.DatabaseProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        showAlarmNotification(context, intent)
    }

    private fun showAlarmNotification(context: Context, intent: Intent?) {

        val userId = intent?.getIntExtra("userId", -1) ?: -1
        val horario = intent?.getLongExtra("horario", -1L) ?: -1L

        val medicineNames: List<String> = if (userId != -1 && horario != -1L) {
            val db = DatabaseProvider.getDatabase(context)
            runBlocking {
                db.reminderDao().getRemindersByTime(userId, horario)
                    .first()
                    .map { it.nomeRemedio }
            }
        } else {
            emptyList()
        }

        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putStringArrayListExtra("medicineNames", ArrayList(medicineNames))
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        var vibrationPattern = longArrayOf(0, 100, 200, 300, 400, 500, 400, 300, 200, 100)



        val notificationBuilder = NotificationCompat.Builder(context, "alarm_channel_id")
            .setSmallIcon(R.drawable.baseline_access_alarm_24)
            .setContentTitle("Alarme Disparado!")
            .setContentText("Toque para desligar o alarme.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSound(soundUri, AudioManager.STREAM_ALARM)
            .setVibrate(vibrationPattern)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel_id",
                "Alarmas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para alarmes"
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                vibrationPattern = vibrationPattern
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, notificationBuilder.build())
    }
}