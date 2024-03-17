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
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity


class InfoFragment : Fragment() {

    private lateinit var ghBtn: Button
    private lateinit var siteBtn: Button
    private lateinit var licenseBtn: Button

    private lateinit var tou: Button
    private lateinit var pp: Button

    private lateinit var  tgBtn: ImageButton
    private lateinit var  vkBtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        ghBtn = v.findViewById(R.id.gh)
        siteBtn = v.findViewById(R.id.site)
        licenseBtn = v.findViewById(R.id.licenses)

        tou = v.findViewById(R.id.terms_of_use)
        pp = v.findViewById(R.id.privacy_policy)
        tgBtn = v.findViewById(R.id.tg)
        vkBtn = v.findViewById(R.id.vk)

        ghBtn.setOnClickListener { openLink("https://github.com/jjewuz/JustNotes") }
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