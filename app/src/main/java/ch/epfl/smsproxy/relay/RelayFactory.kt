package ch.epfl.smsproxy.relay

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ch.epfl.smsproxy.R

object RelayFactory {

    const val PREF_TYPE_KEY = "relay_type"


    fun instantiateFromPreference(context: Context, configPreferencesName: String): Relay? =
        instantiateFromPreference(
            context,
            context.getSharedPreferences(configPreferencesName, Context.MODE_PRIVATE),
        )

    /**
     * Instantiates a [Relay] from the config saved in the given preferences.
     * @param config [SharedPreferences] holding the configuration of the relay
     * @return [Relay]
     */
    fun instantiateFromPreference(context: Context, config: SharedPreferences): Relay? =
        when (val type = config.getString(PREF_TYPE_KEY, null)) {
            context.getString(R.string.pref_type_email) -> {
                val host = config.getString(
                    context.getString(R.string.pref_email_smtp_host_key), null
                )
                val port = config.getString(
                    context.getString(R.string.pref_email_smtp_port_key), null
                )?.toIntOrNull()
                val user = config.getString(
                    context.getString(R.string.pref_email_smtp_user_key), null
                )
                val pwd = config.getString(
                    context.getString(R.string.pref_email_smtp_password_key), null
                )
                val startTls = config.getBoolean(
                    context.getString(R.string.pref_email_smtp_starttls_key), false
                )
                val dest = config.getString(
                    context.getString(R.string.pref_email_remote_address_key), null
                )

                if (host != null && port != null && user != null && dest != null) {
                    val service = EmailService(host, port, user, pwd, startTls)
                    EmailRelay(service, dest)
                } else {
                    Log.e(this::class.simpleName,
                        "Invalid configuration for type $type: host=$host, port=$port, " + "user=$user, pwd=${pwd?.let { "is_present" } ?: "is_absent"}, dest=$dest")
                    null
                }
            }

            else -> {
                Log.e(this::class.simpleName, "Unrecognized config type in preferences: $type")
                null
            }
        }
}
