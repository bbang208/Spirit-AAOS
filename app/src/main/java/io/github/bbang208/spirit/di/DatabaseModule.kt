package io.github.bbang208.spirit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.bbang208.spirit.data.source.local.db.SpiritDatabase
import io.github.bbang208.spirit.data.source.local.db.dao.GhostDao
import io.github.bbang208.spirit.data.source.local.db.dao.LapDao
import io.github.bbang208.spirit.data.source.local.db.dao.SessionDao
import io.github.bbang208.spirit.data.source.local.db.dao.TrackDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SpiritDatabase = Room.databaseBuilder(
        context,
        SpiritDatabase::class.java,
        "spirit_database"
    )
        .fallbackToDestructiveMigration(true)
        .build()

    @Provides
    fun provideTrackDao(db: SpiritDatabase): TrackDao = db.trackDao()

    @Provides
    fun provideSessionDao(db: SpiritDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideLapDao(db: SpiritDatabase): LapDao = db.lapDao()

    @Provides
    fun provideGhostDao(db: SpiritDatabase): GhostDao = db.ghostDao()
}
