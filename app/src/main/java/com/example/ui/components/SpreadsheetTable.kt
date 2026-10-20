package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PieceEntryEntity
import com.example.ui.DailyWorkGroup
import com.example.ui.IndexedPieceEntry
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TableHeaderBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ZebraEven
import com.example.ui.theme.ZebraOdd
import java.util.Locale

// Fixed pixel-perfect column widths in exact sequence:
private val COL_SR_WIDTH = 55.dp
private val COL_PIECE_WIDTH = 95.dp
private val COL_COLOR_WIDTH = 115.dp
private val COL_NAME_WIDTH = 165.dp
private val COL_SIZE_WIDTH = 85.dp
private val COL_PRICE_WIDTH = 115.dp
private val COL_DATE_WIDTH = 110.dp
private val COL_TOTAL_WIDTH = 125.dp
private val COL_ACTIONS_WIDTH = 110.dp

val TOTAL_TABLE_WIDTH = COL_SR_WIDTH + COL_PIECE_WIDTH + COL_COLOR_WIDTH +
        COL_NAME_WIDTH + COL_SIZE_WIDTH + COL_PRICE_WIDTH +
        COL_DATE_WIDTH + COL_TOTAL_WIDTH + COL_ACTIONS_WIDTH

@Composable
fun SpreadsheetLedger(
    dailyGroups: List<DailyWorkGroup>,
    onEditEntry: (PieceEntryEntity) -> Unit,
    onDeleteEntry: (PieceEntryEntity) -> Unit,
    onDuplicateEntry: (PieceEntryEntity) -> Unit,
    onSeedData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    if (dailyGroups.isEmpty()) {
        EmptyLedgerView(modifier = modifier)
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Horizontal Scrollable Container wrapping the complete spreadsheet table
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .width(TOTAL_TABLE_WIDTH)
        ) {
            // Horizontal swipe guide banner
            Row(
                modifier = Modifier
                    .width(TOTAL_TABLE_WIDTH)
                    .background(Color(0xFF131922))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 WORK LEDGER REGISTER (Swip left/right 👈 👉 to view all 9 columns & Actions)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan,
                    letterSpacing = 0.5.sp
                )
            }

            // Sticky Table Header Row (in exact specified column sequence)
            SpreadsheetHeaderRow()

            // Vertical list containing day groups and entry rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("spreadsheet_lazy_list")
            ) {
                dailyGroups.forEach { group ->
                    // Daily Grouping Sub-header banner
                    item(key = "header_${group.date}") {
                        DailyGroupBanner(group = group)
                    }

                    // Individual Entry Rows in exact field sequence
                    items(
                        items = group.items,
                        key = { it.entry.id }
                    ) { item ->
                        SpreadsheetDataRow(
                            item = item,
                            onEdit = { onEditEntry(item.entry) },
                            onDelete = { onDeleteEntry(item.entry) },
                            onDuplicate = { onDuplicateEntry(item.entry) }
                        )
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

/**
 * Table Header with columns in strict required order:
 * 1. # (Sr. No.)
 * 2. Piece (Qty)
 * 3. Color
 * 4. Piece Name
 * 5. Size
 * 6. Price (Rate ₹)
 * 7. Date
 * 8. Total (₹)
 * 9. Actions
 */
@Composable
private fun SpreadsheetHeaderRow() {
    Row(
        modifier = Modifier
            .width(TOTAL_TABLE_WIDTH)
            .background(TableHeaderBg)
            .border(width = 1.dp, color = BorderDark)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCellHeader(text = "#", width = COL_SR_WIDTH, textAlign = TextAlign.Center)
        TableCellHeader(text = "Piece (Qty)", width = COL_PIECE_WIDTH, textAlign = TextAlign.End)
        TableCellHeader(text = "Color", width = COL_COLOR_WIDTH, textAlign = TextAlign.Start)
        TableCellHeader(text = "Piece Name", width = COL_NAME_WIDTH, textAlign = TextAlign.Start)
        TableCellHeader(text = "Size", width = COL_SIZE_WIDTH, textAlign = TextAlign.Center)
        TableCellHeader(text = "Price (Rate ₹)", width = COL_PRICE_WIDTH, textAlign = TextAlign.End)
        TableCellHeader(text = "Date", width = COL_DATE_WIDTH, textAlign = TextAlign.Center)
        TableCellHeader(text = "Total (₹)", width = COL_TOTAL_WIDTH, textAlign = TextAlign.End)
        TableCellHeader(text = "Actions", width = COL_ACTIONS_WIDTH, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TableCellHeader(
    text: String,
    width: Dp,
    textAlign: TextAlign
) {
    Text(
        text = text.uppercase(),
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        maxLines = 1,
        letterSpacing = 0.5.sp
    )
}

/**
 * Daily Grouping Sub-header displaying the Date, Subtotal Pieces, and Subtotal Earnings
 */
@Composable
private fun DailyGroupBanner(group: DailyWorkGroup) {
    Row(
        modifier = Modifier
            .width(TOTAL_TABLE_WIDTH)
            .background(Color(0xFF131922))
            .border(width = 1.dp, color = Color(0xFF263242))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = group.formattedDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF1C2736), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${group.items.size} entries",
                    fontSize = 10.sp,
                    color = AccentCyan
                )
            }
        }

        // Subtotals on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Day Pieces: ",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${group.subtotalPieces} pcs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = AccentGold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Day Earnings: ",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", group.subtotalEarnings)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonGreen
                )
            }
        }
    }
}

