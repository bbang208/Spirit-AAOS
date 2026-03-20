package io.github.bbang208.spirit.ui.trackselect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentTrackSelectBinding
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment

@AndroidEntryPoint
class TrackSelectFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = true

    private var _binding: FragmentTrackSelectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackSelectViewModel by viewModels()

    private lateinit var trackAdapter: TrackListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackSelectBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToggleGroup()
        setupButtons()
        observeTracks()
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackListAdapter { track ->
            findNavController().navigate(
                R.id.action_trackSelect_to_preSession,
                Bundle().apply { putString("trackId", track.id) }
            )
        }
        binding.rvTracks.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = trackAdapter
        }
    }

    private fun setupToggleGroup() {
        val tabs = listOf(binding.btnTabOfficial, binding.btnTabMyTracks, binding.btnTabNearby)
        viewModel.selectedTab.observe(viewLifecycleOwner) { index ->
            tabs.forEach { it.isChecked = it == tabs[index] }
        }
        binding.btnTabOfficial.setOnClickListener { viewModel.selectTab(0) }
        binding.btnTabMyTracks.setOnClickListener { viewModel.selectTab(1) }
        binding.btnTabNearby.setOnClickListener { viewModel.selectTab(2) }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnCreateTrack.setOnClickListener {
            findNavController().navigate(R.id.action_trackSelect_to_trackCreation)
        }
    }

    private fun observeTracks() {
        viewModel.displayedTracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
