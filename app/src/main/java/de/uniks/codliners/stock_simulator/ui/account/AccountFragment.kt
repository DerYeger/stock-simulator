package de.uniks.codliners.stock_simulator.ui.account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels()

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.depotRecyclerView.adapter = DepotShareRecyclerViewAdapter()
        binding.lifecycleOwner = this
        return binding.root
    }
}