package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.fragment.RelayListFragment
import ch.epfl.smsproxy.ui.fragment.RelayListFragment.Companion.PREF_NAME
import ch.epfl.toufi.android_utils.LogicExtensions.reduceAll
import ch.epfl.toufi.android_utils.ui.UIExtensions.checkHasPermissions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var relayListPreferences: SharedPreferences
    private lateinit var relayListFragment: RelayListFragment
    private lateinit var addPreferenceButton: FloatingActionButton
    private lateinit var checkPermissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        relayListPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        relayListFragment = RelayListFragment()

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

    private fun newConfiguration(relayType: String) {
        val prefName = "${relayType}_${getPreferenceIndex()}"

        // Record new sharedPreference name
        relayListPreferences.edit()
            .putString(prefName, relayType)
            .apply()

        RelayPreferencesActivity.launchIntent(
            this,
            title = prefName,
            preferenceId = relayType,
            preferenceName = prefName,
        )
    }

    private fun displayRemoteOptions() {
        AlertDialog.Builder(this).apply {
            setCancelable(true)

            setSingleChoiceItems(R.array.pref_type_display_names, -1) { dialog, checked ->
                val type = resources.getStringArray(R.array.pref_types)[checked]
                dialog.dismiss()

                newConfiguration(type)
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.create().apply {
            setCanceledOnTouchOutside(true)
        }.show()
    }
}
