package ch.epfl.smsproxy.sms

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.RelayFactory
import ch.epfl.smsproxy.ui.fragment.RelayListFragment.Companion.PREF_NAME
import io.mockk.CapturingSlot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MessageSenderImplTest {

    private lateinit var instantiatedPrefs: MutableMap<String, SharedPreferences>
    private lateinit var instantiatedRelays: MutableMap<SharedPreferences, Relay>

    private lateinit var context: Context
    private lateinit var relayListPrefs: SharedPreferences

    private fun runTest(testFun: suspend () -> Unit) {
        mockkObject(RelayFactory, recordPrivateCalls = true) {
            every {
                RelayFactory.instantiateFromPreference(context, any<SharedPreferences>())
            } answers {
                val relay: Relay = mockk(relaxUnitFun = true)
                instantiatedRelays[it.invocation.args[1] as SharedPreferences] = relay
                relay
            }

            runBlocking {
                testFun()
            }
        }
    }

    @Before
    fun init() {
        instantiatedPrefs = mutableMapOf()
        instantiatedRelays = mutableMapOf()

        context = mockk()
        relayListPrefs = mockk()
        every { context.getSharedPreferences(PREF_NAME, MODE_PRIVATE) } returns relayListPrefs
        every { context.getSharedPreferences(not(PREF_NAME), MODE_PRIVATE) } answers {
            val name = it.invocation.args[0] as String
            val pref: SharedPreferences = mockk(name = name)
            instantiatedPrefs[name] = pref
            pref
        }
        every { relayListPrefs.registerOnSharedPreferenceChangeListener(any()) } returns Unit
        every { relayListPrefs.unregisterOnSharedPreferenceChangeListener(any()) } returns Unit
    }

    @Test
    fun broadcastSendsToAllPreviouslyConfiguredRelays() {
        val relayMap = mapOf("relay_1" to "type_1", "relay_2" to "type_2")
        every { relayListPrefs.all } returns relayMap
        every { relayListPrefs.getString(any(), any()) } answers {
            relayMap[it.invocation.args[0] as String]
        }

        runTest {
            val sender = MessageSenderImpl(context)

            val sendText = "test message"
            sender.broadcast(sendText)

            verify {
                context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                for (name in relayMap.keys) {
                    context.getSharedPreferences(name, MODE_PRIVATE)
                }
            }

            assertEquals(2, instantiatedPrefs.size)
            assertEquals(2, instantiatedRelays.size)

            assertEquals(relayMap.keys.toSortedSet(), instantiatedPrefs.keys.toSortedSet())
            assertEquals(instantiatedPrefs.values.toSet(), instantiatedRelays.keys.toSet())
            coVerify {
                instantiatedRelays.values.forEach { relay -> relay.relay(sendText) }
            }
        }
    }

    @Test
    fun broadcastSendsToNewlyConfiguredRelays() {
        // initially no relays configured
        every { relayListPrefs.all } returns mapOf()

        val slot = CapturingSlot<SharedPreferences.OnSharedPreferenceChangeListener>()

        runTest {
            val sender = MessageSenderImpl(context)
            verify {
                relayListPrefs.registerOnSharedPreferenceChangeListener(capture(slot))
            }

            val sendText = "test message"
            sender.broadcast(sendText)

            assertEquals(0, instantiatedRelays.size)

            // Add a new relay to the list & notify sender
            every { relayListPrefs.all } returns mapOf("name" to "type")
            every { relayListPrefs.getString("name", any()) } returns "type"

            slot.captured.onSharedPreferenceChanged(relayListPrefs, "name")
            sender.broadcast(sendText)

            assertEquals(1, instantiatedRelays.size)
            val relay = instantiatedRelays.values.first()
            coVerify { relay.relay(sendText) }

            assertEquals("name", instantiatedPrefs.keys.first())
        }
    }
}
