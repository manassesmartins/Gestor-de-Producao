package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosedMonthDao {
    @Query("SELECT * FROM closed_months ORDER BY closedAt DESC")
    fun getAllClosedMonths(): Flow<List<ClosedMonthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosedMonth(closedMonth: ClosedMonthEntity)

    @Query("DELETE FROM closed_months WHERE monthYear = :monthYear")
    suspend fun deleteClosedMonth(monthYear: String)
}
