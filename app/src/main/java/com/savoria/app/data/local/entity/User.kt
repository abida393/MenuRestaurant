package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val actif: Boolean
)
