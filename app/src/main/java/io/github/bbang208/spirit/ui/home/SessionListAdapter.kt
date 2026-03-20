package io.github.bbang208.spirit.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.source.local.db.entity.SessionWithTrack
import io.github.bbang208.spirit.databinding.ItemSessionCardBinding
import io.github.bbang208.spirit.ui.common.DataBoundListAdapter
import io.github.bbang208.spirit.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionListAdapter(
    private val onClick: (SessionWithTrack) -> Unit
) : DataBoundListAdapter<SessionWithTrack, ItemSessionCardBinding>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    override fun createBinding(parent: ViewGroup): ItemSessionCardBinding {
        return ItemSessionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ItemSessionCardBinding, item: SessionWithTrack) {
        binding.session = item.session
        binding.tvTrackName.text = item.track.name
        binding.tvDate.text = dateFormat.format(Date(item.session.startTime))
        binding.tvBestTime.text = item.session.bestLapTimeMs?.let {
            TimeFormatter.formatLapTime(it)
        } ?: binding.root.context.getString(R.string.no_best_time)
        binding.root.setOnClickListener { onClick(item) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SessionWithTrack>() {
            override fun areItemsTheSame(
                oldItem: SessionWithTrack,
                newItem: SessionWithTrack
            ): Boolean {
                return oldItem.session.id == newItem.session.id
            }

            override fun areContentsTheSame(
                oldItem: SessionWithTrack,
                newItem: SessionWithTrack
            ): Boolean {
                return oldItem.session == newItem.session && oldItem.track == newItem.track
            }
        }
    }
}
