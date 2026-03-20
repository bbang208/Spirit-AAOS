package io.github.bbang208.spirit.data.source.local.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithTrack(
    @Embedded
    val session: SessionEntity,
    @Relation(
        parentColumn = "track_id",
        entityColumn = "id"
    )
    val track: TrackEntity
)
