package ch.epfl.smsproxy.relay

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import ch.epfl.smsproxy.R
import java.net.URL

@Suppress("MemberVisibilityCanBePrivate")
object RelayFactory {

    const val PREF_TYPE_KEY = "relay_type"


    /**
     * Instantiates a [Relay] from the config saved in the given preferences.
     * @param configPreferencesName [String] name of the [SharedPreferences]
     * holding the configuration of the relay
     * @return [Relay]
     */
    fun instantiateFromPreference(context: Context, configPreferencesName: String): Relay? =
        instantiateFromPreference(
            context,
            context.getSharedPreferences(configPreferencesName, MODE_PRIVATE),
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

            context.getString(R.string.pref_type_slack) -> {
                val webhook = config.getString(
                    context.getString(R.string.pref_slack_webhook_key), null
                )
                if (webhook != null) {
                    SlackRelay(URL(webhook))
                } else {
                    Log.e(
                        this::class.simpleName,
                        "Invalid configuration for type $type: webhook is null"
                    )
                    null
                }
            }

            else -> {
                Log.e(this::class.simpleName, "Unrecognized config type in preferences: $type")
                null
            }
        }
}
