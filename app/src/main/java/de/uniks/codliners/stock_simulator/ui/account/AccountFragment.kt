package de.uniks.codliners.stock_simulator.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import de.uniks.codliners.stock_simulator.databinding.FragmentAccountBinding
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import de.uniks.codliners.stock_simulator.updateLineChart
import timber.log.Timber


class AccountFragment : BaseFragment() {

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
                    AccountFragmentDirections.actionNavigationAccountToShareFragment(symbol.id, symbol.type)
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this

        initLineChart(binding.accountChart, context!!)

        viewModel.balanceChanged.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })

        viewModel.balancesLimited.observe(viewLifecycleOwner, Observer { balanceList ->
            run {
                if (balanceList.isEmpty()) return@run

                val referenceTimestamp = balanceList[0].timestamp
                val entries = balanceList.map { balance ->
                    Entry(
                        (balance.timestamp - referenceTimestamp).toFloat(),
                        balance.value.toFloat()
                    )
                }
                updateLineChart(binding.accountChart, entries, "Account Balance", resources.configuration.locale, referenceTimestamp)
            }
        })

        initLineChart(binding.depotChart, context!!)

        viewModel.depotValuesLimited.observe(viewLifecycleOwner, Observer { depotValues ->
            run {
                if (depotValues.isEmpty()) return@run

                val referenceTimestamp = depotValues[0].timestamp
                val entries = depotValues.map { depotValue ->
                    Entry(
                        (depotValue.timestamp - referenceTimestamp).toFloat(),
                        depotValue.value.toFloat()
                    )
                }
                updateLineChart(binding.depotChart, entries, "Depot Balance", resources.configuration.locale, referenceTimestamp)
            }
        })

        return binding.root
    }
}
