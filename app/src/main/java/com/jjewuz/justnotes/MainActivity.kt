package com.jjewuz.justnotes

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.jjewuz.justnotes.databinding.ActivityMainBinding
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var sharedPref: SharedPreferences

    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView

    lateinit var backup: RoomBackup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        val tasksDefault = sharedPref.getBoolean("isTask", false)
        if (enabledFont and enabledMonet) {
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and enabledMonet) {
            setTheme(R.style.FontMonet)
        } else if (!enabledFont and !enabledMonet) {
            setTheme(R.style.Font)
        } else {
            setTheme(R.style.Nothing)
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

        actionBarToggle = ActionBarDrawerToggle(this, binding.drawer, 0, 0)

        binding.drawer.addDrawerListener(actionBarToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBarToggle.syncState()

        navView = findViewById(R.id.nv)
        val head = navView.getHeaderView(0)

        head.findViewById<TextView>(R.id.author).text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        backup = RoomBackup(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
        })


        val notesTxt = resources.getString(R.string.notes)
        val todoTxt = resources.getString(R.string.todo)
        val backupTxt = resources.getString(R.string.backup_title)

        if (tasksDefault) {
            supportActionBar?.title = todoTxt
            binding.nv.setCheckedItem(R.id.todo)
            replaceFragment(TodoFragment())
        } else {
            replaceFragment(NotesFragment())
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

        binding.apply {
            nv.setNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.notes -> {
                        supportActionBar?.title = notesTxt
                        replaceFragment(NotesFragment())
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.todo ->{
                        supportActionBar?.title = todoTxt
                        replaceFragment(TodoFragment())
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.back_up ->{
                        supportActionBar?.title = backupTxt
                        replaceFragment(BackupFragment())
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.settings ->{
                        supportActionBar?.title = resources.getString(R.string.settingsText)
                        replaceFragment(SettingsFragment())
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.info ->{
                        supportActionBar?.title = resources.getString(R.string.inf)
                        replaceFragment(InfoFragment())
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    else -> { true}
                }
            }
        }


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (enabledpass) {
                biometricPrompt.authenticate(promptInfo)
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
    }

    override fun onSupportNavigateUp(): Boolean {
        binding.drawer.openDrawer(navView)
        return true
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
