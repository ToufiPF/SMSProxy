package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.RelayListFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageSenderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MessageSender, SharedPreferences.OnSharedPreferenceChangeListener {

    private val relayListPreferences =
        context.getSharedPreferences(RelayListFragment.PREF_NAME, Context.MODE_PRIVATE)

    private lateinit var relays: MutableMap<String, Relay>

    init {
        relayListPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun broadcast(text: String) {
        if (!this::relays.isInitialized) {
            initializeRelays()
        }

        relays.values.forEach { it.relay(text) }
    }

    private fun initializeRelays() {
        relays = hashMapOf()
        relayListPreferences.all.keys.forEach { prefName ->
            RelayFactory.instantiateFromPreference(context, prefName)?.let { relay ->
                relays[prefName] = relay
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // can ignore changes until broadcast is called once
        if (sharedPreferences == null || key == null || !this::relays.isInitialized) {
            return
        }

        val added = sharedPreferences.getString(key, null) != null
        if (added) {
            RelayFactory.instantiateFromPreference(context, key)?.let { relay ->
                relays[key] = relay
            } ?: Log.e(this::class.simpleName, "Added incorrect config to preferences")
        } else {
            relays.remove(key)
        }
    }
}