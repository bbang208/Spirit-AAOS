package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("track_id")]
)
data class SessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "track_id")
    val trackId: String,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long?,
    @ColumnInfo(name = "total_laps")
    val totalLaps: Int,
    @ColumnInfo(name = "best_lap_time_ms")
    val bestLapTimeMs: Long?,
    val status: String,
    @ColumnInfo(name = "is_uploaded")
    val isUploaded: Boolean
)
