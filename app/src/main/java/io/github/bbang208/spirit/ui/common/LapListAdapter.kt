package io.github.bbang208.spirit.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.source.local.db.entity.LapEntity
import io.github.bbang208.spirit.databinding.ItemLapRowBinding
import io.github.bbang208.spirit.util.TimeFormatter

class LapListAdapter : DataBoundListAdapter<LapEntity, ItemLapRowBinding>(DIFF_CALLBACK) {

    override fun createBinding(parent: ViewGroup): ItemLapRowBinding {
        return ItemLapRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ItemLapRowBinding, item: LapEntity) {
        binding.lap = item
        val context = binding.root.context

        binding.tvLapIndex.text = context.getString(R.string.lap_index_format, item.lapIndex)
        binding.tvLapTime.text = TimeFormatter.formatLapTime(item.lapTimeMs)

        if (item.isPersonalBest) {
            val green = ContextCompat.getColor(context, R.color.switch_on)
            binding.tvLapTime.setTextColor(green)
            binding.tvDelta.text = context.getString(R.string.no_best_time)
            binding.tvDelta.setTextColor(green)
        } else {
            val defaultColor = ContextCompat.getColor(context, R.color.text_dark_primary)
            binding.tvLapTime.setTextColor(defaultColor)

            item.deltaToBest?.let { delta ->
                binding.tvDelta.text = TimeFormatter.formatDelta(delta)
                val deltaColor = if (delta > 0) {
                    ContextCompat.getColor(context, R.color.call_end_normal)
                } else {
                    ContextCompat.getColor(context, R.color.switch_on)
                }
                binding.tvDelta.setTextColor(deltaColor)
            } ?: run {
                binding.tvDelta.text = ""
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LapEntity>() {
            override fun areItemsTheSame(oldItem: LapEntity, newItem: LapEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LapEntity, newItem: LapEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
