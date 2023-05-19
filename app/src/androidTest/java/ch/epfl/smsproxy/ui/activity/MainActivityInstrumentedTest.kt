package ch.epfl.smsproxy.ui.activity

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.activity.RelayPreferencesActivity.Companion.EXTRA_PREFERENCES_NAME
import ch.epfl.smsproxy.ui.fragment.RelayListFragment
import ch.epfl.toufi.android_test_utils.espresso.IntentAsserts.assertNoUnverifiedIntentIgnoringBootstrap
import ch.epfl.toufi.android_test_utils.espresso.ViewInteractions.onMenuItem
import ch.epfl.toufi.android_utils.ui.UIExtensions
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_PREFERENCES_ID
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_TITLE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Before
import org.junit.Test

class MainActivityInstrumentedTest {

    private lateinit var context: Context
    private lateinit var relayListPrefs: SharedPreferences

    private lateinit var emailType: String
    private lateinit var slackType: String

    @Before
    fun init() {
        Intents.init()

        context = getApplicationContext()
        relayListPrefs = context.getSharedPreferences(RelayListFragment.PREF_NAME, MODE_PRIVATE)
        relayListPrefs.edit().clear().apply()

        emailType = context.getString(R.string.pref_type_email)
        slackType = context.getString(R.string.pref_type_slack)
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    // order = 0 => execute first; note: for hilt order must be == 0

//    @get:Rule(order = 0)
//    val permissionRule = GrantPermissionRule.grant(READ_SMS, RECEIVE_SMS, RECEIVE_MMS, INTERNET)!!

    private fun runTest(testFun: (ActivityScenario<MainActivity>) -> Unit) {
        ActivityScenario.launch(MainActivity::class.java).use(testFun)
    }

    @Test
    fun displayFragmentFABAndMenuItem() = runTest {
        onView(withId(R.id.preferences_container)).check(matches(isDisplayed()))
        onView(withId(R.id.relay_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.preferences_add_button)).check(matches(isDisplayed()))
        onMenuItem(withText(R.string.check_sms_permissions)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingOnMenuItemTriggersPermissionsCheck() = runTest { scenario ->
        with(mockk<UIExtensions>()) {
            scenario.onActivity { activity ->
                every { activity.checkHasPermissions(*anyVararg()) } answers { call ->
                    Array(call.invocation.args.size) { false }.toBooleanArray()
                }
            }

            onMenuItem(withText(R.string.check_sms_permissions)).perform(click())
            scenario.onActivity { activity ->
                verify {
                    activity.checkHasPermissions(*anyVararg())
                }
            }
        }
    }

    @Test
    fun clickingOnFABDisplaysConfigChoiceDialog() = runTest {
        onView(withId(R.id.preferences_add_button)).perform(click())

        context.resources.getStringArray(R.array.pref_type_display_names).forEach { str ->
            onView(withText(str)).inRoot(isDialog()).check(matches(isDisplayed()))
        }

        onView(withText(R.string.pref_type_email_display_name)).perform(click())

        val prefTypeEmail = context.getString(R.string.pref_type_email)
        intended(
            allOf(
                hasComponent(RelayPreferencesActivity::class.java.name),
                hasExtra(EXTRA_TITLE, startsWith(prefTypeEmail)),
                hasExtra(EXTRA_PREFERENCES_ID, prefTypeEmail),
                hasExtra(EXTRA_PREFERENCES_NAME, startsWith(prefTypeEmail)),
            )
        )

        assertNoUnverifiedIntentIgnoringBootstrap()
    }

    @Test
    fun clickingOnItemLaunchesRelayPreferenceActivity() {

        relayListPrefs.edit()
            .putString("test_pref_name_1", emailType)
            .putString("test_pref_name_2", slackType)
            .apply()

        runTest {


        }
    }
}
