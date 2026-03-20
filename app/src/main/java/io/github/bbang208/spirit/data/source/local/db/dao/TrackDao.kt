package io.github.bbang208.spirit.data.source.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.bbang208.spirit.data.source.local.db.entity.TrackEntity

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY created_at DESC")
    fun getAllTracks(): LiveData<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackById(trackId: String): LiveData<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackByIdSync(trackId: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE is_local = 1 ORDER BY created_at DESC")
    fun getLocalTracks(): LiveData<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE is_preset = 1 ORDER BY name ASC")
    fun getPresetTracks(): LiveData<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Update
    suspend fun update(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteById(trackId: String)
}
