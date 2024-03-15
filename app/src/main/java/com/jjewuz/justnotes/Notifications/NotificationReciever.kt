package com.jjewuz.justnotes.Notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.jjewuz.justnotes.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")
        val id = intent.getIntExtra("id", -1)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, "0")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.reminders)
            .setAutoCancel(true)

        notificationManager.notify(id, notificationBuilder.build())
    }
}
