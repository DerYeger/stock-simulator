package de.uniks.codliners.stock_simulator.ui.quote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.onErrorActionCompleted()
            }
        })

        return binding.root
    }
}
