package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.databinding.DialogEmailConfigurationBinding
import ch.epfl.smsproxy.relay.EmailRelay
import ch.epfl.smsproxy.relay.EmailService
import ch.epfl.smsproxy.relay.Relay
import ch.epfl.smsproxy.relay.WhatsAppRelay
import ch.epfl.smsproxy.ui.fragment.GeneralPreferencesFragment
import ch.epfl.smsproxy.utils.Extensions.set
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var fragment: GeneralPreferencesFragment
    private lateinit var addPreferenceButton: FloatingActionButton
    private lateinit var checkPermissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragment = GeneralPreferencesFragment()

        supportFragmentManager.beginTransaction().add(R.id.preferences_container, fragment).commit()

        addPreferenceButton = findViewById(R.id.preferences_add_button)
        addPreferenceButton.setOnClickListener {
            displayRemoteOptions()
        }

        checkPermissionButton = findViewById(R.id.permissions_button)
        checkPermissionButton.setOnClickListener {
            if (checkSelfPermission(this, READ_SMS) != PERMISSION_GRANTED || checkSelfPermission(
                    this, RECEIVE_SMS
                ) != PERMISSION_GRANTED
            ) {
                requestPermissions(this, arrayOf(READ_SMS, RECEIVE_SMS, RECEIVE_MMS), 1000)
            } else {
                val text = getString(R.string.permissions_ok)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDefaultPort(startTls: Boolean): String =
        if (startTls) resources.getString(R.string.smtp_ssl_port)
        else resources.getString(R.string.smtp_start_tls_port)

    private suspend fun configureEmailRelay(): EmailRelay = suspendCoroutine { continuation ->
        runOnUiThread {
            val binding = DialogEmailConfigurationBinding.inflate(layoutInflater)
            binding.apply {
                smtpTlsEnabled.setOnCheckedChangeListener { _, isChecked ->
                    val currentPort = smtpPort.editText?.text?.toString()
                    if (currentPort == getDefaultPort(!isChecked))
                        smtpPort.editText?.text?.set(getDefaultPort(isChecked))
                }
                confirmButton.setOnClickListener {
                    runCatching {
                        val service = EmailService(
                            smtpHost.editText?.text?.toString()!!,
                            smtpPort.editText?.text?.toString()?.toIntOrNull()!!,
                            smtpUser.editText?.text?.toString()!!,
                            smtpPassword.editText?.text?.toString(),
                            smtpTlsEnabled.isChecked,
                        )
                        continuation.resume(
                            EmailRelay(
                                service, destination.editText?.text?.toString()!!
                            )
                        )
                    }
                }
            }

            AlertDialog.Builder(this).apply {
                setView(binding.root)
            }.create().apply {
                setCanceledOnTouchOutside(false)
                show()
            }
        }
    }

    private suspend fun configureWhatsapp(): WhatsAppRelay = suspendCoroutine { continuation ->

    }

    private suspend fun configureRelay(type: String): Relay = when (type) {
        resources.getString(R.string.preference_notification_email) -> configureEmailRelay()
        resources.getString(R.string.preference_notification_whatsapp) -> configureWhatsapp()
        else -> throw IllegalArgumentException()
    }

    private fun displayRemoteOptions() {
        val available = resources.getStringArray(R.array.preference_notification_means)
        val dialog = AlertDialog.Builder(this).apply {
            setSingleChoiceItems(R.array.preference_notification_means, -1) { dialog, checked ->
                val type = resources.getStringArray(R.array.preference_notification_means)[checked]
                dialog.dismiss()

                CoroutineScope(Dispatchers.IO).launch {
                    println("Launched $type !!!")
                    configureRelay(type)
                }
            }
            setCancelable(true)
        }.create()

        dialog.apply {
            setCanceledOnTouchOutside(true)
            show()
        }
    }
}