package com.jjewuz.justnotes.Activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jjewuz.justnotes.R

private lateinit var sharedPref: SharedPreferences

class BackupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val theme = sharedPref.getString("theme", "standart")
        val securedNote = sharedPref.getBoolean("screenSecurity", false)
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
        setContentView(R.layout.activity_backup)
    }
}