package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_points",
    foreignKeys = [
        ForeignKey(
            entity = LapEntity::class,
            parentColumns = ["id"],
            childColumns = ["lap_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lap_id"), Index("track_id")]
)
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "lap_id")
    val lapId: String?,
    @ColumnInfo(name = "track_id")
    val trackId: String?,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float
)
