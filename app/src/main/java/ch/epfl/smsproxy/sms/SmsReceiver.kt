package ch.epfl.smsproxy.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.provider.Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED
import android.telephony.TelephonyManager.EXTRA_STATE_IDLE
import android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK
import android.telephony.TelephonyManager.EXTRA_STATE_RINGING
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    companion object {
        fun timestampToString(timeMs: Long): String {
            return DateFormat.format("dd/MM, HH:mm", timeMs).toString()
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val notOffHookedCalls: MutableSet<String?> = hashSetOf()
    }

    @Inject
    lateinit var sendHelper: MessageSender

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            SMS_RECEIVED_ACTION, DATA_SMS_RECEIVED_ACTION -> {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in messages) {
                    val time = timestampToString(sms.timestampMillis)
                    val sender = sms.displayOriginatingAddress
                    val body = sms.displayMessageBody

                    val sentText = "At $time, $sender sent:\n$body"
                    CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
                        sendHelper.broadcast(sentText)
                    }
                }
            }

            ACTION_PHONE_STATE_CHANGED -> {
                @Suppress("DEPRECATION") // worst case, just get null as number
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                    EXTRA_STATE_IDLE -> {
                        synchronized(notOffHookedCalls) {
                            if (number !in notOffHookedCalls) {
                                return
                            }
                        }
                        val timestamp = System.currentTimeMillis()
                        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
                            delay(1000)

                            synchronized(notOffHookedCalls) {
                                notOffHookedCalls.remove(number)
                                if (number == null && notOffHookedCalls.isNotEmpty()) {
                                    return@launch
                                }
                                notOffHookedCalls.remove(null)
                            }

                            val sender = number ?: "<unknown>"
                            val sentText = "At $timestamp, you missed a call from $sender"
                            Log.i(this::class.simpleName, sentText)
                            sendHelper.broadcast(sentText)
                        }
                    }

                    EXTRA_STATE_OFFHOOK -> synchronized(notOffHookedCalls) {
                        notOffHookedCalls.remove(number)
                    }

                    EXTRA_STATE_RINGING -> synchronized(notOffHookedCalls) {
                        notOffHookedCalls.add(number)
                    }
                }
            }

            else -> {
                Log.w(
                    this::class.simpleName,
                    "Received intent with invalid action '${intent?.action}'"
                )
            }
        }
    }
}
