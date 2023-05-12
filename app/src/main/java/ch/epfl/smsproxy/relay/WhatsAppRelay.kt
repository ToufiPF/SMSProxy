package ch.epfl.smsproxy.relay

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import ch.epfl.smsproxy.R
import java.net.URLEncoder

class WhatsAppRelay(
    private val context: Context,
    number: String,
) : Relay {

    private val number = number.removePrefix("00").removePrefix("+")

    override fun relay(text: String) {
        val encodedText = URLEncoder.encode(text, "utf-8")

        val url = "https://api.whatsapp.com/send?phone=$number&text=$encodedText"
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
}