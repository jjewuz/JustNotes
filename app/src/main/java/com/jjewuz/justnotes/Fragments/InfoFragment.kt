package com.jjewuz.justnotes.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jjewuz.justnotes.BuildConfig
import com.jjewuz.justnotes.R


class InfoFragment : Fragment() {

    private lateinit var ghBtn: LinearLayout
    private lateinit var sourceBtn: LinearLayout
    private lateinit var siteBtn: LinearLayout
    private lateinit var licenseBtn: Button

    private lateinit var tou: Button
    private lateinit var pp: Button

    private lateinit var  tgBtn: ImageButton
    private lateinit var  vkBtn: ImageButton

    private lateinit var versionTxt: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.activity_info, container, false)
        ghBtn = v.findViewById(R.id.github_profile)
        sourceBtn = v.findViewById(R.id.github_code)
        siteBtn = v.findViewById(R.id.site)
        licenseBtn = v.findViewById(R.id.licenses)

        tou = v.findViewById(R.id.terms_of_use)
        pp = v.findViewById(R.id.privacy_policy)
        tgBtn = v.findViewById(R.id.tg)
        vkBtn = v.findViewById(R.id.vk)

        versionTxt = v.findViewById(R.id.version)

        versionTxt.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        ghBtn.setOnClickListener { openLink("https://github.com/jjewuz/JustNotes") }
        sourceBtn.setOnClickListener { openLink("https://github.com/jjewuz") }
        siteBtn.setOnClickListener{ openLink("https://jjewuz.ru/justnotes/justnotes.html") }
        licenseBtn.setOnClickListener { startActivity(Intent(requireActivity(), OssLicensesMenuActivity::class.java)) }
        tou.setOnClickListener { openLink("https://jjewuz.ru/justnotes/termsofuse.html") }
        pp.setOnClickListener { openLink("https://jjewuz.ru/justnotes/privacypolicy.html") }
        tgBtn.setOnClickListener { openLink("https://t.me/jjewuz_support") }
        vkBtn.setOnClickListener { openLink("https://vk.com/jjewuzhub") }

        return v
    }

    private fun openLink(url: String){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(i)
    }

}