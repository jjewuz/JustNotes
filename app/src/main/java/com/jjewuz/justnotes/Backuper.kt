package com.jjewuz.justnotes

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Backuper (context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val applicationContext = applicationContext
            val mainActivity = applicationContext as MainActivity
            val backup = applicationContext.backup
            val storageRef = Firebase.storage.reference

            val sdf = SimpleDateFormat("dd.MM.yyyy_HH:mm", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date().time)

            val userId = Firebase.auth.currentUser?.uid
            backup
                .database(NoteDatabase.getDatabase(applicationContext))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                .customBackupFileName("database")
                .backupIsEncrypted(true)
                .customEncryptPassword(userId.toString())
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        if (success) {
                            // Handle success if needed
                        }
                    }
                }
                .backup()

            val fileUri = File(applicationContext.filesDir, "databasebackup/database.aes").toUri()
            return try {
                storageRef.child("user/$userId/database.aes").putFile(fileUri).await()

                val sdf2 = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                val currentDateAndTime2: String = sdf2.format(Date().time)
                val currentTime =
                    "${applicationContext.resources.getString(R.string.last_backup)} $currentDateAndTime2"
                applicationContext.sharedPref.edit().putString("last_backup", currentTime).apply()
                Log.d("BackupWorker", "Backup completed successfully.")
                Result.success()
            } catch (e: Exception) {
                Log.e("BackupWorker", "Backup failed: ${e.message}")
                Result.failure()
            }
        }
}