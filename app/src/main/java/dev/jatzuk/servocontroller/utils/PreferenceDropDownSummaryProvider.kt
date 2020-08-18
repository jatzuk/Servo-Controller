package dev.jatzuk.servocontroller.utils

import androidx.preference.DropDownPreference
import androidx.preference.Preference

class PreferenceDropDownSummaryProvider : Preference.SummaryProvider<DropDownPreference> {

    override fun provideSummary(preference: DropDownPreference?): CharSequence {
        return preference?.entry.toString()
    }
}
