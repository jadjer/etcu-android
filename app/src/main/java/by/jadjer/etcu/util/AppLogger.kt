package by.jadjer.etcu.util

import android.util.Log
import by.jadjer.etcu.BuildConfig

/**
 * Optimized logger that uses inline functions to avoid string allocation when logging is disabled.
 */
object AppLogger {
    const val DEFAULT_TAG = "ETCU"

    @JvmStatic
    inline fun d(tag: String = DEFAULT_TAG, message: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message())
        }
    }

    @JvmStatic
    inline fun i(tag: String = DEFAULT_TAG, message: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message())
        }
    }

    @JvmStatic
    inline fun e(tag: String = DEFAULT_TAG, message: () -> String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message(), throwable)
        }
    }
}
