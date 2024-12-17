package com.jjewuz.justnotes.Utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.IBinder
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.NotificationCompat
import com.jjewuz.justnotes.Activities.AddEditNoteActivity
import com.jjewuz.justnotes.Notifications.NotificationReceiver
import com.jjewuz.justnotes.R


object Utils {
    fun fromHtml(text: String?): SpannableString {
        var spannableString = SpannableString.valueOf(Html.fromHtml(text))
        if (spannableString.toString().endsWith("\n\n")) spannableString =
            spannableString.subSequence(0, spannableString.length - 2) as SpannableString
        return spannableString
    }

    fun textFormatting(param: String, noteEdt: EditText){
        val selectedTextStart = noteEdt.selectionStart
        val selectedTextEnd = noteEdt.selectionEnd

        if (selectedTextStart != -1 && selectedTextEnd != -1) {
            val spannable = SpannableString(noteEdt.text)
            when (param) {
                "bold" -> {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                "italic" -> {
                    spannable.setSpan(StyleSpan(Typeface.ITALIC), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                "under" -> {
                    spannable.setSpan(UnderlineSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                "strike" -> {
                    spannable.setSpan(StrikethroughSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                "null" -> {
                    val spans = spannable.getSpans(
                        selectedTextStart, selectedTextEnd,
                        CharacterStyle::class.java
                    )
                    for (selectSpan in spans) spannable.removeSpan(selectSpan)
                }
            }
            noteEdt.setText(spannable)
            noteEdt.setSelection(selectedTextEnd)
        }
    }

    fun colorFormatting(param: String, noteEdt: EditText){
        val selectedTextStart = noteEdt.selectionStart
        val selectedTextEnd = noteEdt.selectionEnd

        if (selectedTextStart != -1 && selectedTextEnd != -1) {
            val spannable = SpannableString(noteEdt.text)
            when (param) {
                "red" -> {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.rgb(193, 22, 22)),
                        selectedTextStart,
                        selectedTextEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                "yellow" -> {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.rgb(227, 174, 18)),
                        selectedTextStart,
                        selectedTextEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                "blue" -> {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.rgb(15, 68, 202)),
                        selectedTextStart,
                        selectedTextEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                "green" -> {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.rgb(113, 219, 165)),
                        selectedTextStart,
                        selectedTextEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            noteEdt.setText(spannable)
            noteEdt.setSelection(selectedTextEnd)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    class PersistentService : Service() {
        companion object {
            const val NOTE_ID_KEY = "noteId"
            const val NOTE_LABEL_ID = "label"
        }
        private lateinit var notificationManager: NotificationManager

        private var noteId: Int = -1
        private var label: Int = 0

        override fun onCreate() {
            super.onCreate()

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val initialNotification = NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.drawable.note)
                .setContentTitle(resources.getString(R.string.service_work))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            startForeground(1, initialNotification)
            notificationManager.cancel(0)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            noteId = intent?.getIntExtra(NOTE_ID_KEY, -1) ?: -1
            val noteTitle = intent?.getStringExtra("noteTitle") ?: "No Title"
            val noteDescription = intent?.getStringExtra("noteDescription") ?: "No Description"
            val timestamp = intent?.getStringExtra("timestamp") ?: "0"
            val security = intent?.getStringExtra("security") ?: ""
            label = intent?.getIntExtra(NOTE_LABEL_ID, 0) ?: 0


            val deleteIntent = Intent(this, NotificationReceiver::class.java).apply {
                action = "com.jjewuz.NOTIFICATION_DELETED"
                putExtra(NOTE_ID_KEY, noteId)
                putExtra("noteTitle", noteTitle)
                putExtra("noteDescription", noteDescription)
            }

            val deletePendingIntent = PendingIntent.getBroadcast(
                this,
                noteId + 1000,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openIntent = Intent(this, AddEditNoteActivity::class.java).apply {
                putExtra("noteType", "Edit")
                putExtra("noteId", noteId)
                putExtra("noteTitle", noteTitle)
                putExtra("noteDescription", noteDescription)
                putExtra("timestamp", timestamp)
                putExtra("categoryId", label)
                putExtra("security", security)
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val openPendingIntent = PendingIntent.getActivity(
                this,
                noteId,
                openIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val hideIntent = Intent(this, NotificationReceiver::class.java).apply {
                action = "com.jjewuz.NOTIFICATION_HIDE"
                putExtra(NOTE_ID_KEY, noteId)
            }

            val hidePendingIntent = PendingIntent.getBroadcast(
                this,
                noteId,
                hideIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val hideAction = NotificationCompat.Action.Builder(
                R.drawable.delete,
                resources.getString(R.string.hide),
                hidePendingIntent
            ).build()

            val openAction = NotificationCompat.Action.Builder(
                R.drawable.opening,
                resources.getString(R.string.open),
                openPendingIntent
            ).build()

            val spannedContent =
                Html.fromHtml(noteDescription, Html.FROM_HTML_MODE_COMPACT).toString()
            val content = if (spannedContent.length > 100) {
                "${spannedContent.take(150)}..."
            } else {
                spannedContent
            }

            val notification = NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.drawable.note)
                .setContentTitle(noteTitle)
                .setContentText(content)
                .addAction(hideAction)
                .addAction(openAction)
                .setDeleteIntent(deletePendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()


            notificationManager.notify(noteId, notification)


            return START_STICKY
        }

        override fun onDestroy() {
            Log.d("NoteService", "Stopping service with noteId: $noteId")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        override fun onBind(intent: Intent?): IBinder? = null
    }


}