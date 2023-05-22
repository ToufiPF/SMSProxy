package ch.epfl.smsproxy.ui.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.databinding.FragmentRelayListItemBinding
import ch.epfl.smsproxy.ui.activity.RelayPreferencesActivity

/**
 * Pair of
 * - unique [SharedPreferences] name,
 * - name of the [PreferenceFragmentCompat] to launch,
 */
typealias RelayEntry = Pair<String, String>

class RelayListFragment : Fragment(R.layout.fragment_relay_list), OnSharedPreferenceChangeListener {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RelayEntry>() {
            override fun areItemsTheSame(oldItem: RelayEntry, newItem: RelayEntry): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(oldItem: RelayEntry, newItem: RelayEntry): Boolean {
                return oldItem == newItem
            }
        }

        /**
         * Name of the [SharedPreferences] that holds the list of preferences names;
         * it consists uniquely of (String, String) pairs:
         * - key is the unique [SharedPreferences] name,
         * - value is the name of the [PreferenceFragmentCompat] to launch,
         * as understood by [RelayPreferencesActivity].
         */
        const val PREF_NAME = "RelayListPreferences"
    }

    class ViewHolder(
        private val binding: FragmentRelayListItemBinding,
        private val relayListPreferences: SharedPreferences,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(elem: RelayEntry) {
            binding.apply {
                relayName.text = elem.first
                relayName.setOnClickListener {
                    val intent = RelayPreferencesActivity.makeIntent(
                        it.context.applicationContext,
                        title = elem.first,
                        preferenceId = elem.second,
                        preferenceName = elem.first,
                    )
                    it.context.startActivity(intent)
                }
                relayDelete.setOnClickListener {
                    // Remove preference name from relayList preferences
                    relayListPreferences.edit().remove(elem.first).apply()

                    // Clear actual preference
                    it.context.deleteSharedPreferences(elem.first)
                }
            }
        }
    }

    class Adapter(
        private val inflater: LayoutInflater,
        private val relayListPreferences: SharedPreferences,
    ) : ListAdapter<RelayEntry, ViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            FragmentRelayListItemBinding.inflate(inflater, parent, false),
            relayListPreferences,
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private lateinit var relayListPrefs: SharedPreferences
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        relayListPrefs = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        recycler = view.findViewById(R.id.relay_recycler)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = Adapter(requireActivity().layoutInflater, relayListPrefs)
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val relays = relayListPrefs.all.map { it.key to it.value as String }
        adapter.submitList(relays)

        relayListPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        relayListPrefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        val fragmentId = prefs?.getString(key, null)

        val currentList = ArrayList(adapter.currentList)
        if (fragmentId != null) currentList.add(RelayEntry(key!!, fragmentId))
        else currentList.removeIf { it.first == key }

        adapter.submitList(currentList)
    }
}
