package ch.epfl.smsproxy.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.preference.PreferenceFragmentCompat
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.fragment.EmailPreferencesFragment
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity

class PreferenceActivityImpl : PreferencesActivity() {

    companion object {
        const val EXTRA_PREFERENCES_NAME = "extra_shared_preference_name"
    }

    private lateinit var sharedPreferencesName: String

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME) ?: "default"
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME) ?: "default"
        super.onCreate(savedInstanceState)
    }

    override fun loadFragment(preferenceFragmentId: String): PreferenceFragmentCompat? =
        when (preferenceFragmentId) {
            getString(R.string.preference_notification_email) ->
                EmailPreferencesFragment(sharedPreferencesName)

            else -> null
        }

}
