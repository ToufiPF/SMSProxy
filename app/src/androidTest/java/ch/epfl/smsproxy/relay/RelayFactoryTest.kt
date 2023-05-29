package ch.epfl.smsproxy.relay

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.relay.RelayFactory.PREF_TYPE_KEY
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RelayFactoryTest {

    companion object {
        private const val NAME = "test_pref_name"
    }

    private lateinit var context: Context
    private lateinit var configPref: SharedPreferences

    private fun getString(@StringRes res: Int): String = context.getString(res)

    @Before
    fun init() {
        context = getApplicationContext()
        context.deleteSharedPreferences(NAME)
        configPref = context.getSharedPreferences(NAME, MODE_PRIVATE)
    }

    @Test
    fun relayFactoryReturnsNullOnUnknownTypes() {
        configPref.edit().putString(PREF_TYPE_KEY, null).apply()
        assertNull(RelayFactory.instantiateFromPreference(context, NAME))

        configPref.edit().putString(PREF_TYPE_KEY, "unknown_type").apply()
        assertNull(RelayFactory.instantiateFromPreference(context, NAME))

        configPref.edit().putString(PREF_TYPE_KEY, getString(R.string.pref_type_email)).apply()
        assertNull(RelayFactory.instantiateFromPreference(context, NAME))
    }

    @Test
    fun relayFactoryInstantiateEmailRelay() {
        configPref.edit().putString(PREF_TYPE_KEY, getString(R.string.pref_type_email)).apply()

        val relay = RelayFactory.instantiateFromPreference(context, NAME)
        assertTrue(relay is EmailRelay)
    }

    @Test
    fun relayFactoryInstantiateSlackRelay() {
        configPref.edit().putString(PREF_TYPE_KEY, getString(R.string.pref_type_slack)).apply()

        val relay = RelayFactory.instantiateFromPreference(context, NAME)
        assertTrue(relay is SlackRelay)
    }
}
