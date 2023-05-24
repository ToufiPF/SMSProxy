package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKMatcherScope
import io.mockk.coVerify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
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

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sendHelper: MessageSender
    private lateinit var context: Context
    private lateinit var receiver: SmsReceiver

    @Before
    fun init() {
        hiltRule.inject()

        context = getApplicationContext()
        receiver = SmsReceiver()
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

        coVerify {
            sendHelper.broadcast(
                and(contains("27838890001"), and(contains("29/03"), contains("hellohello")))
            )
            sendHelper.broadcast(
                and(contains("27838890101"), and(contains("07/01"), contains("Hi there!")))
            )
        }
    }
}
