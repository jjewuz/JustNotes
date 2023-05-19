package com.jjewuz.justnotes

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class InfoFragment : Fragment() {

    lateinit var ghBtn: Button
    lateinit var siteBtn: Button
    lateinit var rateBtn: Button

    lateinit var  tgBtn: ImageButton
    lateinit var  vkBtn: ImageButton
    lateinit var  dsBtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        ghBtn = v.findViewById(R.id.gh)
        siteBtn = v.findViewById(R.id.site)
        rateBtn = v.findViewById(R.id.rate)

        tgBtn = v.findViewById(R.id.tg)
        vkBtn = v.findViewById(R.id.vk)
        dsBtn = v.findViewById(R.id.ds)

        ghBtn.setOnClickListener { gh_a() }
        siteBtn.setOnClickListener{ site_a() }
        rateBtn.setOnClickListener { ratestore() }
        tgBtn.setOnClickListener { tg_a() }
        vkBtn.setOnClickListener { vk_a() }
        dsBtn.setOnClickListener { ds_a() }

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

    fun ds_a(){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jNHNBdYuAR"))
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

    fun ratestore() {

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jjewuz.justnotes"))
        startActivity(i)
    }

}