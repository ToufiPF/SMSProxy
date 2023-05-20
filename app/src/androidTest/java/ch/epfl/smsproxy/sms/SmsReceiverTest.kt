package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import io.mockk.MockKMatcherScope
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SmsReceiverTest {

    companion object {
        fun String.decodeHex(): ByteArray {
            check(length % 2 == 0) { "Must have an even length" }

            return chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }

        fun MockKMatcherScope.contains(subString: String) = match<String> {
            it.contains(subString)
        }
    }

    private lateinit var context: Context

    private lateinit var sendHelper: MessageSender
    private lateinit var receiver: SmsReceiver

    @Before
    fun init() {
        context = getApplicationContext()
        sendHelper = mockk(relaxed = true)
        receiver = SmsReceiver(sendHelper)
    }

    @Test
    fun callsSendHelperForEachReceivedMessage() {
        val messages = arrayOf(
            // SMSC (sms center), Sender num, other stuff
            // + timestamp yy/mm/dd (+ time millis)
            // + message: (len=0-1 bytes, message=remaining)
            ("07917238010010F5040BC87238880900F10000" + "99309251619580" + "0AE8329BFD4697D9EC37").decodeHex(),
            ("07917238010010F5040BC87238880901F10000" + "99107001619580" + "09C834888E2ECBCB21").decodeHex(),
        )

        val intent = Intent().apply {
            setClassName("com.android.mms", "com.android.mms.transaction.SmsReceiverService")
            action = Telephony.Sms.Intents.SMS_RECEIVED_ACTION
            putExtra("pdus", messages)
            putExtra("format", SmsMessage.FORMAT_3GPP)
        }
        receiver.onReceive(context, intent)

        verify {
            sendHelper.broadcast(
                and(contains("27838890001"), and(contains("29/03"), contains("hellohello")))
            )
            sendHelper.broadcast(
                and(contains("27838890101"), and(contains("07/01"), contains("Hi there!")))
            )
        }
    }
}
