package ch.epfl.smsproxy.ui.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.smsproxy.R
import ch.epfl.toufi.android_test_utils.scenario.SafeFragmentScenario
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test

class SlackPreferenceFragmentInstrumentedTest {

    companion object {
        private const val PREF_NAME = "test_fragment_preferences"
    }

    private fun runTest(testFun: (SafeFragmentScenario<SlackPreferenceFragment>) -> Unit) {
        SafeFragmentScenario.launchInRegularContainer(instantiate = {
            SlackPreferenceFragment(PREF_NAME)
        }, testFunction = testFun)
    }

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    private fun getString(@StringRes res: Int): String = context.getString(res)

    @Before
    fun init() {
        context = getApplicationContext()
        prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    @Test
    fun allPreferencesFieldsAreShown() = runTest {
        onView(withText(R.string.pref_slack_webhook_title)).check(matches(isDisplayed()))
    }

    @Test
    fun sharedPreferencesAreShown() {
        val webhook = "http://mock-webhook.com"
        prefs.edit()
            .putString(getString(R.string.pref_slack_webhook_key), webhook)
            .apply()

        runTest {
            // url should not be displayed
            onView(withChild(withText(R.string.pref_slack_webhook_title)))
                .check(matches(not(hasDescendant(withText(webhook)))))
        }
    }
}
