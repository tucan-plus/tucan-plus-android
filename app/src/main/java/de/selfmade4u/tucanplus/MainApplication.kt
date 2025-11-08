package de.selfmade4u.tucanplus

import android.app.Application
import androidx.work.Configuration

class MainApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
