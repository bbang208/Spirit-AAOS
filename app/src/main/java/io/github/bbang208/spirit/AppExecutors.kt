package io.github.bbang208.spirit

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppExecutors @Inject constructor() {

    val diskIO: Executor = Executors.newSingleThreadExecutor()
    val networkIO: Executor = Executors.newFixedThreadPool(3)
    val mainThread: Executor = MainThreadExecutor()

    private class MainThreadExecutor : Executor {
        private val mainHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainHandler.post(command)
        }
    }
}
