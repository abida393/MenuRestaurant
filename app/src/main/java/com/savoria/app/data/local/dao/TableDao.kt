package com.savoria.app.data.local.dao

import androidx.room.*
import com.savoria.app.data.local.entity.TableEntity
import com.savoria.app.data.local.entity.TableStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM tables ORDER BY numero")
    fun getAllTables(): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE statut = :status")
    fun getTablesByStatus(status: TableStatus): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE id = :tableId")
    suspend fun getTableById(tableId: String): TableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity)

    @Update
    suspend fun updateTable(table: TableEntity)

    @Delete
    suspend fun deleteTable(table: TableEntity)
}
