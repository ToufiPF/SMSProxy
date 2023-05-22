package ch.epfl.smsproxy.relay

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SlackRelay(
    private val webhookUrl: URL
) : Relay {
    companion object {
        private val TAG = this::class.simpleName!!
    }

    private val gson = Gson()
    private fun textToJsonPayload(text: String): String =
        gson.toJson(mapOf("text" to text))

    override suspend fun relay(text: String) {
        val payload = textToJsonPayload(text)
        val bytes = payload.encodeToByteArray()

        withContext(Dispatchers.IO) {
            val connection = webhookUrl.openConnection() as HttpURLConnection
            try {
                connection.apply {
                    // configure the connection
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    setFixedLengthStreamingMode(bytes.size)

                    // write the actual payload
                    BufferedOutputStream(outputStream).use {
                        it.write(bytes)
                    }
                    Log.d(TAG, "Finished writing to connection.")
                    connection.connect()
                    Log.d(TAG, "Connected to slack webhook")
                }
            } finally {
                connection.disconnect()
                Log.d(TAG, "Disconnected.")
            }
        }
    }
}
