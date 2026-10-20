package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.DashboardHeader
import com.example.ui.components.DateRangeDialog
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.RapidInputBar
import com.example.ui.components.SpreadsheetLedger
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BgDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.FileOutputStream

@Composable
fun PieceRateApp(
    viewModel: PieceRateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI state
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val dailyGroups by viewModel.groupedDailyWork.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val customStart by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEnd by viewModel.customEndDate.collectAsStateWithLifecycle()

    // Input Bar states
    val pieceInput by viewModel.pieceInput.collectAsStateWithLifecycle()
    val colorInput by viewModel.colorInput.collectAsStateWithLifecycle()
    val pieceNameInput by viewModel.pieceNameInput.collectAsStateWithLifecycle()
    val sizeInput by viewModel.sizeInput.collectAsStateWithLifecycle()
    val priceInput by viewModel.priceInput.collectAsStateWithLifecycle()
    val dateInput by viewModel.dateInput.collectAsStateWithLifecycle()
    val inputError by viewModel.inputError.collectAsStateWithLifecycle()

    val distinctNames by viewModel.distinctPieceNames.collectAsStateWithLifecycle()
    val distinctColors by viewModel.distinctColors.collectAsStateWithLifecycle()

    // Dialog states
    val editingEntry by viewModel.editingEntry.collectAsStateWithLifecycle()
    val deletingEntry by viewModel.deletingEntry.collectAsStateWithLifecycle()
    val showBackupDialog by viewModel.showBackupDialog.collectAsStateWithLifecycle()
    val showDateRangeDialog by viewModel.showDateRangeDialog.collectAsStateWithLifecycle()

    // Listen for toast/snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Fast CSV direct share helper
    fun shareCsvDirect() {
        val csv = viewModel.getCsvContent()
        try {
            val cacheFile = File(context.cacheDir, "piecerate_ledger.csv")
            FileOutputStream(cacheFile).use { it.write(csv.toByteArray()) }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "PieceRate Tailoring Ledger CSV")
                putExtra(Intent.EXTRA_TEXT, csv)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export / Share CSV"))
        } catch (_: Exception) {
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "PieceRate Tailoring Ledger CSV")
                putExtra(Intent.EXTRA_TEXT, csv)
            }
            context.startActivity(Intent.createChooser(textIntent, "Export / Share CSV"))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
        ) {
            // 1. Dashboard Metrics, Filters & Quick Actions Header
            DashboardHeader(
                metrics = metrics,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                selectedFilter = dateFilter,
                onFilterSelect = { viewModel.dateFilter.value = it },
                customDateRangeText = if (customStart.isNotEmpty()) "$customStart to $customEnd" else "",
                onOpenCustomDateRange = { viewModel.showDateRangeDialog.value = true },
                onExportCsv = { shareCsvDirect() },
                onOpenBackupRestore = { viewModel.showBackupDialog.value = true }
            )

            // 2. Sticky Rapid Single-Line Input Bar across the top
            RapidInputBar(
                pieceInput = pieceInput,
                onPieceChange = { viewModel.onPieceChange(it) },
                colorInput = colorInput,
                onColorChange = { viewModel.onColorChange(it) },
                pieceNameInput = pieceNameInput,
                onPieceNameChange = { viewModel.onPieceNameChange(it) },
                sizeInput = sizeInput,
                onSizeChange = { viewModel.onSizeChange(it) },
                priceInput = priceInput,
                onPriceChange = { viewModel.onPriceChange(it) },
                dateInput = dateInput,
                onDateChange = { viewModel.onDateChange(it) },
                inputError = inputError,
                distinctPieceNames = distinctNames,
                distinctColors = distinctColors,
                onSubmit = { viewModel.submitEntry() }
            )

            // 3. Compact Spreadsheet Ledger Table with daily grouping & horizontal scroll
            SpreadsheetLedger(
                dailyGroups = dailyGroups,
                onEditEntry = { viewModel.editingEntry.value = it },
                onDeleteEntry = { viewModel.deletingEntry.value = it },
                onDuplicateEntry = { viewModel.duplicateEntry(it) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Modal Edit Entry Dialog
    editingEntry?.let { entryToEdit ->
        EditEntryDialog(
            entry = entryToEdit,
            onDismiss = { viewModel.editingEntry.value = null },
            onSave = { updated ->
                viewModel.updateEntry(updated)
                viewModel.editingEntry.value = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingEntry?.let { entryToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.deletingEntry.value = null },
            title = { Text("Delete Entry?", color = AccentRed) },
            text = {
                Text(
                    text = "Delete record #${entryToDelete.id} (${entryToDelete.piece} pcs ${entryToDelete.pieceName} - ₹${entryToDelete.total})?",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEntry(entryToDelete)
                        viewModel.deletingEntry.value = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.deletingEntry.value = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            onDismiss = { viewModel.showBackupDialog.value = false },
            getCsvContent = { viewModel.getCsvContent() },
            getJsonContent = { viewModel.getJsonContent() },
            onImportJson = { json, cb -> viewModel.importJson(json, cb) },
            onSeedData = { viewModel.seedSampleData() },
            onClearAll = { viewModel.clearAllEntries() }
        )
    }

    // Custom Date Range Dialog
    if (showDateRangeDialog) {
        DateRangeDialog(
            initialStart = customStart,
            initialEnd = customEnd,
            onDismiss = { viewModel.showDateRangeDialog.value = false },
            onApply = { start, end ->
                viewModel.customStartDate.value = start
                viewModel.customEndDate.value = end
                viewModel.dateFilter.value = DateFilter.CUSTOM_RANGE
            }
        )
    }
}
