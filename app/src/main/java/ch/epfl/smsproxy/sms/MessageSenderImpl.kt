package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.RelayListFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class MessageSenderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MessageSender, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private val TAG = this::class.simpleName!!
    }

    private val relayListPreferences =
        context.getSharedPreferences(RelayListFragment.PREF_NAME, MODE_PRIVATE)

    private lateinit var relays: MutableMap<String, Relay>

    init {
        relayListPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override suspend fun broadcast(text: String): Unit = coroutineScope {
        if (!this@MessageSenderImpl::relays.isInitialized) {
            initializeRelays()
        }
        Log.d(TAG, "Broadcasting message to ${relays.keys}")
        val jobs = relays.values.map { async(Dispatchers.IO) { it.relay(text) } }
        jobs.awaitAll()
    }

    private fun initializeRelays() {
        relays = hashMapOf()
        relayListPreferences.all.keys.forEach { prefName ->
            RelayFactory.instantiateFromPreference(context, prefName)?.let { relay ->
                relays[prefName] = relay
            }
        }
        Log.i(TAG, "Initialized relays: ${relays.keys}")
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
                Log.i(TAG, "Added relay $key to existing relay list")
            } ?: Log.e(
                this::class.simpleName,
                "Incorrect config $key : ${context.getSharedPreferences(key, MODE_PRIVATE).all}."
            )
        } else {
            relays.remove(key)
            Log.i(TAG, "Removed relay $key from existing relay list")
        }
    }
}
