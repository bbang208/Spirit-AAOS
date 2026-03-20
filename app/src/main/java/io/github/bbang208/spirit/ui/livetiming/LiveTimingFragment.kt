package io.github.bbang208.spirit.ui.livetiming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentLiveTimingBinding
import io.github.bbang208.spirit.domain.timing.TimingState
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment
import io.github.bbang208.spirit.ui.widget.GForceView
import io.github.bbang208.spirit.ui.widget.SectorBarView

@AndroidEntryPoint
class LiveTimingFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = true

    private var _binding: FragmentLiveTimingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LiveTimingViewModel by viewModels()

    private lateinit var sectorBarView: SectorBarView
    private lateinit var gForceView: GForceView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveTimingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add SectorBarView to placeholder
        sectorBarView = SectorBarView(requireContext())
        sectorBarView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.tvSectorBarArea.addView(sectorBarView)

        // Add GForceView to placeholder
        gForceView = GForceView(requireContext())
        gForceView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.gForcePlaceholder.addView(gForceView)

        // Observe sector states
        viewModel.sectorStates.observe(viewLifecycleOwner) { states ->
            sectorBarView.setSectorStates(states)
        }

        // Observe G-Force
        viewModel.lateralG.observe(viewLifecycleOwner) { lateral ->
            val longitudinal = viewModel.longitudinalG.value ?: 0f
            gForceView.setGForce(lateral, longitudinal)
        }
        viewModel.longitudinalG.observe(viewLifecycleOwner) { longitudinal ->
            val lateral = viewModel.lateralG.value ?: 0f
            gForceView.setGForce(lateral, longitudinal)
        }

        // Observe delta color
        viewModel.deltaColorRes.observe(viewLifecycleOwner) { colorRes ->
            if (colorRes != 0) {
                binding.tvDeltaTime.setTextColor(
                    resources.getColor(colorRes, requireContext().theme)
                )
            }
        }

        // Observe timing state for "waiting" message
        viewModel.timingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                TimingState.WAITING_FOR_START -> {
                    binding.tvLapTime.text = getString(R.string.waiting_for_start)
                }
                TimingState.IDLE -> {
                    binding.tvLapTime.text = "0:00.000"
                }
                else -> { /* RUNNING — data binding handles it */ }
            }
        }

        binding.btnStop.setOnClickListener {
            viewModel.stopSession()
            findNavController().navigate(
                R.id.action_liveTiming_to_sessionSummary,
                bundleOf("sessionId" to viewModel.sessionId)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
