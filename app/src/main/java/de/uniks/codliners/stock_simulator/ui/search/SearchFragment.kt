package de.uniks.codliners.stock_simulator.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.uniks.codliners.stock_simulator.databinding.FragmentSearchBinding
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.OnClickListener

/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for searching assets.
 *
 * @author Jan Müller
 */
class SearchFragment : BaseFragment() {

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentSearchBinding

    /**
     * Sets up the fragment view.
     *
     * @param inflater The layout inflater.
     * @param container The view group.
     * @param savedInstanceState The saved instance state.
     * @return The set up fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.searchResultRecyclerView.adapter =
            SymbolListAdapter(OnClickListener { searchResult ->
                val action =
                    SearchFragmentDirections.actionNavigationSearchToShareFragment(
                        searchResult.id,
                        searchResult.type
                    )
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this
        return binding.root
    }
}
