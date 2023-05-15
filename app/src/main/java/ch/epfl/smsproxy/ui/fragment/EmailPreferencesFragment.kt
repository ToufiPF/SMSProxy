package ch.epfl.smsproxy.ui.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.EditTextPreference
import androidx.preference.get
import ch.epfl.smsproxy.R

class EmailPreferencesFragment(sharedPreferenceName: String) :
    RelayPreferenceFragment(sharedPreferenceName, R.xml.preferences_email),
    OnSharedPreferenceChangeListener {

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    private fun getDefaultPort(startTls: Boolean): String =
        if (startTls) resources.getString(R.string.smtp_start_tls_port)
        else resources.getString(R.string.smtp_ssl_port)

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        requireNotNull(prefs)

        val smtpPortKey = getString(R.string.pref_key_email_smtp_port)
        when (key) {
            getString(R.string.pref_key_email_smtp_starttls) -> {
                val startTls = prefs.getBoolean(key, false)
                val currentPort = prefs.getString(smtpPortKey, null)
                if (currentPort == getDefaultPort(!startTls))
                    preferenceScreen.get<EditTextPreference>(smtpPortKey)?.text =
                        getDefaultPort(startTls)
            }
        }
    }
}
