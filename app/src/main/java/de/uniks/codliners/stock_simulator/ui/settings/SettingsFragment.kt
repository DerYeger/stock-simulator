package de.uniks.codliners.stock_simulator.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.an.biometric.BiometricUtils
import de.uniks.codliners.stock_simulator.*
import de.uniks.codliners.stock_simulator.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var preferences: SharedPreferences

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "prefs_fingerprint_added" -> {
                    if (activity!!.getPreferences(Context.MODE_PRIVATE).getBoolean(
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

        // Initialize preferences.
        preferences = activity!!.getPreferences(Context.MODE_PRIVATE)

        // Fire preference changed event to update the (initial) fingerprint button value
        listener.onSharedPreferenceChanged(preferences, "prefs_fingerprint_added")

        // React to reset button clicks.
        viewModel.clickResetStatus.observe(viewLifecycleOwner, Observer { status ->
            status?.let {

                // Reset click indicator.
                viewModel.clickResetStatus.value = null

                this.context!!.resetAccount()
                this.context!!.resetHistory()
                this.context!!.resetQuotes()
                this.context!!.resetStockbrot()

                Toast.makeText(this.context, "Data reset successfully.", Toast.LENGTH_SHORT).show()
            }
        })

        // React to fingerprint button clicks.
        viewModel.toggleFingerprintStatus.observe(viewLifecycleOwner, Observer { status ->
            status?.let {

                // Reset click indicator.
                viewModel.toggleFingerprintStatus.value = null

                // If fingerprint authentication is enabled...
                if (activity!!.getPreferences(Context.MODE_PRIVATE).getBoolean(
                        getString(R.string.prefs_fingerprint_added),
                        false
                    )
                ) {
                    // ... disable fingerprint authentication.
                    with(activity!!.getPreferences(Context.MODE_PRIVATE).edit()) {
                        putBoolean(getString(R.string.prefs_fingerprint_added), false)
                        apply()
                    }
                } else {
                    // ... enable fingerprint authentication.
                    (activity as MainActivity).biometricManager.authenticate(activity as MainActivity)
                }
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

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(listener)
        fingerprintButtonInit()
    }
}
