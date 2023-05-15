package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.RelayListFragment

class MessageSender(private val context: Context) : OnSharedPreferenceChangeListener {

    private val relayListPreferences =
        context.getSharedPreferences(RelayListFragment.PREF_NAME, Context.MODE_PRIVATE)

    private lateinit var relays: MutableMap<String, Relay>

    init {
        relayListPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun sendMessage(text: String) {
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
        requireNotNull(key)
        if (!this::relays.isInitialized) {
            initializeRelays()
            return
        }

        val added = sharedPreferences?.getString(key, null) != null
        if (added) {
            RelayFactory.instantiateFromPreference(context, key)?.let { relay ->
                relays[key] = relay
            } ?: Log.e(this::class.simpleName, "Added incorrect config to preferences")
        } else {
            relays.remove(key)
        }
    }
}
