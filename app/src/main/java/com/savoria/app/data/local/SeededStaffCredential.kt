package com.savoria.app.data.local

import com.savoria.app.data.local.entity.UserRole

/** One-time plaintext credential shown to the admin after secure account seeding. */
data class SeededStaffCredential(
    val email: String,
    val role: UserRole,
    val displayName: String,
    val plainPassword: String
)
