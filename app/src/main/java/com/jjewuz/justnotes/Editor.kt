package com.jjewuz.justnotes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


class Editor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }


    fun vk_a(view: View){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/jjewuzhub"))
        startActivity(i)
    }

    fun tg_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jjewuzhub"))
                startActivity(i)
    }

    fun gh_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jjewuz/JustNotes"))
                startActivity(i)
    }

    fun site_a(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://jjewuz.github.io/"))
                startActivity(i)
    }

    fun ratestore(view: View){

        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jjewuz.justnotes"))
        startActivity(i)
    }

    override fun onBackPressed(){
        finish()
    }


}

