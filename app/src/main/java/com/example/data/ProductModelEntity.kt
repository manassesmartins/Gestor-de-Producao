package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_models")
data class ProductModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double = 0.0,
    val sizePrices: String = ""
) {
    fun getSizePrice(size: String): Double {
        if (sizePrices.isBlank()) return price
        return try {
            val map = sizePrices.split(";").associate {
                val parts = it.split(":")
                parts[0].trim() to parts[1].trim().toDouble()
            }
            map[size] ?: price
        } catch (e: Exception) {
            price
        }
    }

    companion object {
        val SIZES = listOf("P", "M", "G", "GG", "U")

        fun buildSizePrices(prices: Map<String, Double>): String {
            return prices.entries.joinToString(";") { "${it.key}:${it.value}" }
        }
    }
}
