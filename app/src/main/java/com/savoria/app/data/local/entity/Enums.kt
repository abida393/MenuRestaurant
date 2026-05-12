package com.savoria.app.data.local.entity

enum class UserRole {
    ADMIN, CHEF, SERVEUR
}

enum class ShiftType {
    JOUR, NUIT
}

enum class OrderStatus {
    RECUE, EN_CUISINE, PRETE, SERVIE
}

enum class OrderItemStatus {
    EN_ATTENTE, PREPARE, SERVI
}

enum class TableStatus {
    LIBRE, OCCUPEE, RESERVED
}

enum class ReservationStatus {
    CONFIRMEE, ANNULEE, TERMINEE
}
