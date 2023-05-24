package ch.epfl.smsproxy.relay

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class EmailRelayTest {

    private lateinit var service: EmailService
    private lateinit var destination: String

    @Before
    fun init() {
        service = mockk(relaxed = true)
        destination = "test@gmail.com"
    }

    @Test
    fun relaySendsMessageToRecipient(): Unit = runBlocking {
        val text = "hello \"w0rld\" !"
        val relay = EmailRelay(service, destination)
        relay.relay(text)

        verify {
            service.send(arrayOf(destination), arrayOf(), arrayOf(), any(), text)
        }
    }
}
