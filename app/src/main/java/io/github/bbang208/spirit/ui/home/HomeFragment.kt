package io.github.bbang208.spirit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentHomeBinding
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment

@AndroidEntryPoint
class HomeFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = true

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var sessionAdapter: SessionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSessions()

        binding.btnStartSession.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_trackSelect)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
    }

    private fun setupRecyclerView() {
        sessionAdapter = SessionListAdapter { sessionWithTrack ->
            val bundle = Bundle().apply {
                putString("sessionId", sessionWithTrack.session.id)
            }
            findNavController().navigate(R.id.action_home_to_sessionDetail, bundle)
        }
        binding.rvRecentSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
    }

    private fun observeSessions() {
        viewModel.recentSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
