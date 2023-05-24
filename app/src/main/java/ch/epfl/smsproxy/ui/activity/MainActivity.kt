package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.activity.RelayPreferencesActivity.Companion.EXTRA_PREFERENCES_NAME
import ch.epfl.smsproxy.ui.fragment.RelayListFragment
import ch.epfl.smsproxy.ui.fragment.RelayListFragment.Companion.PREF_NAME
import ch.epfl.toufi.android_utils.LogicExtensions.reduceAll
import ch.epfl.toufi.android_utils.permissions.MockPermissionsActivity
import ch.epfl.toufi.android_utils.ui.UIExtensions.checkHasPermissions
import ch.epfl.toufi.android_utils.ui.activity.PreferencesActivity.Companion.EXTRA_PREFERENCES_ID
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : MockPermissionsActivity(R.layout.activity_main) {

    companion object {
        private val TAG = this::class.simpleName!!

        private val PERMISSIONS = arrayOf(
            READ_SMS,
            RECEIVE_SMS,
            RECEIVE_MMS,
            READ_PHONE_STATE,
            READ_CALL_LOG,
        )
    }


    private lateinit var relayListPreferences: SharedPreferences
    private lateinit var relayListFragment: RelayListFragment
    private lateinit var addPreferenceButton: FloatingActionButton

    private val launchRelayPreferencesActivity =
        registerForActivityResult(StartActivityForResult()) { result ->
            requireNotNull(result)

            if (result.resultCode == RESULT_OK) {
                // Record new sharedPreference name
                val prefName = result.data?.getStringExtra(EXTRA_PREFERENCES_NAME)
                val prefType = result.data?.getStringExtra(EXTRA_PREFERENCES_ID)
                if (prefName != null && prefType != null) {
                    Log.i(TAG, "Added $prefName -> $prefType mapping to relayListPreferences")
                    relayListPreferences.edit().putString(prefName, prefType).apply()
                    return@registerForActivityResult
                }
            }
            Log.w(
                TAG,
                "Received invalid result from RelayPrefActivity: ${result.resultCode}, ${result.data?.extras}"
            )
        }

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true // otherwise menu not displayed
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_check_permissions -> {
                checkAndRequestPermissions()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun generatePreferenceName(relayType: String): String {
        var i = 0
        var prefName: String // =
        do {
            prefName = "${relayType}_$i"
            i += 1
        } while (relayListPreferences.getString(prefName, null) != null)
        return prefName
    }

    private fun newConfiguration(relayType: String) {
        val prefName = generatePreferenceName(relayType)
        val intent = RelayPreferencesActivity.makeIntent(
            applicationContext,
            title = prefName,
            preferenceId = relayType,
            preferenceName = prefName,
        )
        launchRelayPreferencesActivity.launch(intent)
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

    private fun checkAndRequestPermissions() {
        if (checkHasPermissions(*PERMISSIONS).reduceAll()) {
            val text = getString(R.string.permissions_ok)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions(this, PERMISSIONS, 1000)
        }
    }
}
