package de.uniks.codliners.stock_simulator.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.an.biometric.BiometricUtils
import com.google.android.material.snackbar.Snackbar
import de.uniks.codliners.stock_simulator.*
import de.uniks.codliners.stock_simulator.databinding.FragmentSettingsBinding
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for changing various options, resetting user data and refreshing available symbols.
 *
 * @author TODO
 * @author Jan Müller
 * @author Jonas Thelemann
 */
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var preferences: SharedPreferences

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "prefs_fingerprint_added" -> {
                    if (requireActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(
                            getString(R.string.prefs_fingerprint_added),
                            false
                        )
                    ) {
                        binding.settingsFingerprint.text = getString(R.string.remove_fingerprint)
                    } else {
                        binding.settingsFingerprint.text = getString(R.string.add_fingerprint)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.coingeckoAttributionText.movementMethod = LinkMovementMethod.getInstance()
        binding.iexAttributionText.movementMethod = LinkMovementMethod.getInstance()

        // Initialize preferences.
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        // Fire preference changed event to update the (initial) fingerprint button value
        listener.onSharedPreferenceChanged(preferences, "prefs_fingerprint_added")

        viewModel.symbolRepositoryStateAction.observe(
            viewLifecycleOwner,
            Observer { state: SymbolRepository.State? ->
                if (state === null) return@Observer
                when (state) {
                    SymbolRepository.State.Refreshing -> Snackbar.make(
                        requireView(),
                        R.string.refreshing_symbols,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    SymbolRepository.State.Done -> Snackbar.make(
                        requireView(),
                        R.string.symbols_refresh_success,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    is SymbolRepository.State.Error -> Snackbar.make(
                        requireView(),
                        state.exception.extractErrorMessageResource<UnknownHostException>(R.string.no_connection) {
                            R.string.unable_to_fetch_symbols
                        },
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                viewModel.onSymbolActionCompleted()
            })

        // React to reset button clicks.
        viewModel.clickResetStatus.observe(viewLifecycleOwner, Observer { status ->
            if (status) {
                val context = requireContext()
                CoroutineScope(Dispatchers.Unconfined).launch {
                    context.resetAccount()
                    context.resetHistory()
                    context.resetQuotes()
                    context.resetStockbrot()
                    context.resetNews()
                    context.resetAchievements()
                }

                Snackbar.make(
                    requireView(),
                    R.string.data_reset_success,
                    Snackbar.LENGTH_SHORT
                ).show()

                viewModel.onGameReset()
            }
        })

        // React to fingerprint button clicks.
        viewModel.toggleFingerprintStatus.observe(viewLifecycleOwner, Observer { status ->
            if (status) {
                // If fingerprint authentication is enabled...
                if (requireActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(
                        getString(R.string.prefs_fingerprint_added),
                        false
                    )
                ) {
                    // ... disable fingerprint authentication.
                    with(requireActivity().getPreferences(Context.MODE_PRIVATE).edit()) {
                        putBoolean(getString(R.string.prefs_fingerprint_added), false)
                        apply()
                    }
                } else {
                    // ... enable fingerprint authentication.
                    (activity as MainActivity).biometricManager.authenticate(activity as MainActivity)
                }

                viewModel.onFingerprintToggled()
            }
        })

        fingerprintButtonInit()

        return binding.root
    }

    private fun fingerprintButtonInit() {
        binding.settingsFingerprintStatus.text = ""

        if (!BiometricUtils.isSdkVersionSupported()) {
            binding.settingsFingerprintStatus.text =
                getString(R.string.prefs_fingerprint_status_sdk)
        }

        if (!BiometricUtils.isHardwareSupported(this.context)) {
            binding.settingsFingerprintStatus.text =
                getString(R.string.prefs_fingerprint_status_hardware)
        }

        if (!BiometricUtils.isFingerprintAvailable(this.context)) {
            binding.settingsFingerprintStatus.text =
                getString(R.string.prefs_fingerprint_status_fingerprint)
        }

        if (!BiometricUtils.isPermissionGranted(this.context)) {
            binding.settingsFingerprintStatus.text =
                getString(R.string.prefs_fingerprint_status_permissions)
        }

        binding.settingsFingerprint.isEnabled =
            BiometricUtils.isSdkVersionSupported()
                    && BiometricUtils.isHardwareSupported(this.context)
                    && BiometricUtils.isFingerprintAvailable(this.context)
                    && BiometricUtils.isPermissionGranted(this.context)
    }

    /**
     * Unregisters the preference change listener.
     */
    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Registers the preference change listener and triggers the fingerprint button initialization.
     */
    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(listener)
        fingerprintButtonInit()
    }
}
