package io.github.bbang208.spirit.ui.presession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.data.models.Sector
import io.github.bbang208.spirit.databinding.FragmentPreSessionBinding
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment
import io.github.bbang208.spirit.util.EventObserver

@AndroidEntryPoint
class PreSessionFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = false

    private var _binding: FragmentPreSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PreSessionViewModel by viewModels()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreSessionBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sectorCount.observe(viewLifecycleOwner) { count ->
            binding.tvSectorValue.text = count.toString()
        }

        binding.switchGhost.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setGhostEnabled(isChecked)
        }

        binding.btnSectorMinus.setOnClickListener {
            viewModel.decrementSectors()
        }

        binding.btnSectorPlus.setOnClickListener {
            viewModel.incrementSectors()
        }

        viewModel.track.observe(viewLifecycleOwner) { track ->
            if (track == null) return@observe

            binding.tvTrackName.text = track.name

            val lengthText = if (track.lengthMeters >= 1000) {
                String.format("%.1f km", track.lengthMeters / 1000f)
            } else {
                "${track.lengthMeters.toInt()}m"
            }
            binding.tvTrackLength.text = lengthText

            // Parse outline and sectors from JSON
            val outlineType = object : TypeToken<List<GpsPoint>>() {}.type
            val outlinePoints: List<GpsPoint> = gson.fromJson(track.outlineJson, outlineType) ?: emptyList()

            val sectorType = object : TypeToken<List<Sector>>() {}.type
            val sectors: List<Sector> = gson.fromJson(track.sectorsJson, sectorType) ?: emptyList()

            if (outlinePoints.isNotEmpty()) {
                binding.trackMapView.setTrackData(outlinePoints, sectors)
            }
        }

        // Observe session creation
        viewModel.sessionCreated.observe(viewLifecycleOwner, EventObserver { sessionId ->
            findNavController().navigate(
                R.id.action_preSession_to_liveTiming,
                bundleOf("trackId" to viewModel.trackId, "sessionId" to sessionId)
            )
        })

        binding.btnGo.setOnClickListener {
            viewModel.createSession()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
