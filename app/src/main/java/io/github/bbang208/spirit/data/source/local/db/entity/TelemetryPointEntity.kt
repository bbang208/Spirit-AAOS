package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry_points",
    foreignKeys = [
        ForeignKey(
            entity = LapEntity::class,
            parentColumns = ["id"],
            childColumns = ["lap_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lap_id")]
)
data class TelemetryPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "lap_id")
    val lapId: String,
    val timestamp: Long,
    @ColumnInfo(name = "speed_kmh")
    val speedKmh: Float,
    @ColumnInfo(name = "lateral_g")
    val lateralG: Float,
    @ColumnInfo(name = "longitudinal_g")
    val longitudinalG: Float,
    val latitude: Double,
    val longitude: Double
)
