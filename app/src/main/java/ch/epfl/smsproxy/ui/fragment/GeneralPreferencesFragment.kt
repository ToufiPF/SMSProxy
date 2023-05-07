package ch.epfl.smsproxy.ui.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.get
import ch.epfl.smsproxy.R

class GeneralPreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    companion object {
        const val NAME = "GeneralPreferences"
    }

    private val allNotificationMeans: Array<String> by lazy {
        requireContext().resources.getStringArray(R.array.preference_notification_means)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = NAME
        setPreferencesFromResource(R.xml.preferences_general, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        when (key) {
            requireContext().getString(R.string.preference_notification_means_key) -> {
                val enabled = prefs!!.getStringSet(key, setOf())!!

                allNotificationMeans.forEach { group ->
                    preferenceScreen.get<PreferenceGroup>(group)?.isEnabled =
                        enabled.contains(group)
                }
            }
        }
    }
}