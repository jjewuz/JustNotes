package com.jjewuz.justnotes.Activities

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import com.jjewuz.justnotes.Fragments.NotesFragment
import com.jjewuz.justnotes.Fragments.TodoFragment
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Notes.NoteViewModal
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.databinding.ActivityMainBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

class ModalBottomSheet: BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.main_menu, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val viewModel = ViewModelProvider(this)[NoteViewModal::class.java]

        val notesCountText = view.findViewById<TextView>(R.id.notes_count)
        val notesText = resources.getString(R.string.total_notes)
        val lastBackupText = view.findViewById<TextView>(R.id.last_backup)
        val jmCard = view.findViewById<MaterialCardView>(R.id.ad_card)
        jmCard.setOnClickListener{
            val url = "https://play.google.com/store/apps/details?id=com.jjewuz.executor"
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(i)
        }

        viewModel.getNotes().observe(this, Observer { notes ->
            val notesCount = notes.size
            notesCountText.text = "$notesText $notesCount"
        })
        lastBackupText.text = sharedPref.getString("last_backup", "${resources.getString(R.string.last_backup)} - ")

        val backupCard = view.findViewById<MaterialCardView>(R.id.backup_card)
        val settingsCard = view.findViewById<MaterialCardView>(R.id.settings_card)
        val infoCard = view.findViewById<MaterialCardView>(R.id.info_card)

        backupCard.setOnClickListener {
            val modalBottomSheet = BackupUI()
            modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
            this.dismiss()}
        settingsCard.setOnClickListener {
            val intent = Intent(requireActivity(), SettingsActivity::class.java)
            startActivity(intent)
            this.dismiss()}
        infoCard.setOnClickListener {
            val intent = Intent(requireActivity(), InfoActivity::class.java)
            startActivity(intent)
            this.dismiss()}
    }
}

class BackupUI: BottomSheetDialogFragment() {

    private lateinit var lastBackupText: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var autoSwitch: MaterialSwitch
    private lateinit var lastBackup: TextView

