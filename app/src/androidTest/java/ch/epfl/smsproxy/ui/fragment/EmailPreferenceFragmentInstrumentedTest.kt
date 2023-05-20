package ch.epfl.smsproxy.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.smsproxy.R
import ch.epfl.toufi.android_test_utils.scenario.SafeFragmentScenario
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test

class EmailPreferenceFragmentInstrumentedTest {

    companion object {
        private const val PREF_NAME = "test_fragment_preferences"
    }

    private fun runTest(testFun: (SafeFragmentScenario<EmailPreferencesFragment>) -> Unit) {
        SafeFragmentScenario.launchInRegularContainer(instantiate = {
            EmailPreferencesFragment(PREF_NAME)
        }, testFunction = testFun)
    }

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    private fun getString(@StringRes res: Int): String = context.getString(res)

    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    @Test
    fun allPreferencesFieldsAreShown() = runTest {
        onView(withText(R.string.pref_email_smtp_host_title)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_email_smtp_port_title)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_email_smtp_user_title)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_email_smtp_password_title)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_email_smtp_start_tls_title)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_email_remote_address_title)).check(matches(isDisplayed()))
    }

    @Test
    fun sharedPreferencesAreShown() {
        val url = "smtp.gmail.com"
        val port = "5100"
        val user = "test@gmail.com"
        val pwd = "dummy_pwd"
        val tls = true
        val destAddress = "dest@yahoo.com"
        prefs.edit()
            .putString(getString(R.string.pref_email_smtp_host_key), url)
            .putString(getString(R.string.pref_email_smtp_port_key), port)
            .putString(getString(R.string.pref_email_smtp_user_key), user)
            .putString(getString(R.string.pref_email_smtp_password_key), pwd)
            .putBoolean(getString(R.string.pref_email_smtp_starttls_key), tls)
            .putString(getString(R.string.pref_email_remote_address_key), destAddress)
            .apply()

        runTest {
            onView(withText(R.string.pref_email_smtp_host_title))
                .check(matches(hasSibling(withText(url))))
            onView(withText(R.string.pref_email_smtp_port_title))
                .check(matches(hasSibling(withText(port))))
            onView(withText(R.string.pref_email_smtp_user_title))
                .check(matches(hasSibling(withText(user))))
            // password should be hidden:
            onView(withText(R.string.pref_email_smtp_password_title))
                .check(matches(not(hasSibling(withText(pwd)))))
            onView(withChild(withChild(withText(R.string.pref_email_smtp_start_tls_title)))).check(
                matches(
                    hasDescendant(
                        allOf(
                            instanceOf(SwitchCompat::class.java),
                            if (tls) isChecked() else isNotChecked()
                        )
                    )
                )
            )
            onView(withText(R.string.pref_email_remote_address_title))
                .check(matches(hasSibling(withText(destAddress))))
        }
    }
}
