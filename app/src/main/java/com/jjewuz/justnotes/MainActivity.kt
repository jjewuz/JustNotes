package com.jjewuz.justnotes

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.jjewuz.justnotes.databinding.ActivityMainBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
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

        var k = 0

        val appCard = view.findViewById<MaterialCardView>(R.id.app_card)
        val sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val already = sharedPref.getBoolean("is_dev", false)
        appCard.setOnClickListener {
            k+=1
            if (k==6 && !already){
                with(sharedPref.edit()){
                    putBoolean("is_dev", true)
                    apply()
                }
                Toast.makeText(requireContext(), resources.getString(R.string.beta_mode_activated), Toast.LENGTH_LONG).show()
            }
        }
        val appVer = view.findViewById<TextView>(R.id.app_build)
        val appLabel = view.findViewById<TextView>(R.id.app_label)
        if (already){
            appLabel.text = "${resources.getString(R.string.app_name)} β"
            appVer.text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        } else{
            appVer.text = "v${BuildConfig.VERSION_NAME}"
        }

        val viewModel = ViewModelProvider(this)[NoteViewModal::class.java]

        val notesCountText = view.findViewById<TextView>(R.id.notes_count)
        val notesText = resources.getString(R.string.total_notes)
        val lastBackupText = view.findViewById<TextView>(R.id.last_backup)

        viewModel.getNotes().observe(this, Observer { notes ->
            val notesCount = notes.size
            notesCountText.text = "$notesText $notesCount"
        })
        lastBackupText.text = sharedPref.getString("last_backup", "${resources.getString(R.string.last_backup)} - ")

        val backupCard = view.findViewById<MaterialCardView>(R.id.backup_card)
        val settingsCard = view.findViewById<MaterialCardView>(R.id.settings_card)
        val infoCard = view.findViewById<MaterialCardView>(R.id.info_card)

        backupCard.setOnClickListener { replaceFragment(BackupFragment())
            this.dismiss()}
        settingsCard.setOnClickListener { replaceFragment(SettingsFragment())
            this.dismiss()}
        infoCard.setOnClickListener { replaceFragment(InfoFragment())
            this.dismiss()}
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.addToBackStack(fragment.tag)
        fragmentTransaction.commit ()
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    lateinit var sharedPref: SharedPreferences

    lateinit var backup: RoomBackup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
        val tasksDefault = sharedPref.getBoolean("isTask", false)
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById(R.id.topAppBar))

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
                .setPositiveButton(R.string.agree) { dialog, id ->
                    sharedPref.edit().putBoolean("agree_conditions2023", true).apply()
                }
                .setNegativeButton(R.string.decline) { dialog, id ->
                    this.finish()
                }
            builder.create().show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(application)
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

        val isRecreate = sharedPref.getBoolean("recreate", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (enabledpass && !isRecreate) {
                biometricPrompt.authenticate(promptInfo)
                sharedPref.edit().putBoolean("recreate", false).apply()
            }
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
            .addOnFailureListener { e ->
                //TODO
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

}