    private lateinit var userEmailTxt: TextView
    private lateinit var authLayout: LinearLayout
    private lateinit var logoutBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var cloudButtons: LinearLayout

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_backup, container, false)

    companion object {
        const val TAG = "BackupUI"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)


        auth = Firebase.auth
        autoSwitch = view.findViewById(R.id.auto_backup_switch)
        lastBackup = view.findViewById(R.id.last_backup)
        deleteBtn = view.findViewById(R.id.delete_btn)
        logoutBtn = view.findViewById(R.id.logout_btn)
        cloudButtons = view.findViewById(R.id.cloud_buttons)
        userEmailTxt = view.findViewById(R.id.userEmailTxt)
        authLayout = view.findViewById(R.id.auth_layout)

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)

        deleteBtn.visibility = View.GONE
        logoutBtn.visibility = View.GONE
        cloudButtons.visibility = View.GONE
        autoSwitch.visibility = View.GONE



        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }

        view.findViewById<Button>(R.id.reg_btn).setOnClickListener {
            register(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        view.findViewById<Button>(R.id.log_btn).setOnClickListener {
            login(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        view.findViewById<Button>(R.id.reset_btn).setOnClickListener {
            resetPassword(emailEditText.text.toString())
        }

        view.findViewById<Button>(R.id.backup).setOnClickListener {
            backup(true)
        }

        view.findViewById<Button>(R.id.rest).setOnClickListener {
            restore(true)
        }

        view.findViewById<Button>(R.id.backup_cloud).setOnClickListener {
            backup(false)
        }

        view.findViewById<Button>(R.id.rest_cloud).setOnClickListener {
            restore(false)
        }

        view.findViewById<Button>(R.id.logout_btn).setOnClickListener {
            auth.signOut()
            userEmailTxt.text = resources.getString(R.string.no_account)
            authLayout.visibility = View.VISIBLE
            logoutBtn.visibility = View.GONE
            deleteBtn.visibility = View.GONE
            cloudButtons.visibility = View.GONE
            autoSwitch.visibility = View.GONE
        }

        autoSwitch.isChecked = sharedPref.getBoolean("auto_backup", false)

        view.findViewById<Button>(R.id.delete_btn).setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delete_account)
                .setMessage(R.string.account_delete_info)
                .setPositiveButton(R.string.delete_account) { _, _ ->
                    val userId = Firebase.auth.currentUser?.uid
                    val userEmail = Firebase.auth.currentUser?.email
                    val storageRef = Firebase.storage.reference
                    storageRef.child("user/$userId/database.aes").delete()
                    auth.currentUser?.delete()?.addOnSuccessListener {
                        userEmailTxt.text = resources.getString(R.string.no_account)
                        authLayout.visibility = View.VISIBLE
                        logoutBtn.visibility = View.GONE
                        deleteBtn.visibility = View.GONE
                        cloudButtons.visibility = View.GONE
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
                                        storageRef.child("user/$userId/database.aes").delete()
                                        auth.currentUser?.delete()?.addOnSuccessListener {
                                            userEmailTxt.text = resources.getString(R.string.no_account)
                                            authLayout.visibility = View.VISIBLE
                                            logoutBtn.visibility = View.GONE
                                            deleteBtn.visibility = View.GONE
                                            cloudButtons.visibility = View.GONE
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
        lastBackupText = lastBackup
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

    private fun register(email: String, password: String){
        if (email != "" && password != "") {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
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
                        updateUI(null)
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

    private fun login(email: String, password: String){
        if (email != "" && password != "") {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
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
                        updateUI(null)
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

    private fun updateUI(user: FirebaseUser?){
        user?.let {
            val email = it.email

            userEmailTxt.text = email
            authLayout.visibility = View.GONE
            logoutBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            cloudButtons.visibility = View.VISIBLE
            autoSwitch.visibility = View.VISIBLE
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var sharedPref: SharedPreferences

    lateinit var backup: RoomBackup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val tasksDefault = sharedPref.getBoolean("isTask", false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
        if (enabledFont and (theme=="monet")) {
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and (theme=="monet")) {
            setTheme(R.style.FontMonet)
        } else if (!enabledFont and (theme=="standart")) {
            setTheme(R.style.Font)
        } else if (enabledFont and (theme=="standart")) {
            setTheme(R.style.Nothing)
        } else if (!enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIceFont)
        } else if (enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIce)
        }
        enableEdgeToEdge()
        actionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 100)
        }


        backup = RoomBackup(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
        })


        if (savedInstanceState == null) {
            if (tasksDefault) {
                replaceFragment(TodoFragment())
            } else {
                replaceFragment(NotesFragment())
            }
        }

        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        val newPP = sharedPref.getBoolean("agree_conditions2023", false)

        if (!newPP){
            val builder = MaterialAlertDialogBuilder(this)
            val inflater = this.layoutInflater.inflate(R.layout.contions, null)
            val tou = inflater.findViewById<Button>(R.id.terms_of_use)
            tou.setOnClickListener { openLink("https://jjewuz.ru/justnotes/termsofuse.html") }
            val pp = inflater.findViewById<Button>(R.id.privacy_policy)
            pp.setOnClickListener { openLink("https://jjewuz.ru/justnotes/privacypolicy.html") }
            builder.setView(inflater)
                .setTitle(R.string.agreement)
                .setIcon(R.drawable.info)
                .setCancelable(false)
                .setPositiveButton(R.string.agree) { _, _ ->
                    sharedPref.edit().putBoolean("agree_conditions2023", true).apply()
                }
                .setNegativeButton(R.string.decline) { _, _ ->
                    this.finish()
                }
            builder.create().show()
        }

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(application)
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intenta ->
                    intenta.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    this.startActivity(intenta)
                }
            }
        }

        val loginErr = resources.getString(R.string.authError)
        val noPasswordErr = resources.getString(R.string.noPassError)


        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                    if (errorCode == 11){
                        Toast.makeText(applicationContext,
                            noPasswordErr, Toast.LENGTH_SHORT)
                            .show()
                    }else{
                        Toast.makeText(applicationContext,
                            "$loginErr $errString" , Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext,
                        resources.getString(R.string.authSuc), Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    finish()
                    Toast.makeText(applicationContext, resources.getString(R.string.authFail),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(resources.getString(R.string.loginTitle))
                .setSubtitle(resources.getString(R.string.loginText))
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(resources.getString(R.string.loginTitle))
                .setSubtitle(resources.getString(R.string.loginText))
                .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                .build()
        }


        if (enabledpass) {
            biometricPrompt.authenticate(promptInfo)
        }


        val appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    1
                )
            }
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.update_downloaded),
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction(resources.getString(R.string.restart)) { appUpdateManager.completeUpdate() }
                    show()
                }
            }
        }

        val name = getString(R.string.reminders)
        val descriptionText = getString(R.string.reminders)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("0", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        val user = Firebase.auth.currentUser
        if (user != null) {
            if (sharedPref.getBoolean("auto_backup", false)) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val currDate: String = sdf.format(Date().time)

                if (sharedPref.getString("last_backup_text", "dd MMM yyyy - HH:mm")
                        ?.dropLast(8) != currDate
                ) {

                    backup()
                    sharedPref.edit().putString("last_backup_text", currDate).apply()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.place_holder)
        outState.putString("currentFragmentTag", currentFragment?.tag)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        val currentFragmentTag = savedInstanceState?.getString("currentFragmentTag")
        val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
        if (fragment != null) {
            replaceFragment(fragment)
        }
    }

    private fun openLink(url: String){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(i)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }


    private fun backup() {
        val backup = this.backup
        val storageRef = Firebase.storage.reference

        val userId = Firebase.auth.currentUser?.uid
        backup
            .database(NoteDatabase.getDatabase(this))
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
                }
            }
            .backup()

        storageRef.child("user/$userId/database.aes").putFile(
            File(
                this.filesDir,
                "databasebackup/database.aes"
            ).toUri()
        ).addOnSuccessListener {
            val sdf2 = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
            val currentDateAndTime2: String = sdf2.format(Date().time)
            val currentTime =
                "${resources.getString(R.string.last_backup)} $currentDateAndTime2"
            with(sharedPref.edit()) {
                putString("last_backup_time", currentDateAndTime2)
                putString("last_backup", currentTime)
                apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
        if (enabledFont and (theme=="monet")) {
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and (theme=="monet")) {
            setTheme(R.style.FontMonet)
        } else if (!enabledFont and (theme=="standart")) {
            setTheme(R.style.Font)
        } else if (enabledFont and (theme=="standart")) {
            setTheme(R.style.Nothing)
        } else if (!enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIceFont)
        } else if (enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIce)
        }
    }

}
