package ch.epfl.smsproxy.ui.fragment

import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat

abstract class RelayPreferenceFragment(
    private val sharedPreferencesName: String,
    @XmlRes
    private val preferencesRes: Int,
) : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = sharedPreferencesName
        setPreferencesFromResource(preferencesRes, rootKey)
    }
}
