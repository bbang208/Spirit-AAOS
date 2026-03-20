package io.github.bbang208.spirit.ui.common

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import io.github.bbang208.spirit.R
import io.github.bbang208.spirit.data.source.vehicle.DrivingStateRepository
import javax.inject.Inject

abstract class DistractionAwareFragment : Fragment() {

    @Inject
    lateinit var drivingStateRepository: DrivingStateRepository

    abstract val allowedWhileDriving: Boolean

    private var overlayView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!allowedWhileDriving) {
            drivingStateRepository.isDriving.observe(viewLifecycleOwner) { isDriving ->
                if (isDriving) {
                    showDrivingOverlay()
                } else {
                    hideDrivingOverlay()
                }
            }
        }
    }

    private fun showDrivingOverlay() {
        if (overlayView != null) return
        val root = view as? FrameLayout ?: (view as? android.view.ViewGroup) ?: return

        overlayView = View.inflate(requireContext(), R.layout.overlay_driving_restriction, null).also {
            root.addView(it)
        }
    }

    private fun hideDrivingOverlay() {
        overlayView?.let { overlay ->
            (overlay.parent as? android.view.ViewGroup)?.removeView(overlay)
        }
        overlayView = null
    }

    override fun onDestroyView() {
        overlayView = null
        super.onDestroyView()
    }
}
