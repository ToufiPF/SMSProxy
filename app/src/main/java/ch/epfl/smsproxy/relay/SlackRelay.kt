package ch.epfl.smsproxy.relay

import org.json.JSONObject
import java.io.BufferedOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SlackRelay(
    webhook: String
) : Relay {
    private val webhookUrl: URL = URL(webhook)

    private fun textToJsonPayload(text: String): String =
        JSONObject.wrap(mapOf("text" to text))!!.toString()

    override fun relay(text: String) {
        val payload = textToJsonPayload(text)
        val bytes = payload.encodeToByteArray()

        val connection = webhookUrl.openConnection() as HttpsURLConnection
        try {
            connection.apply {
                // configure the connection
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                setFixedLengthStreamingMode(bytes.size)

                // write the actual payload
                BufferedOutputStream(outputStream).write(bytes)
            }
        } finally {
            connection.disconnect()
        }
    }
}
