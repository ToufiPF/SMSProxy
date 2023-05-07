package ch.epfl.smsproxy.ui.activity

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_MMS
import android.Manifest.permission.RECEIVE_SMS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.ui.fragment.GeneralPreferencesFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var fragment: GeneralPreferencesFragment
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragment = GeneralPreferencesFragment()

        supportFragmentManager.beginTransaction().add(R.id.preferences_container, fragment).commit()

        button = findViewById(R.id.permissions_button)
        button.setOnClickListener {
            if (checkSelfPermission(this, READ_SMS) != PERMISSION_GRANTED
                || checkSelfPermission(this, RECEIVE_SMS) != PERMISSION_GRANTED
            ) {
                requestPermissions(this, arrayOf(READ_SMS, RECEIVE_SMS, RECEIVE_MMS), 1000)
            } else {
                val text = getString(R.string.permissions_ok)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}