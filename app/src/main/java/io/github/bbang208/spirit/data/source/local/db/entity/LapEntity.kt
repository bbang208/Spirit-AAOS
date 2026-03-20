package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "laps",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("session_id")]
)
data class LapEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "lap_index")
    val lapIndex: Int,
    @ColumnInfo(name = "lap_time_ms")
    val lapTimeMs: Long,
    @ColumnInfo(name = "is_personal_best")
    val isPersonalBest: Boolean,
    @ColumnInfo(name = "delta_to_best")
    val deltaToBest: Long?
)
