package io.github.bbang208.spirit.data.source.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.bbang208.spirit.data.source.local.db.entity.GpsPointEntity
import io.github.bbang208.spirit.data.source.local.db.entity.LapEntity
import io.github.bbang208.spirit.data.source.local.db.entity.LapSectorEntity
import io.github.bbang208.spirit.data.source.local.db.entity.TelemetryPointEntity

@Dao
interface LapDao {

    @Query("SELECT * FROM laps WHERE session_id = :sessionId ORDER BY lap_index ASC")
    fun getLapsBySession(sessionId: String): LiveData<List<LapEntity>>

    @Query("SELECT * FROM laps WHERE session_id = :sessionId ORDER BY lap_index ASC")
    suspend fun getLapsBySessionSync(sessionId: String): List<LapEntity>

    @Query("SELECT * FROM laps WHERE id = :lapId")
    suspend fun getLapById(lapId: String): LapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLap(lap: LapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLapSectors(sectors: List<LapSectorEntity>)

    @Query("SELECT * FROM lap_sectors WHERE lap_id = :lapId ORDER BY sector_index ASC")
    suspend fun getLapSectors(lapId: String): List<LapSectorEntity>

    @Insert
    suspend fun insertGpsPoints(points: List<GpsPointEntity>)

    @Query("SELECT * FROM gps_points WHERE lap_id = :lapId ORDER BY timestamp ASC")
    suspend fun getGpsPointsByLap(lapId: String): List<GpsPointEntity>

    @Query("SELECT * FROM gps_points WHERE track_id = :trackId ORDER BY timestamp ASC")
    suspend fun getGpsPointsByTrack(trackId: String): List<GpsPointEntity>

    @Insert
    suspend fun insertTelemetryPoints(points: List<TelemetryPointEntity>)

    @Query("SELECT * FROM telemetry_points WHERE lap_id = :lapId ORDER BY timestamp ASC")
    suspend fun getTelemetryPointsByLap(lapId: String): List<TelemetryPointEntity>
}
