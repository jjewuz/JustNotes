package com.jjewuz.justnotes.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.jjewuz.justnotes.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


private lateinit var sharedPref: SharedPreferences
private lateinit var passSwitch: MaterialSwitch
private lateinit var secCard: MaterialCardView
private lateinit var fontSwitch: MaterialSwitch
private lateinit var monetCard: MaterialCardView
private lateinit var previewSwitch: MaterialSwitch
private lateinit var openGroup: MaterialButtonToggleGroup
private lateinit var reportBtn: MaterialCardView
private lateinit var screenSecuredNotes: MaterialSwitch
private lateinit var labelsEdit: MaterialCardView
private lateinit var betaCardView: MaterialCardView
private lateinit var betaSwitch: MaterialSwitch


class SettingsActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()
        setSupportActionBar(findViewById(R.id.topAppBar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        passSwitch = findViewById(R.id.passwordtoggle)
        fontSwitch = findViewById(R.id.fonttoggle)
        monetCard = findViewById(R.id.monet)
        secCard = findViewById(R.id.security)
        previewSwitch = findViewById(R.id.previewtoggle)
        openGroup = findViewById(R.id.toggleButton)
        reportBtn = findViewById(R.id.report)
        screenSecuredNotes = findViewById(R.id.screenshottoggle)
        labelsEdit = findViewById(R.id.labels_edit)
        betaCardView = findViewById(R.id.betacard)
        betaSwitch = findViewById(R.id.betatoggle)


        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        val preview = sharedPref.getBoolean("enabledPreview", false)
        val tasksOpen = sharedPref.getBoolean("isTask", false)
        val isSecuredScreen = sharedPref.getBoolean("screenSecurity", false)
        val isDev = sharedPref.getBoolean("is_dev", false)

        if (!isDev){
            betaCardView.visibility = View.GONE
        }

        passSwitch.isChecked = enabledpass
        fontSwitch.isChecked = enabledFont
        previewSwitch.isChecked = preview
        screenSecuredNotes.isChecked = isSecuredScreen

        if (tasksOpen){
            openGroup.check(R.id.openTasks)
        } else {
            openGroup.check(R.id.yesnotes)
        }

        reportBtn.setOnClickListener { val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes/issues/new"))
            startActivity(i) }



        openGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId){
                R.id.yesnotes -> {
                    if (isChecked) {
                        with (sharedPref.edit()) {
                            putBoolean("isTask", false)
                            apply()
                        }
                    }

                }
                R.id.openTasks -> {
                    if (isChecked) {
                        with(sharedPref.edit()) {
                            putBoolean("isTask", true)
                            apply()
                        }
                    }
                }

            }
        }

        previewSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledPreview", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledPreview", false)
                    apply()
                }
            }
            Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
            restartApp()
        }

        screenSecuredNotes.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("screenSecurity", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("screenSecurity", false)
                    apply()
                }
            }
        }

        passSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", true)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.passEnable), Toast.LENGTH_SHORT)
                    .show()
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", false)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.passDisabled), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        fontSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledFont", true)
                    putBoolean("recreate", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledFont", false)
                    putBoolean("recreate", true)
                    apply()
                }
            }
            Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
            restartApp()
        }

        monetCard.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val inf = this.layoutInflater.inflate(R.layout.theme_chooser, null)
            val standart = inf.findViewById<MaterialCardView>(R.id.standart)
            val monet = inf.findViewById<MaterialCardView>(R.id.monet)
            val ice = inf.findViewById<MaterialCardView>(R.id.ice)

            val colorMonet = inf.findViewById<Button>(R.id.monetcolor)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                monet.visibility = View.GONE
            } else {
                colorMonet.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this,
                    R.color.material_dynamic_primary50
                ))
            }
            builder.setIcon(R.drawable.palette)
            builder.setTitle(R.string.select_theme)
            builder.setView(inf)
                .setPositiveButton(R.string.close) { _, _ ->
                }
            builder.create()
            val dialog = builder.show()
            standart.setOnClickListener {
                with (sharedPref.edit()) {
                    putString("theme", "standart")
                    putBoolean("recreate", true)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
                restartApp()
            }
            monet.setOnClickListener {
                with (sharedPref.edit()) {
                    putString("theme", "monet")
                    putBoolean("recreate", true)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
                restartApp()
            }
            ice.setOnClickListener {
                with (sharedPref.edit()) {
                    putString("theme", "ice")
                    putBoolean("recreate", true)
                    apply()
                }
                Toast.makeText(this, resources.getString(R.string.app_restarting), Toast.LENGTH_SHORT).show()
                restartApp()
            }
        }

        labelsEdit.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val inf = this.layoutInflater.inflate(R.layout.label_edit, null)
            val field1 = inf.findViewById<TextInputEditText>(R.id.label1_edit)
            val field2 = inf.findViewById<TextInputEditText>(R.id.label2_edit)
            val field3 = inf.findViewById<TextInputEditText>(R.id.label3_edit)

            field1.setText(sharedPref.getString("label1", ""))
            field2.setText(sharedPref.getString("label2", ""))
            field3.setText(sharedPref.getString("label3", ""))

            val save1 = inf.findViewById<ImageButton>(R.id.save1)
            val save2 = inf.findViewById<ImageButton>(R.id.save2)
            val save3 = inf.findViewById<ImageButton>(R.id.save3)

            save1.setOnClickListener {
                if (field1.text?.length!! <= 15){
                    with (sharedPref.edit()){
                        putString("label1", field1.text.toString())
                        apply()
                    }
                    Toast.makeText(this, resources.getString(R.string.is_saved), Toast.LENGTH_SHORT ).show()
                }
                else {
                    Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT ).show()
                }
            }

            save2.setOnClickListener {
                if (field2.text?.length!! <= 15){
                    with (sharedPref.edit()){
                        putString("label2", field2.text.toString())
                        apply()
                    }
                    Toast.makeText(this, resources.getString(R.string.is_saved), Toast.LENGTH_SHORT ).show()
                } else {
                    Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT ).show()
                }
            }

            save3.setOnClickListener {
                if (field3.text?.length!! <= 15){
                    with (sharedPref.edit()){
                        putString("label3", field3.text.toString())
                        apply()
                    }
                    Toast.makeText(this, resources.getString(R.string.is_saved), Toast.LENGTH_SHORT ).show()
                } else {
                    Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT ).show()
                }
            }

            builder.setIcon(R.drawable.label)
            builder.setTitle(R.string.set_labels)
            builder.setView(inf)
                .setPositiveButton(R.string.close) { _, _ ->

                }
            builder.create()
            builder.show()
        }

        betaSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("is_dev", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("is_dev", false)
                    apply()
                }
            }
        }

    }

    private fun restartApp() = runBlocking {
        launch {
            delay(1500L)
            val ctx = applicationContext
            val pm = ctx.packageManager
            val intent = pm.getLaunchIntentForPackage(ctx.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
            ctx.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return false
    }
}