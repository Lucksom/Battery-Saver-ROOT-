package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TweakConfigDao {
    @Query("SELECT * FROM tweak_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<TweakConfigEntity?>

    @Query("SELECT * FROM tweak_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): TweakConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: TweakConfigEntity)

    @Update
    suspend fun update(config: TweakConfigEntity)

    @Query("UPDATE tweak_config SET isMasterApplied = :applied, lastAppliedTimestamp = :timestamp WHERE id = 1")
    suspend fun updateAppliedStatus(applied: Boolean, timestamp: Long)

    @Query("UPDATE tweak_config SET activeProfile = :profile WHERE id = 1")
    suspend fun updateProfile(profile: String)

    @Query("UPDATE tweak_config SET performanceSubMode = :subMode WHERE id = 1")
    suspend fun updatePerformanceSubMode(subMode: String)

    @Query("UPDATE tweak_config SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: String)

    @Query("UPDATE tweak_config SET backupCreated = :created, backupFilePath = :path WHERE id = 1")
    suspend fun updateBackupStatus(created: Boolean, path: String)
}
