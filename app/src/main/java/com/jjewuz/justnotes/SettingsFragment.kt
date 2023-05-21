package com.jjewuz.justnotes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar


class SettingsFragment : Fragment() {

    private lateinit var version: TextView
    private lateinit var passSwitch: MaterialSwitch
    private lateinit var fontSwitch: MaterialSwitch
    private lateinit var monetSwitch: MaterialSwitch
    private lateinit var reverseSwitch: MaterialSwitch
    private lateinit var tasksSwitch: MaterialSwitch
    private lateinit var reportBtn: Button

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        passSwitch = v.findViewById(R.id.passwordtoggle)
        fontSwitch = v.findViewById(R.id.fonttoggle)
        monetSwitch = v.findViewById(R.id.monettoggle)
        reverseSwitch = v.findViewById(R.id.reversetoggle)
        tasksSwitch = v.findViewById(R.id.opentoggle)
        reportBtn = v.findViewById(R.id.sharebug)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            passSwitch.visibility = View.GONE
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            monetSwitch.visibility = View.GONE
        }
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        val reversed = sharedPref.getBoolean("reversed", false)
        val tasksOpen = sharedPref.getBoolean("isTask", false)

        passSwitch.isChecked = enabledpass
        fontSwitch.isChecked = enabledFont
        monetSwitch.isChecked = enabledMonet
        reverseSwitch.isChecked = reversed
        tasksSwitch.isChecked = tasksOpen

        reportBtn.setOnClickListener { val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes/issues/new"))
            startActivity(i) }

        tasksSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("isTask", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("isTask", false)
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
            Snackbar.make(v, resources.getString(R.string.needRestart), Snackbar.LENGTH_LONG)
                .setAction(R.string.restart) {
                    val intent = Intent(activity, requireActivity()::class.java)
                    this.startActivity(intent)
                    activity?.finishAffinity()
                }
                .show()
        }

        monetSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledMonet", true)
                    apply()
                }
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledMonet", false)
                    apply()
                }
            }
            Snackbar.make(v, resources.getString(R.string.needRestart), Snackbar.LENGTH_LONG)
                .setAction(R.string.restart) {
                    val intent = Intent(activity, requireActivity()::class.java)
                    this.startActivity(intent)
                    activity?.finishAffinity()
                }
                .show()
        }

        val ver = resources.getString(R.string.appversion)
        val build = resources.getString(R.string.buildn)



        version = v.findViewById(R.id.version)

        version.text = "$ver ${BuildConfig.VERSION_NAME} \n$build ${BuildConfig.VERSION_CODE}"

        return v
    }
}