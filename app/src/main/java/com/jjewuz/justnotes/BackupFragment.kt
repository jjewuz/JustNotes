package com.jjewuz.justnotes

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.compose.material3.Text
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjewuz.justnotes.ui.theme.AppTheme
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

class BackupFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val isDynamicColor = sharedPref.getBoolean("enabledMonet", true)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme(isDynamicColor, enabledFont) {
                    Column ( horizontalAlignment = Alignment.CenterHorizontally) {
                        WarningText()
                        BackupFun()
                        Button({ replaceFragment(OtherFragment()) },
                            modifier = Modifier.padding(0.dp, 15.dp)){
                            Text(text = stringResource(id = R.string.back))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WarningText(
        title: String = stringResource(R.string.backup_txt),
        content: String = stringResource(R.string.backup_txt2),
        warn: String = stringResource(R.string.not_encrypted)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Box(modifier = Modifier.padding(10.dp)){
                Column() {
                    Text(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = content, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = warn, color = Color.Red)
                }
            }
        }
    }

    @Preview
    @Composable
    fun BackupFun(
        saveText: String = stringResource(id = R.string.do_backup),
        loadText: String = stringResource(id = R.string.do_restore)
    ){
        Card {
            Row(modifier = Modifier.padding(2.dp),){
                Button(onClick = { saveBackup() },
                    modifier = Modifier.padding(2.dp, 0.dp)){
                    Text(saveText)
                }
                Button(onClick = { loadBackup() },
                    modifier = Modifier.padding(2.dp, 0.dp)){
                    Text(loadText)
                }
            }
        }
    }

    private fun saveBackup(){
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

    private fun loadBackup(){
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

    private fun killAndRestartApp(activity: Activity) {

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }

}