package io.github.bbang208.spirit

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import io.github.bbang208.spirit.util.CustomDebugTree
import timber.log.Timber

@HiltAndroidApp
class SpiritApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(AndroidLogAdapter())
            Timber.plant(CustomDebugTree())
        }
    }
}
