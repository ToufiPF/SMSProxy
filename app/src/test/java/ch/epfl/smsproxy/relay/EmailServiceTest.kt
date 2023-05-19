package ch.epfl.smsproxy.relay

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

class EmailServiceTest {

    private lateinit var host: String
    private var port: Int = 465
    private lateinit var user: String
    private var pwd: String? = null
    private var startTls: Boolean = false

    private lateinit var session: Session
    private lateinit var mimeMessage: MimeMessage
    private lateinit var service: EmailService

    @Before
    fun init() {
        session = mockk(relaxed = true)
//        mimeMessage = mockk(relaxed = true)
    }

    private fun runTest(testFun: () -> Unit) {
//        mockkStatic(Session::class, Transport::class) { //
//            every { Session.getInstance(any()) } returns session
//            every { Session.getInstance(any(), any()) } returns session
//            every { Transport.send(any()) } returns Unit

//            mockkConstructor(MimeMessage::class) {
//                every { anyConstructed<MimeMessage>() } returns mimeMessage
//
//                testFun()
//            }
            testFun()
//        }
    }

    @Test
    fun callsTransport() = runTest {
        service = EmailService(host, port, user, pwd, startTls)

        val subject = "Hello world!!"
        val text = "I sure hope mockk will be able to mock the javax.mail library"
        val to = arrayOf("abc@epfl.ch", "def@gmail.com")
        val cc = arrayOf("abc@epfl.ch", "def@gmail.com")
        val bcc = arrayOf("abc@epfl.ch", "def@gmail.com")
        service.send(to, cc, bcc, subject, text)
//
//        verify {
//            Transport.send(any())
//        }
    }
}