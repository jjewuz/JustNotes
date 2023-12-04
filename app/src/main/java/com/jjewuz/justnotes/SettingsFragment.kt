package com.jjewuz.justnotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar


class SettingsFragment : Fragment() {

    private lateinit var passSwitch: MaterialSwitch
    private lateinit var secCard: MaterialCardView
    private lateinit var fontSwitch: MaterialSwitch
    private lateinit var monetCard: MaterialCardView
    private lateinit var previewSwitch: MaterialSwitch
    private lateinit var reverseSwitch: MaterialSwitch
    private lateinit var openGroup: MaterialButtonToggleGroup
    private lateinit var reportBtn: MaterialCardView
    private lateinit var screenSecuredNotes: MaterialSwitch

    private lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        passSwitch = v.findViewById(R.id.passwordtoggle)
        fontSwitch = v.findViewById(R.id.fonttoggle)
        monetCard = v.findViewById(R.id.monet)
        secCard = v.findViewById(R.id.security)
        previewSwitch = v.findViewById(R.id.previewtoggle)
        reverseSwitch = v.findViewById(R.id.reversetoggle)
        openGroup = v.findViewById(R.id.toggleButton)
        reportBtn = v.findViewById(R.id.report)
        screenSecuredNotes = v.findViewById(R.id.screenshottoggle)


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            secCard.visibility = View.GONE
        }

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        val preview = sharedPref.getBoolean("enabledPreview", false)
        val reversed = sharedPref.getBoolean("reversed", false)
        val tasksOpen = sharedPref.getBoolean("isTask", false)
        val isGrid = sharedPref.getBoolean("grid", false)
        val isSecuredScreen = sharedPref.getBoolean("screenSecurity", false)

        passSwitch.isChecked = enabledpass
        fontSwitch.isChecked = enabledFont
        previewSwitch.isChecked = preview
        reverseSwitch.isChecked = reversed
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
        }

        reverseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("reversed", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("reversed", false)
                    apply()
                }
            }
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
                Snackbar.make(v, resources.getString(R.string.passEnable), Snackbar.LENGTH_SHORT)
                    .show()
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", false)
                    apply()
                }
                Snackbar.make(v, resources.getString(R.string.passDisabled), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        fontSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledFont", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledFont", false)
                    apply()
                }
            }
            activity?.recreate()
        }

        monetCard.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            val inf = requireActivity().layoutInflater.inflate(R.layout.theme_chooser, null)
            val standart = inf.findViewById<MaterialCardView>(R.id.standart)
            val monet = inf.findViewById<MaterialCardView>(R.id.monet)
            val ice = inf.findViewById<MaterialCardView>(R.id.ice)

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
                    apply()
                }
                activity?.recreate()
            }
            monet.setOnClickListener {
                with (sharedPref.edit()) {
                    putString("theme", "monet")
                    apply()
                }
                activity?.recreate()
            }
            ice.setOnClickListener {
                with (sharedPref.edit()) {
                    putString("theme", "ice")
                    apply()
                }
                activity?.recreate()
            }
        }

        return v
    }

}