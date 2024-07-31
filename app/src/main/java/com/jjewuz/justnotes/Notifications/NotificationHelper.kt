package com.jjewuz.justnotes.Notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jjewuz.justnotes.R
import java.time.LocalDateTime
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    @SuppressLint("ServiceCast", "LaunchActivityFromNotification")
    fun createNotification(title: String, message: String, id: Int, dateTime: LocalDateTime) {

        val channel = NotificationChannel(
            "0",
            "Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channel.id)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.reminders)
            .setAutoCancel(true)

        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("message", message)
        intent.putExtra("id", id)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.setContentIntent(pendingIntent)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateTime.year)
            set(Calendar.MONTH, dateTime.monthValue) // Месяцы в Calendar начинаются с 0
            set(Calendar.DAY_OF_MONTH, dateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, dateTime.hour)
            set(Calendar.MINUTE, dateTime.minute)
            set(Calendar.SECOND, 0)
        }
        val alarmTime = calendar.timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intenta ->
                    intenta.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    context.startActivity(intenta)
                }
            }
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }
}
