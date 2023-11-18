package com.jjewuz.justnotes

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Note::class), version = 2, exportSchema = true, autoMigrations = [ AutoMigration (1,2)])
abstract class NoteDatabase : RoomDatabase() {

    abstract fun getNotesDao(): NotesDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                instance
            }

        }

    }
}

fun getSelectedNoteID(context: Context): Int{
    val sharedPref = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    return sharedPref.getInt("note_id", -1)
}