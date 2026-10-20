package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "piece_entries")
data class PieceEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val piece: Int,              // 1. Piece (Qty)
    val color: String,           // 2. Color
    val pieceName: String,       // 3. Piece Name
    val size: String,            // 4. Size
    val price: Double,           // 5. Price (Rate per Piece ₹)
    val date: String,            // 6. Date (YYYY-MM-DD)
    val createdAt: Long = System.currentTimeMillis()
) {
    // 7. Total (₹) = Piece × Price
    val total: Double
        get() = piece * price
}
