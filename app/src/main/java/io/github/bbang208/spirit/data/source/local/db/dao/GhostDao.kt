package io.github.bbang208.spirit.data.source.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.bbang208.spirit.data.source.local.db.entity.GhostRunEntity

@Dao
interface GhostDao {

    @Query("SELECT * FROM ghost_runs WHERE track_id = :trackId ORDER BY lap_time_ms ASC")
    fun getGhostsByTrack(trackId: String): LiveData<List<GhostRunEntity>>

    @Query("SELECT * FROM ghost_runs WHERE id = :ghostId")
    suspend fun getGhostById(ghostId: String): GhostRunEntity?

    @Query("SELECT * FROM ghost_runs WHERE track_id = :trackId ORDER BY lap_time_ms ASC LIMIT 1")
    suspend fun getFastestGhost(trackId: String): GhostRunEntity?

    @Query("SELECT * FROM ghost_runs WHERE track_id = :trackId AND user_id = :userId ORDER BY lap_time_ms ASC LIMIT 1")
    suspend fun getUserGhost(trackId: String, userId: String): GhostRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ghost: GhostRunEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ghosts: List<GhostRunEntity>)

    @Query("DELETE FROM ghost_runs WHERE id = :ghostId")
    suspend fun deleteById(ghostId: String)
}
