package com.jjewuz.justnotes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class LicensesFragment : Fragment() {

    private lateinit var dbBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_licenses, container, false)

        dbBtn = v.findViewById(R.id.db_github)

        dbBtn.setOnClickListener { val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rafi0101/Android-Room-Database-Backup/tree/master"))
            startActivity(i) }

        return v
    }
}