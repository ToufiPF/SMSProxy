package ch.epfl.smsproxy.relay

import android.icu.util.Calendar
import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message.RecipientType
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService(
    private val smtpServer: String,
    private val smtpPort: Int,
    private val senderAddress: String,
    private val senderPassword: String?,
    private val startTls: Boolean,
) {
    companion object {
        private val TAG = this::class.simpleName!!
    }

    private class SMTPAuthenticator(
        private val username: String,
        private val password: String,
    ) : Authenticator() {
        override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
    }

    fun send(
        toAddresses: Array<String>,
        ccAddresses: Array<String>,
        bccAddresses: Array<String>,
        subject: String,
        text: String
    ) {
        val props = Properties().apply {
            put("mail.smtp.host", smtpServer)
            put("mail.smtp.port", smtpPort.toString())
            put("mail.smtp.starttls.enable", startTls.toString())
            put("mail.smtp.auth", (senderPassword != null).toString())

            put("mail.smtp.user", senderAddress)
        }
        val auth = senderPassword?.let { SMTPAuthenticator(senderAddress, it) }
        Log.d(TAG, "SMTP props: $props, auth: $auth")

        val session = Session.getInstance(props, auth)
        session.debug = true
        val msg = MimeMessage(session).apply {
            setSubject(subject)
            setText(text)
            setFrom(InternetAddress(senderAddress))
            sender = InternetAddress(senderAddress)
            sentDate = Calendar.getInstance().time

            listOf(RecipientType.TO, RecipientType.CC, RecipientType.BCC).zip(
                listOf(toAddresses, ccAddresses, bccAddresses)
            ) { type, addresses ->
                setRecipients(type, addresses.map { InternetAddress(it) }.toTypedArray())
            }
        }
        Log.d(TAG, "Sender: ${msg.sender}, msg: ${msg.subject}, recipients: ${msg.allRecipients}")

        try {
            Transport.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}