package ch.epfl.smsproxy.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.provider.Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.text.format.DateFormat
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    companion object {
        fun timestampToString(timeMs: Long): String {
            return DateFormat.format("dd/MM, hh:mm", timeMs).toString()
        }
    }

    @Inject
    lateinit var sendHelper: MessageSender

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (!(intent?.action == SMS_RECEIVED_ACTION || intent?.action == DATA_SMS_RECEIVED_ACTION)) {
            Log.w(this::class.simpleName, "Received intent with invalid action '${intent?.action}'")
            return
        }

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
}
