package by.jadjer.etcu.data.source.local

import android.content.Context
import androidx.core.content.edit
import by.jadjer.etcu.data.source.ble.BleConstants

class BlePreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences(BleConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastMac(mac: String) {
        prefs.edit { putString(BleConstants.KEY_LAST_MAC, mac) }
    }

    fun getLastMac(): String? {
        return prefs.getString(BleConstants.KEY_LAST_MAC, null)
    }

    fun clearLastMac() {
        prefs.edit { remove(BleConstants.KEY_LAST_MAC) }
    }
}
