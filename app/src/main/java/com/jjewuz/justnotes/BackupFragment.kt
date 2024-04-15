package com.jjewuz.justnotes

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import com.jjewuz.justnotes.databinding.FragmentBackupBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class BackupFragment : Fragment(R.layout.fragment_backup) {

    private var fragmentBackupBinding: FragmentBackupBinding? = null

    private lateinit var lastBackupText: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var autoSwitch: MaterialSwitch

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val binding = view.let { FragmentBackupBinding.bind(it) }
        fragmentBackupBinding = binding

        auth = Firebase.auth

        progressBar = binding.progressBar
        autoSwitch = binding.autoBackupSwitch
        
        binding.deleteBtn.visibility = View.GONE
        binding.logoutBtn.visibility = View.GONE
        binding.cloudButtons.visibility = View.GONE
        autoSwitch.visibility = View.GONE

        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser, binding)
        }

        binding.regBtn.setOnClickListener {
            register(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(), binding)
        }

        binding.logBtn.setOnClickListener {
            login(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(), binding)
        }

        binding.resetBtn.setOnClickListener {
            resetPassword(binding.emailEditText.text.toString())
        }

        binding.backup.setOnClickListener {
            backup(true)
        }

        binding.rest.setOnClickListener {
            restore(true)
        }

        binding.backupCloud.setOnClickListener {
            backup(false)
        }

        binding.restCloud.setOnClickListener {
            restore(false)
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            binding.userEmailTxt.text = resources.getString(R.string.no_account)
            binding.authLayout.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.GONE
            binding.deleteBtn.visibility = View.GONE
            binding.cloudButtons.visibility = View.GONE
            autoSwitch.visibility = View.GONE
        }

        autoSwitch.isChecked = sharedPref.getBoolean("auto_backup", false)

        binding.deleteBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delete_account)
                .setMessage(R.string.account_delete_info)
                .setPositiveButton(R.string.delete_account) { _, _ ->
                    val userId = Firebase.auth.currentUser?.uid
                    val userEmail = Firebase.auth.currentUser?.email
                    val storageRef = Firebase.storage.reference
                    storageRef.child("user/$userId/database.aes").delete()
                    auth.currentUser?.delete()?.addOnSuccessListener {
                        binding.userEmailTxt.text = resources.getString(R.string.no_account)
                        binding.authLayout.visibility = View.VISIBLE
                        binding.logoutBtn.visibility = View.GONE
                        binding.deleteBtn.visibility = View.GONE
                        binding.cloudButtons.visibility = View.GONE
                        Toast.makeText(requireContext(), R.string.deletion_succes, Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener {
                        Toast.makeText(requireContext(), R.string.auth_need, Toast.LENGTH_SHORT).show()
                        val builder = MaterialAlertDialogBuilder(requireActivity())
                        val inflater = requireActivity().layoutInflater.inflate(R.layout.auth_credential, null)
                        val pass = inflater.findViewById<TextInputEditText>(R.id.input)
                        builder.setView(inflater)
                            .setPositiveButton(R.string.delete_account) { dialog, id ->
                                val credential = userEmail?.let { it1 ->
                                    EmailAuthProvider.getCredential(
                                        it1, pass.text.toString())
                                }
                                if (credential != null) {
                                    auth.currentUser?.reauthenticate(credential)!!.addOnSuccessListener {
                                        auth.currentUser?.delete()?.addOnSuccessListener {
                                            binding.userEmailTxt.text = resources.getString(R.string.no_account)
                                            binding.authLayout.visibility = View.VISIBLE
                                            binding.logoutBtn.visibility = View.GONE
                                            binding.deleteBtn.visibility = View.GONE
                                            binding.cloudButtons.visibility = View.GONE
                                            Toast.makeText(requireContext(), R.string.deletion_succes, Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(requireContext(), R.string.deletion_error, Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }
                            .setNegativeButton(R.string.back) { dialog, _ ->
                                dialog.cancel()
                            }
                        builder.create().show()

                    }

                }
                .setNegativeButton(R.string.back) { _, _ ->

                }
                .show()
        }

        autoSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("auto_backup", true)
                    apply()
                }

                Toast.makeText(requireContext(), R.string.auto_backup_on, Toast.LENGTH_SHORT).show()
            }else{
                with (sharedPref.edit()) {
                    putBoolean("auto_backup", false)
                    apply()
                }
            }
        }
        lastBackupText = binding.lastBackup
        lastBackupText.text = sharedPref.getString("last_backup", "${resources.getString(R.string.last_backup)} - ")
    }

    fun backup(local: Boolean){
        val mainActivity = (activity as MainActivity)
        val backup = mainActivity.backup
        val storageRef = Firebase.storage.reference

        val sdf = SimpleDateFormat("dd.MM.yyyy_HH:mm", Locale.getDefault())
        val currentDateAndTime: String = sdf.format(Date().time)

        if (local) {
            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .customBackupFileName("JustNotes_Backup_$currentDateAndTime.sqlite")
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                        if (success) {}
                    }
                }
                .backup()
        } else {
            val userId = Firebase.auth.currentUser?.uid
            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                .customBackupFileName("database")
                .backupIsEncrypted(true)
                .customEncryptPassword(userId.toString())
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(
                            ContentValues.TAG,
                            "success: $success, message: $message, exitCode: $exitCode"
                        )
                        if (success) {
                            progressBar.progress = 25
                        }
                    }
                }
                .backup()

            storageRef.child("user/$userId/database.aes").putFile(
                File(
                    requireContext().filesDir,
                    "databasebackup/database.aes"
                ).toUri()
            ).addOnSuccessListener {
                val sdf2 = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                val currentDateAndTime2: String = sdf2.format(Date().time)
                val currentTime = "${resources.getString(R.string.last_backup)} $currentDateAndTime2"
                with (sharedPref.edit()) {
                    putString("last_backup", currentTime)
                    apply()
                }
                lastBackupText.text = currentTime
                Toast.makeText(requireContext(), R.string.backup_complete, Toast.LENGTH_SHORT).show()
                progressBar.progress = 100
            }
                .addOnProgressListener {
                    progressBar.progress = (it.bytesTransferred / it.totalByteCount).toInt() * 50
                }
        }
    }

    private fun restore(local: Boolean){
        val mainActivity = (activity as MainActivity)
        val backup = mainActivity.backup
        val storageRef = Firebase.storage.reference

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
        } else {
            val userId = Firebase.auth.currentUser?.uid
            val storagePath = File(requireContext().filesDir, "databasebackup")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "database.aes")

            val dbRef = storageRef.child("user/$userId/database.aes")
            dbRef.getFile(myFile).addOnSuccessListener {
                backup
                    .database(NoteDatabase.getDatabase(requireContext()))
                    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                    .backupIsEncrypted(true)
                    .customEncryptPassword(userId.toString())
                    .apply {
                        onCompleteListener { success, message, exitCode ->
                            Log.d(ContentValues.TAG, "success: $success, message: $message, exitCode: $exitCode")
                            if (success) {
                                killAndRestartApp(requireActivity())
                            }
                        }
                    }
                    .restore()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), R.string.restore_fail, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun register(email: String, password: String, binding: FragmentBackupBinding){
        if (email != "" && password != "") {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user, binding)
                        Toast.makeText(
                            requireContext(),
                            R.string.authSuc,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.authFail,
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null, binding)
                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                 R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun login(email: String, password: String, binding: FragmentBackupBinding){
        if (email != "" && password != "") {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user, binding)
                        Toast.makeText(
                            requireContext(),
                            R.string.authSuc,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.authFail,
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null, binding)
                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }

    }

    private fun resetPassword(email: String){
        if (email != "") {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), R.string.reset_email_sent, Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.no_info,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun updateUI(user: FirebaseUser?, binding: FragmentBackupBinding){
        user?.let {
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            binding.userEmailTxt.text = email
            binding.authLayout.visibility = View.GONE
            binding.logoutBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.VISIBLE
            binding.cloudButtons.visibility = View.VISIBLE
            autoSwitch.visibility = View.VISIBLE

            val emailVerified = it.isEmailVerified
            val uid = it.uid
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