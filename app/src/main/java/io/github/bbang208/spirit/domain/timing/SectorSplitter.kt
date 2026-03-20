package io.github.bbang208.spirit.domain.timing

import io.github.bbang208.spirit.data.models.Sector
import io.github.bbang208.spirit.domain.tracking.GpsTracker
import io.github.bbang208.spirit.util.GeoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectorSplitter @Inject constructor(
    private val gpsTracker: GpsTracker
) {
    private var gates: List<GateEndpoints> = emptyList()
    private var sectorCount: Int = 0

    @Volatile
    var currentSectorIndex: Int = 0
        private set

    fun configure(sectors: List<Sector>) {
        sectorCount = sectors.size
        gates = sectors.map { sector ->
            GateUtils.computeGateEndpoints(sector.gateLat, sector.gateLng, sector.gateBearing)
        }
        currentSectorIndex = 0
    }

    fun observeCrossings(): Flow<SectorCrossing> = flow {
        if (gates.isEmpty()) return@flow

        var prevLat: Double? = null
        var prevLng: Double? = null
        var prevTime: Long? = null

        gpsTracker.currentLocation.collect { point ->
            if (point == null) return@collect
            if (currentSectorIndex >= sectorCount) return@collect

            val curLat = point.latitude
            val curLng = point.longitude
            val curTime = point.timestamp

            if (prevLat != null && prevLng != null && prevTime != null) {
                val gate = gates[currentSectorIndex]
                val intersects = GeoUtils.segmentsIntersect(
                    prevLat!!, prevLng!!,
                    curLat, curLng,
                    gate.lat1, gate.lng1,
                    gate.lat2, gate.lng2
                )

                if (intersects) {
                    val crossingTime = GeoUtils.interpolateCrossingTime(
                        prevTime!!, prevLat!!, prevLng!!,
                        curTime, curLat, curLng,
                        (gate.lat1 + gate.lat2) / 2.0, (gate.lng1 + gate.lng2) / 2.0
                    )

                    emit(SectorCrossing(crossingTime, currentSectorIndex))
                    currentSectorIndex++
                }
            }

            prevLat = curLat
            prevLng = curLng
            prevTime = curTime
        }
    }

    fun resetToSector(index: Int) {
        currentSectorIndex = index
    }
}
