package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "shifts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Shift(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: ShiftType,
    val date: Long,
    val heureDebut: String,
    val heureFin: String,
    val estActif: Boolean
)
