import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.jjewuz.justnotes.MainActivity
import com.jjewuz.justnotes.NoteDatabase
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.databinding.FragmentBackupBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BackupFragment : Fragment(R.layout.fragment_backup) {

    private var fragmentBackupBinding: FragmentBackupBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = view.let { FragmentBackupBinding.bind(it) }
        fragmentBackupBinding = binding

        binding.backup.setOnClickListener {
            backup(true)
        }

        binding.rest.setOnClickListener {
            restore(true)
        }

    }

    private fun backup(local: Boolean){
        val mainActivity = (activity as MainActivity)
        val backup = mainActivity.backup

        val sdf = SimpleDateFormat("dd.mm.yyyy_HH:mm", Locale.getDefault())
        val currentDateAndTime: String = sdf.format(Date().time)

        if (local) {
            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .customBackupFileName("JustNotes_Backup_$currentDateAndTime.sqlite")
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(
                            ContentValues.TAG,
                            "success: $success, message: $message, exitCode: $exitCode"
                        )
                        if (success) {
                        }
                    }
                }
                .backup()
        }
    }

    private fun restore(local: Boolean){
        val mainActivity = (activity as MainActivity)
        val backup = mainActivity.backup

        if (local) {
            backup
                .database(NoteDatabase.getDatabase(requireContext()))
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




    private fun killAndRestartApp(activity: Activity) {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }

}