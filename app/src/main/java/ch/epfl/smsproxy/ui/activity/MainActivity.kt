package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.fragment.RelayListPreferenceFragment
import ch.epfl.smsproxy.ui.fragment.RelayListPreferenceFragment.Companion.PREF_NAME
import ch.epfl.toufi.android_utils.LogicExtensions.reduceAll
import ch.epfl.toufi.android_utils.ui.UIExtensions.checkHasPermissions
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var relayListPreferences: SharedPreferences
    private lateinit var relayListFragment: RelayListPreferenceFragment
    private lateinit var addPreferenceButton: FloatingActionButton
    private lateinit var checkPermissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        relayListPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        relayListFragment = RelayListPreferenceFragment()

        supportFragmentManager.beginTransaction().add(R.id.preferences_container, relayListFragment)
            .commit()

        addPreferenceButton = findViewById(R.id.preferences_add_button)
        addPreferenceButton.setOnClickListener {
            displayRemoteOptions()
        }

        checkPermissionButton = findViewById(R.id.permissions_button)
        checkPermissionButton.setOnClickListener {
            if (checkHasPermissions(READ_SMS, RECEIVE_SMS, RECEIVE_MMS).reduceAll()) {
                val text = getString(R.string.permissions_ok)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions(this, arrayOf(READ_SMS, RECEIVE_SMS, RECEIVE_MMS), 1000)
            }
        }
    }

    private fun getPreferenceIndex(): Int = relayListPreferences.all.size

    private fun newConfiguration(fragmentId: String, prefBaseName: String) {
        val prefName = "${prefBaseName}_${getPreferenceIndex()}"

        // Record new sharedPreference name
        relayListPreferences.edit()
            .putBoolean(prefName, true)
            .apply()

        val intent = Intent(this, PreferenceActivityImpl::class.java)
        intent.putExtra(PreferencesActivity.EXTRA_TITLE, fragmentId)
        intent.putExtra(PreferencesActivity.EXTRA_PREFERENCES_ID, fragmentId)
        intent.putExtra(PreferenceActivityImpl.EXTRA_PREFERENCES_NAME, prefName)
        startActivity(intent)
    }

    private fun newEmailConfiguration() {
        newConfiguration("Email", "email")
    }

    private fun newWhatsappConfiguration() {
        newConfiguration("What's app", "whats_app")
    }

    private fun displayRemoteOptions() {
        AlertDialog.Builder(this).apply {
            setCancelable(true)

            setSingleChoiceItems(R.array.preference_notification_means, -1) { dialog, checked ->
                val type = resources.getStringArray(R.array.preference_notification_means)[checked]
                dialog.dismiss()

                when (type) {
                    getString(R.string.preference_notification_email) -> newEmailConfiguration()

                    getString(R.string.preference_notification_whatsapp) -> newWhatsappConfiguration()
                }
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.create().apply {
            setCanceledOnTouchOutside(true)
        }.show()
    }
}
