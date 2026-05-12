package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reservations",
    foreignKeys = [
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tableId")]
)
data class Reservation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tableId: String,
    val nomClient: String,
    val nbPersonnes: Int,
    val dateHeure: Long,
    val statut: ReservationStatus
)
