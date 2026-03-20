package io.github.bbang208.spirit.binding

import androidx.databinding.BindingAdapter
import io.github.bbang208.spirit.domain.timing.SectorState
import io.github.bbang208.spirit.ui.widget.GForceView
import io.github.bbang208.spirit.ui.widget.SectorBarView

@BindingAdapter("sectorStates")
fun setSectorStates(view: SectorBarView, states: List<SectorState>?) {
    states?.let { view.setSectorStates(it) }
}

@BindingAdapter("lateralG", "longitudinalG")
fun setGForce(view: GForceView, lateralG: Float?, longitudinalG: Float?) {
    view.setGForce(lateralG ?: 0f, longitudinalG ?: 0f)
}
