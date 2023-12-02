package com.jjewuz.justnotes

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


object Utils {
    fun fromHtml(text: String?): SpannableString {
        var spannableString = SpannableString.valueOf(Html.fromHtml(text))
        if (spannableString.toString().endsWith("\n\n")) spannableString =
            spannableString.subSequence(0, spannableString.length - 2) as SpannableString
        return spannableString
    }

    fun textFormatting(param: String, noteEdt: EditText){
        val selectedTextStart = noteEdt.selectionStart
        val selectedTextEnd = noteEdt.selectionEnd

        if (selectedTextStart != -1 && selectedTextEnd != -1) {
            val spannable = SpannableString(noteEdt.text)
            if (param == "bold"){
                spannable.setSpan(StyleSpan(Typeface.BOLD), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "italic"){
                spannable.setSpan(StyleSpan(Typeface.ITALIC), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "under"){
                spannable.setSpan(UnderlineSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "strike"){
                spannable.setSpan(StrikethroughSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "null"){
                val spans = spannable.getSpans(
                    selectedTextStart, selectedTextEnd,
                    CharacterStyle::class.java
                )
                for (selectSpan in spans) spannable.removeSpan(selectSpan)
            }
            noteEdt.setText(spannable)
        }
    }

    fun colorFormatting(param: String, noteEdt: EditText){
        val selectedTextStart = noteEdt.selectionStart
        val selectedTextEnd = noteEdt.selectionEnd

        if (selectedTextStart != -1 && selectedTextEnd != -1) {
            val spannable = SpannableString(noteEdt.text)
            if (param == "red") {
                spannable.setSpan(
                    ForegroundColorSpan(Color.rgb(193, 22, 22)),
                    selectedTextStart,
                    selectedTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }else if(param == "yellow"){
                spannable.setSpan(
                    ForegroundColorSpan(Color.rgb(227, 174, 18)),
                    selectedTextStart,
                    selectedTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }else if(param == "blue"){
                spannable.setSpan(
                    ForegroundColorSpan(Color.rgb(15, 68, 202)),
                    selectedTextStart,
                    selectedTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }else if(param == "green"){
                spannable.setSpan(
                    ForegroundColorSpan(Color.rgb(113, 219, 165)),
                    selectedTextStart,
                    selectedTextEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            noteEdt.setText(spannable)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}