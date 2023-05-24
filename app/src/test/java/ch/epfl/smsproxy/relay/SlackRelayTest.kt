package ch.epfl.smsproxy.relay

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SlackRelayTest {

    private lateinit var url: URL
    private lateinit var urlConnection: HttpsURLConnection
    private lateinit var outputStream: ByteArrayOutputStream

    @Before
    fun init() {
        url = mockk(relaxed = true)
        urlConnection = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()

        every { url.openConnection() } answers { urlConnection }
        every { urlConnection.outputStream } returns outputStream
    }

    @Test
    fun relayPostsMessageToWebhook(): Unit = runBlocking {
        val text = "hello \"w0rld\" !"
        val expected = "{\"text\":\"hello \\\"w0rld\\\" !\"}"
        val relay = SlackRelay(url)
        relay.relay(text)

        verify {
            url.openConnection()
            urlConnection.disconnect()
        }
        val written = outputStream.toByteArray()

        assertEquals(expected, written.decodeToString())
    }
}
