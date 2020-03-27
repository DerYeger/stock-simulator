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

/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for viewing account related information like balances and the depot.
 *
 * @author Jan Müller
 * @author Jonas Thelemann
 */
class AccountFragment : BaseFragment() {

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentAccountBinding

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
        binding = FragmentAccountBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.depotRecyclerView.adapter =
            DepotQuoteListAdapter(OnClickListener { symbol ->
                val action =
                    AccountFragmentDirections.actionNavigationAccountToShareFragment(
                        symbol.id,
                        symbol.type
                    )
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this

        initLineChart(binding.accountChart, requireContext())

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

                // resources.configuration.locales[0] requires API level 24
                @Suppress("DEPRECATION")
                updateLineChart(
                    binding.accountChart,
                    entries,
                    "Account Balance",
                    resources.configuration.locale,
                    referenceTimestamp
                )
            }
        })

        initLineChart(binding.depotChart, requireContext())

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

                // resources.configuration.locales[0] requires API level 24
                @Suppress("DEPRECATION")
                updateLineChart(
                    binding.depotChart,
                    entries,
                    "Depot Balance",
                    resources.configuration.locale,
                    referenceTimestamp
                )
            }
        })

        return binding.root
    }
}
