package com.jjewuz.justnotes

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.jjewuz.justnotes.databinding.FragmentBackupBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

class BackupFragment : Fragment(R.layout.fragment_backup) {

   private var fragmentBackupBinding: FragmentBackupBinding? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = view.let { FragmentBackupBinding.bind(it) }
        fragmentBackupBinding = binding


        binding.backup.setOnClickListener {
            val mainActivity = (activity as MainActivity)
            val backup = mainActivity.backup

            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                        if (success) {}
                    }
                }
                .backup()
        }

        binding.rest.setOnClickListener {
            val mainActivity = (activity as MainActivity)
            val backup = mainActivity.backup

            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                        if (success) {
                            killAndRestartApp(requireActivity())
                        }
                    }
                }
                .restore()
        }
    }

    fun killAndRestartApp(activity: Activity) {

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }

}