package de.uniks.codliners.stock_simulator.ui.quote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding

class QuoteFragment : Fragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val symbol = QuoteFragmentArgs.fromBundle(arguments!!).symbol
        QuoteViewModel.Factory(activity!!.application, symbol)
    }

    private lateinit var binding: FragmentQuoteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuoteBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}