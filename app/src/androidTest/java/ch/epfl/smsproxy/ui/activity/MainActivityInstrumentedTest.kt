package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
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
import ch.epfl.toufi.android_utils.permissions.MockPermissionsActivity
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_PREFERENCES_ID
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_TITLE
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
        MockPermissionsActivity.configuredSelfPermissions.clear()
        MockPermissionsActivity.configuredShouldShowRationale.clear()

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
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // depending on espresso version,
            // may need to catch the initial intent used to start the activity
            runCatching { intended(hasComponent(MainActivity::class.qualifiedName)) }
            testFun(scenario)
        }
    }

    @Test
    fun displayFragmentFABAndMenuItem() = runTest {
        onView(withId(R.id.preferences_container)).check(matches(isDisplayed()))
        onView(withId(R.id.relay_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.preferences_add_button)).check(matches(isDisplayed()))
        onMenuItem(withText(R.string.check_sms_permissions)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingOnMenuItemWithPermissionsGrantedShowsToast() {
        MockPermissionsActivity.configuredSelfPermissions[READ_SMS] = PERMISSION_GRANTED
        MockPermissionsActivity.configuredSelfPermissions[RECEIVE_SMS] = PERMISSION_GRANTED
        MockPermissionsActivity.configuredSelfPermissions[RECEIVE_MMS] = PERMISSION_GRANTED

        runTest {
            onMenuItem(withText(R.string.check_sms_permissions)).perform(click())
            //TODO assert Toast is called

        }
    }

    @Test
    fun clickingOnMenuItemWithPermissionsNotGrantedRequestsThem() {
        MockPermissionsActivity.configuredSelfPermissions[READ_SMS] = PERMISSION_DENIED
        MockPermissionsActivity.configuredSelfPermissions[RECEIVE_SMS] = PERMISSION_GRANTED
        MockPermissionsActivity.configuredSelfPermissions[RECEIVE_MMS] = PERMISSION_GRANTED

        runTest {
            onMenuItem(withText(R.string.check_sms_permissions)).perform(click())
            //TODO assert request permissions is called

        }
    }

    @Test
    fun clickingOnFABDisplaysConfigChoiceDialog() = runTest {
        onView(withId(R.id.preferences_add_button)).perform(click())

        context.resources.getStringArray(R.array.pref_type_display_names).forEach { str ->
            onView(withText(str)).inRoot(isDialog()).check(matches(isDisplayed()))
        }

        onView(withText(R.string.pref_type_email_display_name)).perform(click())

        intended(
            allOf(
                hasComponent(RelayPreferencesActivity::class.java.name),
                hasExtra(EXTRA_TITLE, startsWith(emailType)),
                hasExtra(EXTRA_PREFERENCES_ID, emailType),
                hasExtra(EXTRA_PREFERENCES_NAME, startsWith(emailType)),
            )
        )

        assertNoUnverifiedIntentIgnoringBootstrap()
    }

    @Test
    fun clickingOnItemLaunchesRelayPreferenceActivity() {
        val pref1 = "test_pref_name_1"
        val pref2 = "test_pref_name_2"
        relayListPrefs.edit().putString(pref1, emailType).putString(pref2, slackType).apply()

        runTest {
            onView(withText(pref2)).perform(click())

            intended(
                allOf(
                    hasComponent(RelayPreferencesActivity::class.java.name),
                    hasExtra(EXTRA_TITLE, pref2),
                    hasExtra(EXTRA_PREFERENCES_ID, slackType),
                    hasExtra(EXTRA_PREFERENCES_NAME, pref2),
                )
            )

            assertNoUnverifiedIntentIgnoringBootstrap()
        }
    }
}
