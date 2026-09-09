package com.sih26168.app

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import java.io.File

class SihApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().osmdroidTileCache = File(this.cacheDir, "osmdroid")
        Configuration.getInstance().osmdroidTileCache.mkdirs()
    }
}
