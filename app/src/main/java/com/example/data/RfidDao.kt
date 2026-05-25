package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RfidDao {
    @Query("SELECT * FROM rfid_tags ORDER BY lastScanned DESC, title ASC")
    fun getAllTags(): Flow<List<RfidTag>>

    @Query("SELECT * FROM rfid_tags WHERE uid = :uid LIMIT 1")
    suspend fun getTagByUid(uid: String): RfidTag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: RfidTag)

    @Delete
    suspend fun deleteTag(tag: RfidTag)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()
}
