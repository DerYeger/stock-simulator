package de.uniks.codliners.stock_simulator.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.historyRecyclerView.adapter = HistoryRecyclerViewAdapter()
        binding.lifecycleOwner = this
        return binding.root
    }
}