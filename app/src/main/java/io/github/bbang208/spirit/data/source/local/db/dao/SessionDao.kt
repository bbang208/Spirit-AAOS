package io.github.bbang208.spirit.data.source.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.bbang208.spirit.data.source.local.db.entity.SessionEntity
import io.github.bbang208.spirit.data.source.local.db.entity.SessionWithTrack

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY start_time DESC")
    fun getAllSessions(): LiveData<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: String): LiveData<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionByIdSync(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE track_id = :trackId ORDER BY start_time DESC")
    fun getSessionsByTrack(trackId: String): LiveData<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY start_time DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): LiveData<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY start_time DESC LIMIT :limit")
    fun getRecentSessionsWithTrack(limit: Int): LiveData<List<SessionWithTrack>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionWithTrackById(sessionId: String): LiveData<SessionWithTrack?>
}
