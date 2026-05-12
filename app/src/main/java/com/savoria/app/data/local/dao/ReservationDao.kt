package com.savoria.app.data.local.dao

import androidx.room.*
import com.savoria.app.data.local.entity.Reservation
import com.savoria.app.data.local.entity.ReservationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY dateHeure")
    fun getAllReservations(): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE tableId = :tableId")
    fun getReservationsForTable(tableId: String): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE statut = :status")
    fun getReservationsByStatus(status: ReservationStatus): Flow<List<Reservation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation)

    @Update
    suspend fun updateReservation(reservation: Reservation)

    @Delete
    suspend fun deleteReservation(reservation: Reservation)
}
