package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
import dev.jatzuk.servocontroller.other.ServoTexture
import dev.jatzuk.servocontroller.utils.PreferenceDropDownSummaryProvider
import dev.jatzuk.servocontroller.utils.SettingsHolder
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settingsHolder: SettingsHolder

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        setPreferencesFromResource(R.xml.preferences, rootKey)

        preferenceScreen.findPreference<DropDownPreference>(
            getString(R.string.key_connection_type)
        )?.apply {
            summaryProvider = PreferenceDropDownSummaryProvider()
            updateIcon()
            setOnPreferenceChangeListener { _, newValue ->
                val index = entries.indexOf(newValue)
                val connectionType = ConnectionType.values()[index]
                settingsHolder.applyChanges(
                    connectionType = connectionType
                )
                updateIcon(newValue.toString())
                true
            }
        }

        preferenceScreen.findPreference<SeekBarPreference>(
            getString(R.string.key_servos_count)
        )?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                settingsHolder.applyChanges(
                    servosCount = newValue as Int
                )
                true
            }
        }

        preferenceScreen.findPreference<SwitchPreferenceCompat>(
            getString(R.string.key_should_display_sent_data)
        )?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                settingsHolder.applyChanges(
                    shouldDisplaySentData = newValue as Boolean
                )
                true
            }
        }

        preferenceScreen.findPreference<DropDownPreference>(
            getString(R.string.key_servos_textures)
        )?.apply {
            summaryProvider = PreferenceDropDownSummaryProvider()
            val angleGridPreference = findPreference<SwitchPreferenceCompat>(
                getString(R.string.key_is_angle_grid_should_show)
            )!!
            val index =
                if (value.isNullOrEmpty()) 0 // in case value is not loaded properly(cold start)
                else entryValues.indexOf(value)
            var servoTexture = ServoTexture.values()[index]
            angleGridPreference.isEnabled = servoTexture == ServoTexture.TEXTURE
            setOnPreferenceChangeListener { _, newValue ->
                servoTexture = ServoTexture.values()[entryValues.indexOf(newValue)]
                angleGridPreference.isEnabled = servoTexture == ServoTexture.TEXTURE
                settingsHolder.applyChanges(
                    servosTexture = servoTexture
                )
                true
            }
        }

        preferenceScreen.findPreference<SwitchPreferenceCompat>(
            requireContext().getString(R.string.key_should_keep_screen_on)
        )?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                settingsHolder.applyChanges(
                    shouldKeepScreenOn = newValue as Boolean
                )
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
