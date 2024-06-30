package com.jjewuz.justnotes.Notes

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jjewuz.justnotes.Category.Category
import com.jjewuz.justnotes.Category.CategoryDao
import com.jjewuz.justnotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(entities = [Note::class, Category::class], version = 4, exportSchema = true, autoMigrations = [ AutoMigration (1,2), AutoMigration(2,3)])
abstract class NoteDatabase : RoomDatabase() {

    abstract fun getNotesDao(): NotesDao
    abstract fun getCategoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getDatabase(context).getCategoryDao().insert(Category(name = "Unlabeled"))
                        }
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultCategoryName = context.getString(R.string.no_label)
                            val dao = getDatabase(context).getCategoryDao()
                            dao.updateCategoryName(1, defaultCategoryName)
                        }
                    }
                })
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }

        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Выполняем миграцию для добавления categoryId
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY NOT NULL,
                `name` TEXT NOT NULL
            )
        """.trimIndent())

                // Добавление столбца categoryId в таблицу notes
                db.execSQL("ALTER TABLE notesTable ADD COLUMN categoryId INTEGER")

                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `notesTable` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `Title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `timestamp` TEXT NOT NULL,
                `security` TEXT DEFAULT '',
                `label` TEXT DEFAULT '',
                `categoryId` INTEGER
            )
        """.trimIndent())

                db.execSQL("""
            INSERT INTO categories (id, name) VALUES (1, 'Unlabeled')
        """.trimIndent())
            }
        }

    }

}

fun getSelectedNoteID(context: Context): Int{
    val sharedPref = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    return sharedPref.getInt("note_id", -1)
}