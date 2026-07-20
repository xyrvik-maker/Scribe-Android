package com.primaloptima.scribe

import android.app.Application
import com.primaloptima.scribe.data.AppDatabase
import com.primaloptima.scribe.util.PrefsManager
import com.primaloptima.scribe.util.ThemeManager

class ScribeApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val prefs: PrefsManager by lazy { PrefsManager(this) }
    val themeManager: ThemeManager by lazy { ThemeManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ScribeApp
            private set
    }
}
