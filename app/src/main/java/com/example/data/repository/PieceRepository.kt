package com.example.data.repository

import com.example.data.local.PieceEntryDao
import com.example.data.local.PieceEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PieceRepository(private val dao: PieceEntryDao) {

    val allEntries: Flow<List<PieceEntryEntity>> = dao.getAllEntries()
    val distinctPieceNames: Flow<List<String>> = dao.getDistinctPieceNames()
    val distinctColors: Flow<List<String>> = dao.getDistinctColors()

    suspend fun insert(entry: PieceEntryEntity): Long {
        return dao.insert(entry)
    }

    suspend fun update(entry: PieceEntryEntity) {
        dao.update(entry)
    }

    suspend fun delete(entry: PieceEntryEntity) {
        dao.delete(entry)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun seedSampleDataIfEmpty() {
        // App starts with zero pre-existing entries as requested by user
    }

    fun exportToCsv(entries: List<PieceEntryEntity>): String {
        val sb = StringBuilder()
        // Exact order from specification:
        // 1. # (Sr. No.)
        // 2. Piece (Qty)
        // 3. Color
        // 4. Piece Name
        // 5. Size
        // 6. Price (Rate per Piece ₹)
        // 7. Date
        // 8. Total (₹)
        sb.append("Sr No,Piece (Qty),Color,Piece Name,Size,Price (Rate INR),Date,Total (INR)\n")
        entries.forEachIndexed { index, item ->
            val escapedColor = escapeCsv(item.color)
            val escapedPieceName = escapeCsv(item.pieceName)
            val escapedSize = escapeCsv(item.size)
            sb.append("${index + 1},${item.piece},\"$escapedColor\",\"$escapedPieceName\",\"$escapedSize\",${item.price},\"${item.date}\",${String.format(Locale.US, "%.2f", item.total)}\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    fun exportToJson(entries: List<PieceEntryEntity>): String {
        val array = JSONArray()
        for (item in entries) {
            val obj = JSONObject()
            obj.put("piece", item.piece)
            obj.put("color", item.color)
            obj.put("pieceName", item.pieceName)
            obj.put("size", item.size)
            obj.put("price", item.price)
            obj.put("date", item.date)
            obj.put("total", item.total)
            obj.put("createdAt", item.createdAt)
            array.put(obj)
        }
        return array.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Int {
        val array = JSONArray(jsonString)
        val list = mutableListOf<PieceEntryEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val piece = obj.optInt("piece", 0)
            val color = obj.optString("color", "")
            val pieceName = obj.optString("pieceName", "")
            val size = obj.optString("size", "M")
            val price = obj.optDouble("price", 0.0)
            val date = obj.optString("date", "")
            val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

            if (piece > 0 && pieceName.isNotBlank() && date.isNotBlank()) {
                list.add(
                    PieceEntryEntity(
                        piece = piece,
                        color = color,
                        pieceName = pieceName,
                        size = size,
                        price = price,
                        date = date,
                        createdAt = createdAt
                    )
                )
            }
        }
        if (list.isNotEmpty()) {
            dao.insertAll(list)
        }
        return list.size
    }
}
