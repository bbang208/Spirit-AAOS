package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lap_sectors",
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
data class LapSectorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "lap_id")
    val lapId: String,
    @ColumnInfo(name = "sector_index")
    val sectorIndex: Int,
    @ColumnInfo(name = "time_ms")
    val timeMs: Long,
    @ColumnInfo(name = "is_personal_best")
    val isPersonalBest: Boolean,
    @ColumnInfo(name = "delta_ms")
    val deltaMs: Long?
)
