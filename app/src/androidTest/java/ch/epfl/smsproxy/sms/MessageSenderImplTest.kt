package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.RelayListFragment.Companion.PREF_NAME
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MessageSenderImplTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var sender: MessageSender

    private lateinit var instantiatedRelays: ArrayList<Triple<Relay, String, SharedPreferences>>

    private fun getString(@StringRes res: Int): String = context.getString(res)

    private fun runTest(testFun: suspend () -> Unit) {
        mockkObject(RelayFactory::class, recordPrivateCalls = false) {
            println("allloooo1")
            every {
                RelayFactory.instantiateFromPreference(any(), any<String>())
                RelayFactory.instantiateFromPreference(any(), any<SharedPreferences>())
            } answers {
                val relay: Relay = mockk(relaxUnitFun = true)
                instantiatedRelays.add(
                    Triple(
                        relay,
                        it.invocation.args[0] as String,
                        it.invocation.args[1] as SharedPreferences
                    )
                )
                relay
            }
            println("allloooo2")

            runBlocking(Dispatchers.Default) {
                testFun()
            }
        }
    }

    @Before
    fun init() {
        instantiatedRelays = arrayListOf()

        context = getApplicationContext()
        prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    @Test
    fun broadcastSendsToAllPreviouslyConfiguredRelays() {
        prefs.edit().putString("relay_1", "type_1")

        runTest {
            sender = MessageSenderImpl(context)

            sender.broadcast("test message")

            assertEquals(1, instantiatedRelays.size)
            coVerify {
                instantiatedRelays.first().first.relay("test message")
            }
            confirmVerified()
            assertEquals("type_1", instantiatedRelays.first().second)
        }
    }
}
