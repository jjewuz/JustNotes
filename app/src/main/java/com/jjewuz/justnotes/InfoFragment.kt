package com.jjewuz.justnotes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton


class InfoFragment : Fragment() {

    lateinit var ghBtn: Button
    lateinit var siteBtn: Button
    lateinit var licenseBtn: Button

    lateinit var  tgBtn: ImageButton
    lateinit var  vkBtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        ghBtn = v.findViewById(R.id.gh)
        siteBtn = v.findViewById(R.id.site)
        licenseBtn = v.findViewById(R.id.licenses)

        tgBtn = v.findViewById(R.id.tg)
        vkBtn = v.findViewById(R.id.vk)

        ghBtn.setOnClickListener { gh_a() }
        siteBtn.setOnClickListener{ site_a() }
        licenseBtn.setOnClickListener { replaceFragment(LicensesFragment()) }
        tgBtn.setOnClickListener { tg_a() }
        vkBtn.setOnClickListener { vk_a() }

        return v
    }

    fun vk_a(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/jjewuzhub"))
        startActivity(i)
    }

    fun tg_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jjewuzhub"))
        startActivity(i)
    }

    fun gh_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes"))
        startActivity(i)
    }

    fun site_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
        startActivity(i)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.addToBackStack( "tag" )
        fragmentTransaction.commit ()
    }

}