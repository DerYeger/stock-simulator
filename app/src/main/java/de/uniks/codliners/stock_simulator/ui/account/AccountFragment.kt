package de.uniks.codliners.stock_simulator.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import de.uniks.codliners.stock_simulator.databinding.FragmentAccountBinding
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import de.uniks.codliners.stock_simulator.updateLineChart


class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.depotRecyclerView.adapter =
            DepotQuoteRecyclerViewAdapter(OnClickListener { symbol ->
                val action =
                    AccountFragmentDirections.actionNavigationAccountToShareFragment(symbol.symbol)
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this

        initLineChart(binding.accountChart, context!!, resources.configuration.locale)

        viewModel.balancesLimited.observe(viewLifecycleOwner, Observer { balanceList ->
            run {
                val entries = balanceList.map { balance ->
                    Entry(
                        balance.timestamp.toFloat(),
                        balance.value.toFloat()
                    )
                }
                updateLineChart(binding.accountChart, entries, "Account Balance")
            }
        })

        return binding.root
    }
}
