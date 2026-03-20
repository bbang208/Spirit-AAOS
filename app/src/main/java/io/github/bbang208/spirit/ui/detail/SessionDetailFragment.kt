package io.github.bbang208.spirit.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.databinding.FragmentSessionDetailBinding
import io.github.bbang208.spirit.ui.common.DistractionAwareFragment
import io.github.bbang208.spirit.ui.common.LapListAdapter
import io.github.bbang208.spirit.util.EventObserver
import io.github.bbang208.spirit.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SessionDetailFragment : DistractionAwareFragment() {

    override val allowedWhileDriving = false

    private var _binding: FragmentSessionDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionDetailViewModel by viewModels()
    private lateinit var lapAdapter: LapListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionDetailBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete_session) { _, _ ->
                    viewModel.deleteSession()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        viewModel.navigateBack.observe(viewLifecycleOwner, EventObserver {
            findNavController().popBackStack()
        })
    }

    private fun setupRecyclerView() {
        lapAdapter = LapListAdapter()
        binding.rvLaps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lapAdapter
        }
    }

    private fun observeData() {
        viewModel.laps.observe(viewLifecycleOwner) { laps ->
            lapAdapter.submitList(laps)
        }

        viewModel.session.observe(viewLifecycleOwner) { session ->
            session ?: return@observe
            binding.tvTotalLaps.text = getString(R.string.total_laps_format, session.totalLaps)
            binding.tvBestTime.text = getString(
                R.string.best_lap_format,
                session.bestLapTimeMs?.let { TimeFormatter.formatLapTime(it) }
                    ?: getString(R.string.no_best_time)
            )
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            binding.tvSessionDate.text = dateFormat.format(Date(session.startTime))
        }

        viewModel.track.observe(viewLifecycleOwner) { track ->
            track ?: return@observe
            binding.tvTrackName.text = track.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
