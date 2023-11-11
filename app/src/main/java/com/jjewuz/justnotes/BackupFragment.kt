package com.jjewuz.justnotes

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import com.jjewuz.justnotes.databinding.FragmentBackupBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File


class BackupFragment : Fragment(R.layout.fragment_backup) {

    private var fragmentBackupBinding: FragmentBackupBinding? = null

    private lateinit var auth: FirebaseAuth

    private var isLocal = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = view.let { FragmentBackupBinding.bind(it) }
        fragmentBackupBinding = binding

        val sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        isLocal = sharedPref.getBoolean("isLocal", true)

        auth = Firebase.auth
        
        binding.deleteBtn?.visibility = View.GONE

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.authLayout?.visibility = View.GONE
            binding.deleteBtn?.visibility = View.VISIBLE
            updateUI(currentUser, binding)
        }

        if (isLocal) {
            binding.toggleButton?.check(R.id.button1)
            binding.cloudLayout?.visibility = View.GONE
            binding.localLayout?.visibility = View.VISIBLE
        } else {
            binding.toggleButton?.check(R.id.button2)
            binding.cloudLayout?.visibility = View.VISIBLE
            binding.localLayout?.visibility = View.GONE
        }

        binding.toggleButton?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button1 -> {
                    if (isChecked) {
                        with(sharedPref.edit()) {
                            putBoolean("isLocal", true)
                            apply()
                        }
                        binding.cloudLayout?.visibility = View.GONE
                        binding.localLayout?.visibility = View.VISIBLE
                        isLocal = true
                    }

                }

                R.id.button2 -> {
                    if (isChecked) {
                        with(sharedPref.edit()) {
                            putBoolean("isLocal", false)
                            apply()
                        }
                        binding.cloudLayout?.visibility = View.VISIBLE
                        binding.localLayout?.visibility = View.GONE
                        isLocal = false
                    }
                }
            }
        }

        binding.regBtn?.setOnClickListener {
            register(binding.emailEditText?.text.toString(), binding.passwordEditText?.text.toString(), binding)
        }

        binding.logBtn?.setOnClickListener {
            login(binding.emailEditText?.text.toString(), binding.passwordEditText?.text.toString(), binding)
        }

        binding.resetBtn?.setOnClickListener {
            resetPassword(binding.emailEditText?.text.toString())
        }

        binding.backup.setOnClickListener {
            backup(isLocal)
        }

        binding.rest.setOnClickListener {
            restore(isLocal)
        }

    }

    private fun backup(local: Boolean){
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
                        if (success) {}
                    }
                }
                .backup()
        } else {
            val userId = Firebase.auth.currentUser?.uid
            backup
                .database(NoteDatabase.getDatabase(requireContext()))
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                .customBackupFileName("database_test")
                .backupIsEncrypted(true)
                .customEncryptPassword(userId.toString())
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

            storageRef.child("user/$userId/test.aes").putFile(
                File(
                    requireContext().filesDir,
                    "databasebackup/database_test.aes"
                ).toUri()
            )
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

            val myFile = File(storagePath, "database_test.aes")

            val dbRef = storageRef.child("user/$userId/test.aes")
            dbRef.getFile(myFile)

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
        }
    }

    private fun register(email: String, password: String, binding: FragmentBackupBinding){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user, binding)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null, binding)
                }
            }
    }

    private fun login(email: String, password: String, binding: FragmentBackupBinding){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user, binding)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null, binding)
                }
            }

    }

    private fun resetPassword(email: String){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Email sent.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?, binding: FragmentBackupBinding){
        user?.let {
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            binding.userEmailTxt?.text = email
            binding.authLayout?.visibility = View.GONE

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