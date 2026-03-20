package io.github.bbang208.spirit.ui.trackcreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentTrackCreationBinding
import io.github.bbang208.spirit.domain.tracking.RecorderState
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment
import io.github.bbang208.spirit.util.EventObserver

@AndroidEntryPoint
class TrackCreationFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = true

    private var _binding: FragmentTrackCreationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackCreationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackCreationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnStartRecording.setOnClickListener {
            viewModel.startRecording()
        }

        binding.btnCancelRecording.setOnClickListener {
            viewModel.cancelRecording()
        }

        binding.btnSaveTrack.setOnClickListener {
            viewModel.saveTrack()
        }

        binding.btnSectorMinus.setOnClickListener {
            viewModel.decrementSectors()
        }

        binding.btnSectorPlus.setOnClickListener {
            viewModel.incrementSectors()
        }

        binding.etTrackName.doAfterTextChanged { text ->
            viewModel.trackName.value = text?.toString() ?: ""
        }
    }

    private fun observeState() {
        viewModel.recorderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                RecorderState.IDLE -> {
                    binding.btnStartRecording.visibility = View.VISIBLE
                    binding.recordingContainer.visibility = View.GONE
                    binding.completedContainer.visibility = View.GONE
                    binding.badgeRecording.visibility = View.GONE
                    updateStepper(0)
                }
                RecorderState.RECORDING -> {
                    binding.btnStartRecording.visibility = View.GONE
                    binding.recordingContainer.visibility = View.VISIBLE
                    binding.completedContainer.visibility = View.GONE
                    binding.badgeRecording.visibility = View.VISIBLE
                    updateStepper(1)
                }
                RecorderState.COMPLETED -> {
                    binding.btnStartRecording.visibility = View.GONE
                    binding.recordingContainer.visibility = View.GONE
                    binding.completedContainer.visibility = View.VISIBLE
                    binding.badgeRecording.visibility = View.GONE
                    updateStepper(2)
                }
            }
        }

        viewModel.livePoints.observe(viewLifecycleOwner) { points ->
            if (points.isNotEmpty()) {
                binding.trackMapView.setLivePoints(points)
            }
        }

        viewModel.totalDistance.observe(viewLifecycleOwner) { meters ->
            binding.tvRecordingDistance.text =
                getString(R.string.recording_distance_format, meters / 1000.0)
        }

        viewModel.gpsPointCount.observe(viewLifecycleOwner) { count ->
            binding.tvGpsPoints.text = getString(R.string.gps_points_format, count)
        }

        viewModel.distanceToStart.observe(viewLifecycleOwner) { meters ->
            binding.tvLoopDistance.text =
                getString(R.string.distance_meters_format, meters.toInt())
            // Progress: inversely proportional (closer = more progress)
            // Max distance considered: 1000m, auto-close at 50m
            val progress = ((1000.0 - meters.coerceIn(0.0, 1000.0)) / 1000.0 * 1000).toInt()
            binding.progressLoop.progress = progress
        }

        viewModel.sectorCount.observe(viewLifecycleOwner) { count ->
            binding.tvSectorCount.text = count.toString()
        }

        viewModel.trackSaved.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), R.string.track_created_toast, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        })
    }

    private fun updateStepper(activeStep: Int) {
        // Step 0: Idle, Step 1: Recording, Step 2: Complete
        binding.icStepIdle.setImageResource(
            if (activeStep > 0) R.drawable.ic_step_completed else R.drawable.ic_step_active
        )
        binding.tvStepIdle.setTextColor(
            requireContext().getColor(if (activeStep >= 0) R.color.switch_on else R.color.text_light_tertiary)
        )
        binding.line1.setBackgroundColor(
            requireContext().getColor(if (activeStep > 0) R.color.switch_on else R.color.basic_500)
        )

        binding.icStepRecording.setImageResource(
            when {
                activeStep > 1 -> R.drawable.ic_step_completed
                activeStep == 1 -> R.drawable.ic_step_active
                else -> R.drawable.ic_step_pending
            }
        )
        binding.tvStepRecording.setTextColor(
            requireContext().getColor(if (activeStep >= 1) R.color.text_light_primary else R.color.text_light_tertiary)
        )
        binding.line2.setBackgroundColor(
            requireContext().getColor(if (activeStep > 1) R.color.switch_on else R.color.basic_500)
        )

        binding.icStepComplete.setImageResource(
            when {
                activeStep >= 2 -> R.drawable.ic_step_active
                else -> R.drawable.ic_step_pending
            }
        )
        binding.tvStepComplete.setTextColor(
            requireContext().getColor(if (activeStep >= 2) R.color.text_light_primary else R.color.text_light_tertiary)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
