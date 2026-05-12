package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val numero: Int,
    val capacite: Int,
    val statut: TableStatus
)
