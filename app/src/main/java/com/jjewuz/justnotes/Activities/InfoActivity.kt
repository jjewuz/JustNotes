package com.jjewuz.justnotes.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jjewuz.justnotes.BuildConfig
import com.jjewuz.justnotes.R


class InfoActivity : AppCompatActivity() {

    private lateinit var ghBtn: LinearLayout
    private lateinit var sourceBtn: LinearLayout
    private lateinit var siteBtn: LinearLayout
    private lateinit var weblateBtn: LinearLayout
    private lateinit var licenseBtn: Button

    private lateinit var tou: Button
    private lateinit var pp: Button

    private lateinit var  tgBtn: ImageButton
    private lateinit var  vkBtn: ImageButton

    private lateinit var versionTxt: TextView

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setContentView(R.layout.activity_info)
        setSupportActionBar(findViewById(R.id.topAppBar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        enableEdgeToEdge()

        ghBtn = findViewById(R.id.github_profile)
        sourceBtn = findViewById(R.id.github_code)
        siteBtn = findViewById(R.id.site)
        weblateBtn = findViewById(R.id.weblate)
        licenseBtn = findViewById(R.id.licenses)

        tou = findViewById(R.id.terms_of_use)
        pp = findViewById(R.id.privacy_policy)
        tgBtn = findViewById(R.id.tg)
        vkBtn = findViewById(R.id.vk)

        versionTxt = findViewById(R.id.version)

        versionTxt.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        ghBtn.setOnClickListener { openLink("https://github.com/jjewuz/JustNotes") }
        sourceBtn.setOnClickListener { openLink("https://github.com/jjewuz") }
        siteBtn.setOnClickListener{ openLink("https://jjewuz.ru/justnotes/justnotes.html") }
        weblateBtn.setOnClickListener {openLink("https://hosted.weblate.org/engage/justnotes/")}
        licenseBtn.setOnClickListener { startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }
        tou.setOnClickListener { openLink("https://jjewuz.ru/justnotes/termsofuse.html") }
        pp.setOnClickListener { openLink("https://jjewuz.ru/justnotes/privacypolicy.html") }
        tgBtn.setOnClickListener { openLink("https://t.me/jjewuz_support") }
        vkBtn.setOnClickListener { openLink("https://vk.com/jjewuzhub") }


    }

    private fun openLink(url: String){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(i)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return false
    }

}