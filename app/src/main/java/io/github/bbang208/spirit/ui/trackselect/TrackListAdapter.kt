package io.github.bbang208.spirit.ui.trackselect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity
import io.github.bbang208.spirit.databinding.ItemTrackCardBinding
import io.github.bbang208.spirit.ui.common.DataBoundListAdapter

class TrackListAdapter(
    private val onClick: (TrackEntity) -> Unit
) : DataBoundListAdapter<TrackEntity, ItemTrackCardBinding>(DIFF_CALLBACK) {

    override fun createBinding(parent: ViewGroup): ItemTrackCardBinding {
        return ItemTrackCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ItemTrackCardBinding, item: TrackEntity) {
        binding.track = item

        // Format track length
        binding.tvTrackLength.text = if (item.lengthMeters >= 1000) {
            binding.root.context.getString(
                R.string.track_length_format, item.lengthMeters / 1000f
            )
        } else {
            binding.root.context.getString(
                R.string.track_length_m_format, item.lengthMeters.toInt()
            )
        }

        binding.root.setOnClickListener { onClick(item) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackEntity>() {
            override fun areItemsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
