package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "closed_months")
data class ClosedMonthEntity(
    @PrimaryKey
    val monthYear: String,
    val closedAt: Long
)
