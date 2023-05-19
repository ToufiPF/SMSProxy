package ch.epfl.smsproxy.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.activity.RelayPreferencesActivity.Companion.EXTRA_PREFERENCES_NAME
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_PREFERENCES_ID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RelayPreferenceActivityInstrumentedTest {

    companion object {
        const val PREF_NAME = "test_fragment_pref"
    }

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    private lateinit var emailType: String
    private lateinit var slackType: String

    @Before
    fun init() {
        context = getApplicationContext()
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        emailType = context.getString(R.string.pref_type_email)
        slackType = context.getString(R.string.pref_type_slack)
    }

    @Test
    fun emailFragmentIsShownWhenPassedViaExtra() {
        val intent = Intent(context, RelayPreferencesActivity::class.java)
        intent.putExtra(EXTRA_PREFERENCES_NAME, PREF_NAME)
        intent.putExtra(EXTRA_PREFERENCES_ID, emailType)

        ActivityScenario.launch<RelayPreferencesActivity>(intent).use {
            onView(withText(R.string.pref_email_smtp_host_title)).check(matches(isDisplayed()))
        }

        val type = prefs.getString(RelayFactory.PREF_TYPE_KEY, null)
        assertEquals(emailType, type)
    }


    @Test
    fun slackFragmentIsShownWhenPassedViaExtra() {
        val intent = Intent(context, RelayPreferencesActivity::class.java)
        intent.putExtra(EXTRA_PREFERENCES_NAME, PREF_NAME)
        intent.putExtra(EXTRA_PREFERENCES_ID, slackType)

        ActivityScenario.launch<RelayPreferencesActivity>(intent).use {
            onView(withText(R.string.pref_slack_webhook_title)).check(matches(isDisplayed()))
        }

        val type = prefs.getString(RelayFactory.PREF_TYPE_KEY, null)
        assertEquals(slackType, type)
    }
}
