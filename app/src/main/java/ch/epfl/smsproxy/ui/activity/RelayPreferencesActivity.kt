package ch.epfl.smsproxy.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.EmailPreferencesFragment
import ch.epfl.smsproxy.ui.fragment.RelayPreferenceFragment
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity

class RelayPreferencesActivity : PreferencesActivity() {

    companion object {
        const val EXTRA_PREFERENCES_NAME = "extra_shared_preference_name"

        fun launchIntent(
            context: Context,
            title: String,
            preferenceId: String,
            preferenceName: String
        ) {
            val intent = Intent(context, RelayPreferencesActivity::class.java)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_PREFERENCES_ID, preferenceId)
            intent.putExtra(EXTRA_PREFERENCES_NAME, preferenceName)
            context.startActivity(intent)
        }
    }

    private lateinit var sharedPreferencesName: String

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME)!!
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME)!!
        super.onCreate(savedInstanceState)
    }

    override fun loadFragment(fragmentId: String): RelayPreferenceFragment? {
        val fragment: RelayPreferenceFragment? = when (fragmentId) {
            getString(R.string.preference_notification_email) -> EmailPreferencesFragment(
                sharedPreferencesName
            )

            else -> null
        }

        if (fragment != null) {
            getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
                .edit().putString(RelayFactory.PREF_TYPE_KEY, fragmentId).apply()
        }

        return fragment
    }

}
