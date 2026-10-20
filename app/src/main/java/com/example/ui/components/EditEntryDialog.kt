package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PieceEntryEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val SIZES_LIST = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "Free Size", "Custom")

@Composable
fun EditEntryDialog(
    entry: PieceEntryEntity,
    onDismiss: () -> Unit,
    onSave: (PieceEntryEntity) -> Unit
) {
    val context = LocalContext.current

    var pieceText by remember { mutableStateOf(entry.piece.toString()) }
    var colorText by remember { mutableStateOf(entry.color) }
    var pieceNameText by remember { mutableStateOf(entry.pieceName) }
    var sizeText by remember { mutableStateOf(entry.size) }
    var priceText by remember { mutableStateOf(entry.price.toString()) }
    var dateText by remember { mutableStateOf(entry.date) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var sizeDropdownExpanded by remember { mutableStateOf(false) }

    val qty = pieceText.toIntOrNull() ?: 0
    val rate = priceText.toDoubleOrNull() ?: 0.0
    val calculatedTotal = qty * rate

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = sdf.parse(dateText)
            if (parsed != null) calendar.time = parsed
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dateText = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Edit Piece-Rate Record",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Record #${entry.id}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Piece (Qty) & 2. Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. Piece (Qty)", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = pieceText,
                            onValueChange = {
                                pieceText = it.filter { c -> c.isDigit() }
                                errorText = null
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDarkVariant,
                                unfocusedContainerColor = SurfaceDarkVariant,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_input_piece")
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("2. Color", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = colorText,
                            onValueChange = { colorText = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDarkVariant,
                                unfocusedContainerColor = SurfaceDarkVariant,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderDark
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_input_color")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Piece Name
                Text("3. Piece Name (Item Description)", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = pieceNameText,
                    onValueChange = {
                        pieceNameText = it
                        errorText = null
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Size & 5. Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("4. Size", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(SurfaceDarkVariant, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                                    .clickable { sizeDropdownExpanded = true }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sizeText,
                                        color = AccentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = sizeDropdownExpanded,
                                onDismissRequest = { sizeDropdownExpanded = false },
                                modifier = Modifier.background(SurfaceDarkVariant)
                            ) {
                                SIZES_LIST.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            sizeText = s
                                            sizeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("5. Price (Rate ₹)", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = {
                                val clean = it.filter { c -> c.isDigit() || c == '.' }
                                if (clean.count { c -> c == '.' } <= 1) {
                                    priceText = clean
                                }
                                errorText = null
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDarkVariant,
                                unfocusedContainerColor = SurfaceDarkVariant,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderDark
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_input_price")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Date
                Text("6. Date", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(SurfaceDarkVariant, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                        .clickable { showDatePicker() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateText,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. Auto-calculated Total Highlight Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1C13), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF008037), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "7. TOTAL (₹)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "$qty pcs × ₹${String.format(Locale.US, "%.2f", rate)}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", calculatedTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonGreen
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        fontSize = 11.sp,
                        color = AccentRed
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val pieceVal = pieceText.toIntOrNull()
                            if (pieceVal == null || pieceVal <= 0) {
                                errorText = "Enter valid piece quantity"
                                return@Button
                            }
                            if (pieceNameText.isBlank()) {
                                errorText = "Piece Name is required"
                                return@Button
                            }
                            val priceVal = priceText.toDoubleOrNull()
                            if (priceVal == null || priceVal < 0) {
                                errorText = "Enter valid price rate"
                                return@Button
                            }
                            onSave(
                                entry.copy(
                                    piece = pieceVal,
                                    color = colorText.trim().ifEmpty { "-" },
                                    pieceName = pieceNameText.trim(),
                                    size = sizeText.trim().ifEmpty { "Free Size" },
                                    price = priceVal,
                                    date = dateText.trim()
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("btn_save_edit"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF238636),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
