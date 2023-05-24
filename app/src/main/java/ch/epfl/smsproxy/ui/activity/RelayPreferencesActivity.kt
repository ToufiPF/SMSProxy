package ch.epfl.smsproxy.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.OnBackPressedCallback
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.EmailPreferencesFragment
import ch.epfl.smsproxy.ui.fragment.RelayPreferenceFragment
import ch.epfl.smsproxy.ui.fragment.SlackPreferenceFragment
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity

class RelayPreferencesActivity : PreferencesActivity() {

    companion object {
        const val EXTRA_PREFERENCES_NAME = "extra_shared_preference_name"

        fun makeIntent(
            appContext: Context,
            title: String,
            preferenceId: String,
            preferenceName: String
        ) = Intent(appContext, RelayPreferencesActivity::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_PREFERENCES_ID, preferenceId)
            putExtra(EXTRA_PREFERENCES_NAME, preferenceName)
        }
    }

    private lateinit var sharedPreferencesName: String
    private var validFragmentId: String? = null

    init {
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME)!!
        super.onCreate(savedInstanceState, persistentState)
        initBackPressedCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferencesName = intent.getStringExtra(EXTRA_PREFERENCES_NAME)!!
        super.onCreate(savedInstanceState)
        initBackPressedCallback()
    }

    private fun initBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (validFragmentId != null) {
                    val intent = Intent().apply {
                        putExtra(EXTRA_PREFERENCES_NAME, sharedPreferencesName)
                        putExtra(EXTRA_PREFERENCES_ID, validFragmentId)
                    }
                    setResult(RESULT_OK, intent)
                } else {
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        })
    }

    override fun loadFragment(fragmentId: String): RelayPreferenceFragment? {
        val fragment: RelayPreferenceFragment? = when (fragmentId) {
            getString(R.string.pref_type_email) -> EmailPreferencesFragment(sharedPreferencesName)
            getString(R.string.pref_type_slack) -> SlackPreferenceFragment(sharedPreferencesName)
            else -> null
        }

        validFragmentId = null
        if (fragment != null) {
            validFragmentId = fragmentId
            getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
                .edit().putString(RelayFactory.PREF_TYPE_KEY, fragmentId).apply()
        }

        return fragment
    }
}
