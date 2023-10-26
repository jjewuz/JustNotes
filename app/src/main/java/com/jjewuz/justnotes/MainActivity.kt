package com.jjewuz.justnotes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
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

        actionBarToggle = ActionBarDrawerToggle(this, binding.drawer, 0, 0)

        binding.drawer.addDrawerListener(actionBarToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBarToggle.syncState()

        navView = findViewById(R.id.nv)
        val head = navView.getHeaderView(0)

        val ver = resources.getString(R.string.appversion)

        head.findViewById<TextView>(R.id.author).text = "$ver ${BuildConfig.VERSION_NAME}"

        backup = RoomBackup(this)

        val notesTxt = resources.getString(R.string.notes)
        val todoTxt = resources.getString(R.string.todo)
        val backupTxt = resources.getString(R.string.app_name)

        if (tasksDefault) {
            supportActionBar?.title = todoTxt
            binding.nv.setCheckedItem(R.id.todo)
            replaceFragment(TodoFragment())
        } else {
            replaceFragment(NotesFragment())
        }

        if (sharedPref.getInt("update1", 0) == 0) {
            MaterialAlertDialogBuilder(this)
                .setTitle("${resources.getString(R.string.what_new_update)} ${BuildConfig.VERSION_NAME}?")
                .setMessage("${resources.getString(R.string.what_new_desc)}:  \"${resources.getString(R.string.set_on_widget)}\".")
                .setNegativeButton(R.string.notshowanymore) { dialog, which ->
                    sharedPref.edit().putInt("update1", 1).apply()
                }
                .setPositiveButton("OK") { dialog, which ->
                }
                .show()
        }


        val enabledpass = sharedPref.getBoolean("enabledPassword", false)

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

    }

    override fun onSupportNavigateUp(): Boolean {
        binding.drawer.openDrawer(navView)
        return true
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }
}
