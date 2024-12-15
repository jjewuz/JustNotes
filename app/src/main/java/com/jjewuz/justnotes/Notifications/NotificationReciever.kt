package com.jjewuz.justnotes.Notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.jjewuz.justnotes.Utils.Utils


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.jjewuz.NOTIFICATION_DELETED" -> {
                val serviceIntent = Intent(context, Utils.PersistentService::class.java).apply {
                    putExtra("noteId", intent.getIntExtra("noteId", -1))
                    putExtra("noteTitle", intent.getStringExtra("noteTitle"))
                    putExtra("noteDescription", intent.getStringExtra("noteDescription"))
                }
                Log.d("NotesService", "Open note with ${intent.getStringExtra("noteTitle")}")
                ContextCompat.startForegroundService(context, serviceIntent)
            }

            "com.jjewuz.NOTIFICATION_HIDE" -> {
                val noteId = intent.getIntExtra("noteId", -1)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(noteId)
                Log.d("NotesService", "Closed note with $noteId")
            }
        }
    }
}

