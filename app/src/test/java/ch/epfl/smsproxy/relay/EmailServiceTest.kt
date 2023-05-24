package ch.epfl.smsproxy.relay

import android.icu.util.Calendar
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.Date
import java.util.Properties
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailServiceTest {

    private lateinit var host: String
    private var port: Int = 465
    private lateinit var user: String
    private var pwd: String? = null
    private var startTls: Boolean = false
    private lateinit var date: Date

    private lateinit var session: Session
    private lateinit var properties: CapturingSlot<Properties>
    private lateinit var mimeMessage: CapturingSlot<MimeMessage>
    private lateinit var service: EmailService

    @Before
    fun init() {
        date = Date.from(Instant.now())
        properties = CapturingSlot()
        mimeMessage = CapturingSlot()
        session = mockk(relaxed = true)
    }

    private fun runTest(testFun: () -> Unit) {
        mockkStatic(Session::class, Calendar::class, Transport::class) { //
            every { Session.getInstance(capture(properties)) } returns session
            every { Session.getInstance(capture(properties), any()) } returns session
            every { Calendar.getInstance().time } returns date
            every { Transport.send(capture(mimeMessage)) } returns Unit

            testFun()
        }
    }

    @Test
    fun callsTransport() = runTest {
        host = "smtp.mock.com"
        port = 465
        user = "user@mock.com"
        pwd = null
        startTls = false
        date = Date.from(Instant.now())

        service = EmailService(host, port, user, pwd, startTls)

        val subject = "Hello world!!"
        val text = "I sure hope mockk will be able to mock the javax.mail library"
        val to = arrayOf("abc@epfl.ch", "def@gmail.com")
        val cc = arrayOf("abc@epfl.ch", "def@gmail.com")
        val bcc = arrayOf("abc@epfl.ch", "def@gmail.com")
        service.send(to, cc, bcc, subject, text)

        val props = properties.captured
        assertEquals(host, props["mail.smtp.host"])
        assertEquals(port.toString(), props["mail.smtp.port"])
        assertEquals(startTls.toString(), props["mail.smtp.starttls.enable"])
        assertEquals(false.toString(), props["mail.smtp.auth"])
        assertEquals(user, props["mail.smtp.user"])

        val msg = mimeMessage.captured
        assertEquals(subject, msg.subject)
        assertArrayEquals(arrayOf(InternetAddress(user)), msg.from)
        assertEquals(InternetAddress(user), msg.sender)
        assertEquals("text/plain", msg.contentType)
        assertEquals(text, msg.content)

        verify {
            Session.getInstance(any(), null)
            Transport.send(msg)
        }
    }
}
