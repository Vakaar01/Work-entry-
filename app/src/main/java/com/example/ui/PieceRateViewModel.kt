package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PieceEntryEntity
import com.example.data.repository.PieceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IndexedPieceEntry(
    val srNo: Int,
    val entry: PieceEntryEntity
)

data class DailyWorkGroup(
    val date: String,
    val formattedDate: String,
    val subtotalPieces: Int,
    val subtotalEarnings: Double,
    val items: List<IndexedPieceEntry>
)

data class DashboardMetrics(
    val todayPieces: Int = 0,
    val todayEarnings: Double = 0.0,
    val grandTotalPieces: Int = 0,
    val grandTotalEarnings: Double = 0.0,
    val totalRecordsCount: Int = 0
)

class PieceRateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PieceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PieceRepository(db.pieceEntryDao())
        viewModelScope.launch {
            val prefs = application.getSharedPreferences("piecerate_prefs", android.content.Context.MODE_PRIVATE)
            if (!prefs.getBoolean("clean_zero_entries_v3", false)) {
                repository.clearAll()
                prefs.edit().putBoolean("clean_zero_entries_v3", true).apply()
            }
        }
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val dateFilter = MutableStateFlow(DateFilter.ALL_TIME)
    val customStartDate = MutableStateFlow("")
    val customEndDate = MutableStateFlow("")

    // Rapid Input Bar states
    val pieceInput = MutableStateFlow("")
    val colorInput = MutableStateFlow("")
    val pieceNameInput = MutableStateFlow("")
    val sizeInput = MutableStateFlow("L")
    val priceInput = MutableStateFlow("")
    val dateInput = MutableStateFlow(DateUtils.getTodayString())
    val inputError = MutableStateFlow<String?>(null)

    // Autocomplete / Suggestions
    val distinctPieceNames: StateFlow<List<String>> = repository.distinctPieceNames
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val distinctColors: StateFlow<List<String>> = repository.distinctColors
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Raw entries
    val allEntries: StateFlow<List<PieceEntryEntity>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered entries
    val filteredEntries: StateFlow<List<PieceEntryEntity>> = combine(
        allEntries,
        searchQuery,
        dateFilter,
        customStartDate,
        customEndDate
    ) { entries, query, filter, start, end ->
        val q = query.trim().lowercase()
        entries.filter { entry ->
            val matchesSearch = q.isEmpty() ||
                entry.pieceName.lowercase().contains(q) ||
                entry.color.lowercase().contains(q) ||
                entry.size.lowercase().contains(q)

            val matchesDate = DateUtils.isWithinRange(entry.date, filter, start, end)
            matchesSearch && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allEntries,
        filteredEntries
    ) { all, filtered ->
        val todayStr = DateUtils.getTodayString()
        val todayEntries = all.filter { it.date == todayStr }
        val todayPieces = todayEntries.sumOf { it.piece }
        val todayEarnings = todayEntries.sumOf { it.total }

        val grandTotalPieces = filtered.sumOf { it.piece }
        val grandTotalEarnings = filtered.sumOf { it.total }

        DashboardMetrics(
            todayPieces = todayPieces,
            todayEarnings = todayEarnings,
            grandTotalPieces = grandTotalPieces,
            grandTotalEarnings = grandTotalEarnings,
            totalRecordsCount = filtered.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Grouped by day
    val groupedDailyWork: StateFlow<List<DailyWorkGroup>> = filteredEntries.combine(
        MutableStateFlow(Unit)
    ) { entries, _ ->
        val groupsMap = LinkedHashMap<String, MutableList<IndexedPieceEntry>>()
        var srNo = 1
        for (item in entries) {
            val list = groupsMap.getOrPut(item.date) { mutableListOf() }
            list.add(IndexedPieceEntry(srNo++, item))
        }

        groupsMap.map { (date, items) ->
            DailyWorkGroup(
                date = date,
                formattedDate = DateUtils.formatDisplayDate(date),
                subtotalPieces = items.sumOf { it.entry.piece },
                subtotalEarnings = items.sumOf { it.entry.total },
                items = items
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dialog states
    val editingEntry = MutableStateFlow<PieceEntryEntity?>(null)
    val deletingEntry = MutableStateFlow<PieceEntryEntity?>(null)
    val showBackupDialog = MutableStateFlow(false)
    val showDateRangeDialog = MutableStateFlow(false)

    fun onPieceChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        pieceInput.value = filtered
        inputError.value = null
    }

    fun onColorChange(value: String) {
        colorInput.value = value
        inputError.value = null
    }

    fun onPieceNameChange(value: String) {
        pieceNameInput.value = value
        inputError.value = null
    }

    fun onSizeChange(value: String) {
        sizeInput.value = value
    }

    fun onPriceChange(value: String) {
        // allow digits and at most one decimal point
        val clean = value.filter { it.isDigit() || it == '.' }
        if (clean.count { it == '.' } <= 1) {
            priceInput.value = clean
        }
        inputError.value = null
    }

    fun onDateChange(value: String) {
        dateInput.value = value
    }

    /**
     * Submits the entry from the top input bar.
     * Returns true if successfully inserted, false otherwise.
     */
    fun submitEntry(): Boolean {
        val qty = pieceInput.value.toIntOrNull()
        if (qty == null || qty <= 0) {
            inputError.value = "Enter valid piece quantity (> 0)"
            return false
        }
        val name = pieceNameInput.value.trim()
        if (name.isEmpty()) {
            inputError.value = "Piece Name is required"
            return false
        }
        val price = priceInput.value.toDoubleOrNull()
        if (price == null || price < 0.0) {
            inputError.value = "Enter valid rate per piece (₹)"
            return false
        }
        val date = dateInput.value.trim().ifEmpty { DateUtils.getTodayString() }
        val color = colorInput.value.trim().ifEmpty { "-" }
        val size = sizeInput.value.trim().ifEmpty { "Free Size" }

        viewModelScope.launch {
            repository.insert(
                PieceEntryEntity(
                    piece = qty,
                    color = color,
                    pieceName = name,
                    size = size,
                    price = price,
                    date = date,
                    createdAt = System.currentTimeMillis()
                )
            )
            _userMessage.emit("Added $qty pcs $name (₹${String.format(java.util.Locale.US, "%.2f", qty * price)})")
        }

        // Reset inputs while keeping current selected date for continuous rapid logging
        pieceInput.value = ""
        colorInput.value = ""
        pieceNameInput.value = ""
        priceInput.value = ""
        inputError.value = null
        return true
    }

    fun deleteEntry(entry: PieceEntryEntity) {
        viewModelScope.launch {
            repository.delete(entry)
            _userMessage.emit("Deleted row #${entry.id}")
        }
    }

    fun updateEntry(updated: PieceEntryEntity) {
        viewModelScope.launch {
            repository.update(updated)
            _userMessage.emit("Updated ${updated.pieceName}")
        }
    }

    fun duplicateEntry(entry: PieceEntryEntity) {
        viewModelScope.launch {
            repository.insert(
                entry.copy(
                    id = 0,
                    date = DateUtils.getTodayString(),
                    createdAt = System.currentTimeMillis()
                )
            )
            _userMessage.emit("Duplicated ${entry.pieceName} to today's log")
        }
    }

    fun clearAllEntries() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.emit("All ledger records cleared")
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedSampleDataIfEmpty()
            _userMessage.emit("Sample garment tailoring data seeded")
        }
    }

    fun getCsvContent(): String {
        return repository.exportToCsv(filteredEntries.value.ifEmpty { allEntries.value })
    }

    fun getJsonContent(): String {
        return repository.exportToJson(allEntries.value)
    }

    fun importJson(json: String, onComplete: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = repository.importFromJson(json)
                _userMessage.emit("Successfully imported $count entries")
                onComplete(true, count)
            } catch (e: Exception) {
                _userMessage.emit("Import failed: ${e.message}")
                onComplete(false, 0)
            }
        }
    }
}
