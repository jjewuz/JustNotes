package com.jjewuz.justnotes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.play.core.review.ReviewManagerFactory

class Editor : AppCompatActivity() {


    private lateinit var version: TextView
    private lateinit var passSwitch: MaterialSwitch
    private lateinit var cardSettings: MaterialCardView

    private lateinit var sharedPref: SharedPreferences

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById(R.id.topAppBar))

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        cardSettings = findViewById(R.id.card_settings)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            cardSettings.visibility = View.GONE
        }


        passSwitch = findViewById(R.id.passwordtoggle)

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledpass = sharedPref.getBoolean("enabledPassword", false)

        passSwitch.isChecked = enabledpass

        passSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", true)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.passEnable), Toast.LENGTH_SHORT).show()
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", false)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.passDisabled), Toast.LENGTH_SHORT).show()
            }
        }

        version = findViewById(R.id.version)

        version.text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }


    fun vk_a(view: View){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/jjewuzhub"))
        startActivity(i)
    }

    fun tg_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jjewuzhub"))
                startActivity(i)
    }

    fun ds_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jNHNBdYuAR"))
        startActivity(i)
    }

    fun gh_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes"))
                startActivity(i)
    }

    fun site_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
                startActivity(i)
    }

    fun ratestore(view: View) {

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
        startActivity(i)
    }

    fun boostyBtn(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jjewuz.justnotes"))
        startActivity(i)
    }

    fun close(){
        this.finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        close()
        return false
    }



}

