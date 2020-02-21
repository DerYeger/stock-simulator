package de.uniks.codliners.stock_simulator.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.uniks.codliners.stock_simulator.databinding.FragmentHistoryBinding
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class HistoryFragment : BaseFragment() {

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
        binding.historyRecyclerView.adapter = HistoryRecyclerViewAdapter(
            onClickListener = OnClickListener { transaction ->
                val action = HistoryFragmentDirections.actionNavigationHistoryToShareFragment(transaction.id, transaction.type)
                findNavController().navigate(action)
            },
            locale = resources.configuration.locale
        )
        binding.lifecycleOwner = this
        return binding.root
    }
}