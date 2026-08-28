package by.jadjer.etcu.data.local

import android.content.Context
import androidx.core.content.edit
import by.jadjer.etcu.data.ble.BLEConstants

class BLEPreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences(BLEConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastMac(mac: String) {
        prefs.edit { putString(BLEConstants.KEY_LAST_MAC, mac) }
    }

    fun getLastMac(): String? {
        return prefs.getString(BLEConstants.KEY_LAST_MAC, null)
    }

    fun clearLastMac() {
        prefs.edit { remove(BLEConstants.KEY_LAST_MAC) }
    }
}
