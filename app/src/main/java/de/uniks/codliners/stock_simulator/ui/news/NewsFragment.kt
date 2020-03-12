package de.uniks.codliners.stock_simulator.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.uniks.codliners.stock_simulator.databinding.FragmentNewsBinding

/**
 * The fragment that displays news.
 *
 * @author Jonas Thelemann
 * @author Jan Müller
 */
class NewsFragment : Fragment() {

    private val viewModel: NewsViewModel by viewModels {
        val args = NewsFragmentArgs.fromBundle(requireArguments())
        val symbol = args.symbol
        NewsViewModel.Factory(requireActivity().application, symbol)
    }

    private lateinit var binding: FragmentNewsBinding

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
        viewModel.errorAction.observe(viewLifecycleOwner, Observer { message: String? ->
            message?.let {
                Snackbar.make(
                    requireView(),
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
                viewModel.onErrorActionCompleted()
            }
        })

        binding = FragmentNewsBinding.inflate(inflater)
        binding.viewModel = viewModel

        // resources.configuration.locales[0] requires API level 24
        @Suppress("DEPRECATION")
        binding.newsRecyclerView.adapter = NewsAdapter(resources.configuration.locale)
        binding.lifecycleOwner = this

        viewModel.refresh()

        return binding.root
    }
}
