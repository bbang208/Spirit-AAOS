package io.github.bbang208.spirit.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.bbang208.spirit.data.source.local.db.converter.Converters
import io.github.bbang208.spirit.data.source.local.db.dao.GhostDao
import io.github.bbang208.spirit.data.source.local.db.dao.LapDao
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import io.github.bbang208.spirit.data.source.local.db.entity.GhostRunEntity
import io.github.bbang208.spirit.data.source.local.db.entity.GpsPointEntity
import io.github.bbang208.spirit.data.source.local.db.entity.LapEntity
import io.github.bbang208.spirit.data.source.local.db.entity.LapSectorEntity
import io.github.bbang208.spirit.data.source.local.db.entity.SessionEntity
import io.github.bbang208.spirit.data.source.local.db.entity.TelemetryPointEntity
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        SessionEntity::class,
        LapEntity::class,
        LapSectorEntity::class,
        GpsPointEntity::class,
        TelemetryPointEntity::class,
        GhostRunEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SpiritDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun sessionDao(): SessionDao
    abstract fun lapDao(): LapDao
    abstract fun ghostDao(): GhostDao
}
