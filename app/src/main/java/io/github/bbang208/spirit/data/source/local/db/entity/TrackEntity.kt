package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val grade: String,
    @ColumnInfo(name = "start_line_lat")
    val startLineLat: Double,
    @ColumnInfo(name = "start_line_lng")
    val startLineLng: Double,
    @ColumnInfo(name = "start_line_bearing")
    val startLineBearing: Float,
    @ColumnInfo(name = "length_meters")
    val lengthMeters: Float,
    @ColumnInfo(name = "outline_json")
    val outlineJson: String,
    @ColumnInfo(name = "sectors_json")
    val sectorsJson: String,
    @ColumnInfo(name = "is_local")
    val isLocal: Boolean,
    @ColumnInfo(name = "is_preset")
    val isPreset: Boolean,
    @ColumnInfo(name = "remote_id")
    val remoteId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
