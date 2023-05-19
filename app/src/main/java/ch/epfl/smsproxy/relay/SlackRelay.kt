package ch.epfl.smsproxy.relay

import com.google.gson.Gson
import java.io.BufferedOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SlackRelay(
    private val webhookUrl: URL
) : Relay {
    private val gson = Gson()
    private fun textToJsonPayload(text: String): String =
        gson.toJson(mapOf("text" to text))

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
                BufferedOutputStream(outputStream).use {
                    it.write(bytes)
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
