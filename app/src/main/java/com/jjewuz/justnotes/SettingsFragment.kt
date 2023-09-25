package com.jjewuz.justnotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar


class SettingsFragment : Fragment() {

    private lateinit var passSwitch: MaterialSwitch
    private lateinit var secCard: LinearLayout
    private lateinit var fontSwitch: MaterialSwitch
    private lateinit var monetSwitch: MaterialSwitch
    private lateinit var monetCard: MaterialCardView
    private lateinit var previewSwitch: MaterialSwitch
    private lateinit var reverseSwitch: MaterialSwitch
    private lateinit var openGroup: MaterialButtonToggleGroup
    private lateinit var backBtn: Button

    private lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)

        passSwitch = v.findViewById(R.id.passwordtoggle)
        fontSwitch = v.findViewById(R.id.fonttoggle)
        monetSwitch = v.findViewById(R.id.monettoggle)
        monetCard = v.findViewById(R.id.monet)
        secCard = v.findViewById(R.id.security)
        previewSwitch = v.findViewById(R.id.previewtoggle)
        reverseSwitch = v.findViewById(R.id.reversetoggle)
        openGroup = v.findViewById(R.id.toggleButton)
        backBtn = v.findViewById(R.id.backBtn)


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            secCard.visibility = View.GONE
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            monetCard.visibility = View.GONE
        }

        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabledpass = sharedPref.getBoolean("enabledPassword", false)
        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        val preview = sharedPref.getBoolean("enabledPreview", false)
        val reversed = sharedPref.getBoolean("reversed", false)
        val tasksOpen = sharedPref.getBoolean("isTask", false)
        val isGrid = sharedPref.getBoolean("grid", false)

        passSwitch.isChecked = enabledpass
        fontSwitch.isChecked = enabledFont
        monetSwitch.isChecked = enabledMonet
        reverseSwitch.isChecked = reversed

        if (tasksOpen){
            openGroup.check(R.id.openTasks)
        } else {
            openGroup.check(R.id.yesnotes)
        }

        backBtn.setOnClickListener { replaceFragment(OtherFragment()) }



        openGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            Log.i("BUG", "Button $checkedId")
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
                    killAndRestartApp(requireActivity())
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
                    killAndRestartApp(requireActivity())
                }
                .show()
        }

        return v
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }

    fun killAndRestartApp(activity: Activity) {

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }
}