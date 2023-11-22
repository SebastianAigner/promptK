package io.sebi.promptk

import android.app.Application
import android.content.Context

class PromptApplication : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}
