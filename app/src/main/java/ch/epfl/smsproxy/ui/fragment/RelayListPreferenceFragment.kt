package ch.epfl.smsproxy.ui.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.smsproxy.R
import ch.epfl.smsproxy.databinding.FragmentRelayListItemBinding

class RelayListPreferenceFragment : Fragment(R.layout.fragment_relay_list),
    OnSharedPreferenceChangeListener {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }

        const val PREF_NAME = "RelayListPreferences"
    }

    class ViewHolder(
        private val binding: FragmentRelayListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(elem: String) {
            binding.relayName.text = elem
        }
    }

    class Adapter(
        private val inflater: LayoutInflater
    ) : ListAdapter<String, ViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(FragmentRelayListItemBinding.inflate(inflater, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ListAdapter<String, ViewHolder>
    private lateinit var preferences: SharedPreferences


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.relay_recycler)
        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = Adapter(requireActivity().layoutInflater)
        recycler.adapter = adapter

        preferences = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    override fun onResume() {
        val relays = preferences.all.keys.toList()
        adapter.submitList(relays)

        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        val added = prefs?.getBoolean(key, false) ?: false

        val currentList = ArrayList(adapter.currentList)
        if (added) currentList.add(key)
        else currentList.remove(key)

        adapter.submitList(currentList)
    }
}