/**
 * Exactly one horizontal row per record, strictly adhering to the 9-column sequence:
 * 1. # (Sr. No.)
 * 2. Piece (Qty)
 * 3. Color
 * 4. Piece Name
 * 5. Size
 * 6. Price (Rate ₹)
 * 7. Date
 * 8. Total (₹)
 * 9. Actions
 */
@Composable
private fun SpreadsheetDataRow(
    item: IndexedPieceEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val entry = item.entry
    val isEven = item.srNo % 2 == 0
    val rowBg = if (isEven) ZebraEven else ZebraOdd

    Row(
        modifier = Modifier
            .width(TOTAL_TABLE_WIDTH)
            .background(rowBg)
            .border(width = 0.5.dp, color = BorderDark)
            .padding(vertical = 6.dp)
            .testTag("row_entry_${entry.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. # (Sr. No.)
        Text(
            text = "${item.srNo}",
            modifier = Modifier
                .width(COL_SR_WIDTH)
                .padding(horizontal = 8.dp),
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )

        // 2. Piece (Qty)
        Text(
            text = "${entry.piece}",
            modifier = Modifier
                .width(COL_PIECE_WIDTH)
                .padding(horizontal = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            textAlign = TextAlign.End
        )

        // 3. Color
        Box(
            modifier = Modifier
                .width(COL_COLOR_WIDTH)
                .padding(horizontal = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(getColorIndicator(entry.color), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.color,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 4. Piece Name
        Text(
            text = entry.pieceName,
            modifier = Modifier
                .width(COL_NAME_WIDTH)
                .padding(horizontal = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 5. Size (Badge)
        Box(
            modifier = Modifier
                .width(COL_SIZE_WIDTH)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF2B2510), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF6B5817), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = entry.size,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    maxLines = 1
                )
            }
        }

        // 6. Price (Rate per piece ₹)
        Text(
            text = "₹${String.format(Locale.US, "%.2f", entry.price)}",
            modifier = Modifier
                .width(COL_PRICE_WIDTH)
                .padding(horizontal = 8.dp),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary,
            textAlign = TextAlign.End
        )

        // 7. Date
        Text(
            text = entry.date,
            modifier = Modifier
                .width(COL_DATE_WIDTH)
                .padding(horizontal = 8.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        // 8. Total (₹) [Auto-calculated: Piece × Price] with bright neon green badge
        Box(
            modifier = Modifier
                .width(COL_TOTAL_WIDTH)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF00381B), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF008037), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", entry.total)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonGreen,
                    maxLines = 1
                )
            }
        }

        // 9. Actions (Inline Edit & Delete & Duplicate)
        Row(
            modifier = Modifier
                .width(COL_ACTIONS_WIDTH)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Edit
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Row",
                    tint = NeonCyan,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Duplicate
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate Row",
                    tint = AccentBlue,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Row",
                    tint = AccentRed,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Returns a tasteful color representation for common tailoring fabric color names
 */
private fun getColorIndicator(colorName: String): Color {
    val lower = colorName.trim().lowercase()
    return when {
        lower.contains("navy") || lower.contains("blue") -> Color(0xFF388BFD)
        lower.contains("black") -> Color(0xFF484F58)
        lower.contains("white") -> Color(0xFFF0F6FC)
        lower.contains("red") || lower.contains("crimson") -> Color(0xFFF85149)
        lower.contains("maroon") -> Color(0xFF8B1E3F)
        lower.contains("olive") || lower.contains("green") -> Color(0xFF3FB950)
        lower.contains("yellow") || lower.contains("gold") -> Color(0xFFD29922)
        lower.contains("grey") || lower.contains("gray") -> Color(0xFF8B949E)
        lower.contains("pink") -> Color(0xFFF778BA)
        lower.contains("beige") || lower.contains("cream") -> Color(0xFFD4C5A9)
        lower.contains("purple") -> Color(0xFFA371F7)
        else -> NeonCyan
    }
}

@Composable
private fun EmptyLedgerView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(SurfaceDarkVariant, RoundedCornerShape(12.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Abhi koi entry nahi hai",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Upar details bharkar 'Add Entry' dabayein.\nAapka saara piece-rate hisaab yahan table mein aayega.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
