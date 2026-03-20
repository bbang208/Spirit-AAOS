package io.github.bbang208.spirit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentSettingsBinding
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment

@AndroidEntryPoint
class SettingsFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = false

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleSpeedUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_kmh -> viewModel.setSpeedUnit(SettingsViewModel.UNIT_KMH)
                    R.id.btn_mph -> viewModel.setSpeedUnit(SettingsViewModel.UNIT_MPH)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
