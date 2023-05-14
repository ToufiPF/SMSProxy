package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.fragment.RelayListPreferenceFragment
import java.net.URLEncoder

@Suppress("PrivatePropertyName")
class MessageSender(private val context: Context) {

    private val NOTIF_MEANS_KEY = context.getString(R.string.preference_notification_means_key)
    private val NOTIF_EMAIL = context.getString(R.string.preference_notification_email)
    private val NOTIF_WHATSAPP = context.getString(R.string.preference_notification_whatsapp)
    private val EMAIL_DEST_KEY = context.getString(R.string.preference_mail_destination_address_key)
    private val WHATSAPP_DEST_KEY =
        context.getString(R.string.preference_whatsapp_destination_number_key)

    private val preferences =
        context.getSharedPreferences(RelayListPreferenceFragment.PREF_NAME, Context.MODE_PRIVATE)

    private val emailDestinations: List<String>
        get() {
            val emails = ArrayList<String>()
            if (preferences.getStringSet(NOTIF_MEANS_KEY, setOf())!!.contains(NOTIF_EMAIL)) {
                preferences.getString(EMAIL_DEST_KEY, null)?.let {
                    emails.add(it)
                }
            }
            return emails
        }

    private val whatsAppNumbers: List<String>
        get() {
            val numbers = ArrayList<String>()
            if (preferences.getStringSet(NOTIF_MEANS_KEY, setOf())!!.contains(NOTIF_WHATSAPP)) {
                preferences.getString(WHATSAPP_DEST_KEY, null)?.let {
                    numbers.add(it)
                }
            }
            return numbers
        }

    private fun sendByEmail(address: String, text: String) {

    }

    private fun sendByWhatsApp(number: String, text: String) {
        val num = number.removePrefix("+")
        val encodedText = URLEncoder.encode(text, "utf-8")

        val url = "https://api.whatsapp.com/send?phone=$num&text=$encodedText"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            `package` = "com.whatsapp"
            data = Uri.parse(url)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Couldn't send message to $number via Whatsapp: whatsapp not resolved",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun sendMessage(text: String) {
        for (address in emailDestinations) sendByEmail(address, text)
        for (number in whatsAppNumbers) sendByWhatsApp(number, text)
    }
}