package de.uniks.codliners.stock_simulator.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentShareBinding

class ShareFragment : Fragment() {

    private val viewModel: ShareViewModel by viewModels {
        val shareId = ShareFragmentArgs.fromBundle(arguments!!).shareId
        ShareViewModel.Factory(activity!!.application, shareId)
    }

    private lateinit var binding: FragmentShareBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShareBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}