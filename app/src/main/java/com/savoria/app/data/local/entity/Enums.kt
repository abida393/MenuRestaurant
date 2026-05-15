package com.savoria.app.data.local.entity

enum class UserRole {
    ADMIN, CHEF, SERVEUR
}

enum class ShiftType {
    JOUR, NUIT
}

/** Statut commande client / cuisine unifié. */
enum class OrderStatus {
    EN_ATTENTE,
    EN_PREPARATION,
    PRET,
    SERVI;

    companion object {
        /** Compatibilité migration anciennes valeurs Room. */
        fun fromLegacy(value: String): OrderStatus = when (value) {
            "RECUE" -> EN_ATTENTE
            "EN_CUISINE" -> EN_PREPARATION
            "PRETE" -> PRET
            "SERVIE" -> SERVI
            else -> enumValueOf(value)
        }
    }
}

enum class ConsumptionMode {
    SUR_PLACE,
    EMPORTER
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
