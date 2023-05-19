package com.jjewuz.justnotes

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment() {

    private lateinit var version: TextView
    private lateinit var passSwitch: MaterialSwitch
    private lateinit var fontSwitch: MaterialSwitch
    private lateinit var monetSwitch: MaterialSwitch

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        passSwitch = v.findViewById(R.id.passwordtoggle)
        fontSwitch = v.findViewById(R.id.fonttoggle)
        monetSwitch = v.findViewById(R.id.monettoggle)

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

        passSwitch.isChecked = enabledpass
        fontSwitch.isChecked = enabledFont
        monetSwitch.isChecked = enabledMonet

        passSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", true)
                    apply()
                }
                Toast.makeText(requireActivity(), resources.getString(R.string.passEnable), Toast.LENGTH_SHORT).show()
            }else{
                with (sharedPref.edit()) {
                    putBoolean("enabledPassword", false)
                    apply()
                }
                Toast.makeText(requireActivity(), resources.getString(R.string.passDisabled), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireActivity(), resources.getString(R.string.needRestart), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireActivity(), resources.getString(R.string.needRestart), Toast.LENGTH_SHORT).show()
        }


        version = v.findViewById(R.id.version)

        version.text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        return v
    }
}