package com.jjewuz.justnotes

import android.text.Html
import android.text.SpannableString


object Utils {
    fun fromHtml(text: String?): SpannableString {
        var spannableString = SpannableString.valueOf(Html.fromHtml(text))
        if (spannableString.toString().endsWith("\n\n")) spannableString =
            spannableString.subSequence(0, spannableString.length - 2) as SpannableString
        return spannableString
    }
}