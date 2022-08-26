package com.jjewuz.justnotes

import android.app.Application
import com.google.android.material.color.DynamicColors

class JustNotesApp : Application() {
    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}