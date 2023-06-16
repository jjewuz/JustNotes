package com.jjewuz.justnotes

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView


class OtherFragment : Fragment() {
    private lateinit var reportBtn: MaterialCardView
    private lateinit var backupBtn: MaterialCardView
    private  lateinit var settingsBtn: MaterialCardView
    private lateinit var infoBtn: MaterialCardView
    private lateinit var version: TextView
    private lateinit var layoutContent: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_other, container, false)
        reportBtn = v.findViewById(R.id.report)
        backupBtn = v.findViewById(R.id.backup)
        settingsBtn = v.findViewById(R.id.settings)
        infoBtn = v.findViewById(R.id.inform)
        layoutContent = activity?.findViewById(R.id.bottomNavView)!!
        layoutContent.visibility = View.VISIBLE

        reportBtn.setOnClickListener { val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes/issues/new"))
            startActivity(i) }

        settingsBtn.setOnClickListener {
            replaceFragment(SettingsFragment()) }

        backupBtn.setOnClickListener { replaceFragment(BackupFragment()) }
        infoBtn.setOnClickListener { replaceFragment(InfoFragment()) }

        version = v.findViewById(R.id.version)
        val ver = resources.getString(R.string.appversion)
        val build = resources.getString(R.string.buildn)
        version.text = "$ver ${BuildConfig.VERSION_NAME} \n$build ${BuildConfig.VERSION_CODE}"


        return v
    }


    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.addToBackStack( "tag" )
        layoutContent.visibility = View.GONE
        fragmentTransaction.commit ()
    }
 }