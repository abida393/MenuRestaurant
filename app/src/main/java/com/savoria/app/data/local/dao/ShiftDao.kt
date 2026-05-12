package com.savoria.app.data.local.dao

import androidx.room.*
import com.savoria.app.data.local.entity.Shift
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE userId = :userId ORDER BY date DESC")
    fun getShiftsForUser(userId: String): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE estActif = 1")
    fun getActiveShifts(): Flow<List<Shift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift)

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)
}
