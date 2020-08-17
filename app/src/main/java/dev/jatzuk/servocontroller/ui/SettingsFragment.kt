package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.util.Log
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME

private const val TAG = "SettingsFragment"

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        setPreferencesFromResource(R.xml.preferences, rootKey)

        preferenceScreen.findPreference<DropDownPreference>(
            getString(R.string.key_connection_type)
        )?.apply {
            summaryProvider = Preference.SummaryProvider<DropDownPreference> {
                Log.d(TAG, "summaryProvider: ${it.entry}")
                it?.entry.toString()
            }
            updateIcon()
            setOnPreferenceChangeListener { preference, newValue ->
                Log.d(TAG, "changeListener: $newValue")
                (preference as DropDownPreference).updateIcon(newValue.toString())
                true
            }
        }
    }

    private fun DropDownPreference.updateIcon(newValue: String? = null) {
        val array = resources.getStringArray(R.array.connection_types)
        val value = newValue ?: value
        val icon = if (value == array[0]) R.drawable.ic_bluetooth else R.drawable.ic_wifi
        setIcon(icon)
    }
}
