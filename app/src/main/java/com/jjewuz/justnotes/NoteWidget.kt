package com.jjewuz.justnotes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NoteWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val noteId =  getSelectedNoteID(context)
        for (appWidgetId in appWidgetIds) {
            getNoteTextFromDB(context, noteId) { textNote, noteDesc -> updateAppWidget(context, appWidgetManager, appWidgetId, textNote, noteDesc)}
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun getNoteTextFromDB(context: Context, noteId: Int, callback: (String, String) -> Unit){
        val database = Room.databaseBuilder(context, NoteDatabase::class.java, "note_database").build()
        val empty = context.resources.getString(R.string.empty)
        val desc = context.resources.getString(R.string.no_note)

        GlobalScope.launch(Dispatchers.IO) {
            val note = database.getNotesDao().getNoteById(noteId)
            val noteText = note?.noteTitle ?: empty
            val noteDesc = note?.noteDescription ?: desc

            callback(noteText, noteDesc)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    noteText: String,
    noteDesc: String,
) {
    val views = RemoteViews(context.packageName, R.layout.note_widget)
    views.setTextViewText(R.id.appwidget_text, noteText)
    views.setTextViewText(R.id.widgetdesc_text, Utils.fromHtml(noteDesc))

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(R.id.background, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}