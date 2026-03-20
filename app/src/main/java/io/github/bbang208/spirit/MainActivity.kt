package io.github.bbang208.spirit

import ai.pleos.playground.vehicle.Vehicle
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.bbang208.spirit.data.source.vehicle.DrivingStateRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var vehicle: Vehicle

    @Inject
    lateinit var drivingStateRepository: DrivingStateRepository

    private val requestCarSpeedPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.d("CAR_SPEED permission granted")
        } else {
            Timber.w("CAR_SPEED permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vehicle.initialize()
        drivingStateRepository.startMonitoring()
        Timber.d("MainActivity created, Vehicle initialized")

        requestCarSpeedPermission.launch("android.car.permission.CAR_SPEED")
    }

    override fun onDestroy() {
        super.onDestroy()
        drivingStateRepository.stopMonitoring()
        vehicle.release()
        Timber.d("Vehicle released")
    }
}
