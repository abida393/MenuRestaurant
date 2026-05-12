package com.savoria.app.data.local.converter

import androidx.room.TypeConverter
import com.savoria.app.data.local.entity.OrderItemStatus
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.entity.ReservationStatus
import com.savoria.app.data.local.entity.ShiftType
import com.savoria.app.data.local.entity.TableStatus
import com.savoria.app.data.local.entity.UserRole
import java.util.Date
import java.util.UUID

class Converters {

    // UUID Converters
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? {
        return uuidString?.let { UUID.fromString(it) }
    }

    // Date Converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Enums Converters
    @TypeConverter
    fun toUserRole(value: String) = enumValueOf<UserRole>(value)

    @TypeConverter
    fun fromUserRole(value: UserRole) = value.name

    @TypeConverter
    fun toShiftType(value: String) = enumValueOf<ShiftType>(value)

    @TypeConverter
    fun fromShiftType(value: ShiftType) = value.name

    @TypeConverter
    fun toOrderStatus(value: String) = enumValueOf<OrderStatus>(value)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus) = value.name

    @TypeConverter
    fun toOrderItemStatus(value: String) = enumValueOf<OrderItemStatus>(value)

    @TypeConverter
    fun fromOrderItemStatus(value: OrderItemStatus) = value.name

    @TypeConverter
    fun toTableStatus(value: String) = enumValueOf<TableStatus>(value)

    @TypeConverter
    fun fromTableStatus(value: TableStatus) = value.name

    @TypeConverter
    fun toReservationStatus(value: String) = enumValueOf<ReservationStatus>(value)

    @TypeConverter
    fun fromReservationStatus(value: ReservationStatus) = value.name
}
