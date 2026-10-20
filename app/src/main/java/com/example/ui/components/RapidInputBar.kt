package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val SIZES_LIST = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "Free Size", "Custom")
private val QUICK_ITEMS = listOf("Kurti", "8-Kali Kurti", "Pant", "Shirt", "Blouse", "Palazzo")

@Composable
fun RapidInputBar(
    pieceInput: String,
    onPieceChange: (String) -> Unit,
    colorInput: String,
    onColorChange: (String) -> Unit,
    pieceNameInput: String,
    onPieceNameChange: (String) -> Unit,
    sizeInput: String,
    onSizeChange: (String) -> Unit,
    priceInput: String,
    onPriceChange: (String) -> Unit,
    dateInput: String,
    onDateChange: (String) -> Unit,
    inputError: String?,
    distinctPieceNames: List<String>,
    distinctColors: List<String>,
    onSubmit: () -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val pieceFocusRequester = remember { FocusRequester() }

    val qtyInt = pieceInput.toIntOrNull() ?: 0
    val priceDouble = priceInput.toDoubleOrNull() ?: 0.0
    val liveTotal = qtyInt * priceDouble

    var sizeDropdownExpanded by remember { mutableStateOf(false) }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = sdf.parse(dateInput)
            if (parsed != null) calendar.time = parsed
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateChange(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun handleCommit() {
        val success = onSubmit()
        if (success) {
            try {
                pieceFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF10151E))
            .border(width = 1.dp, color = BorderDark)
            .padding(10.dp)
    ) {
        // Simple Title & Quick items
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "➕ NAYI ENTRY KAREIN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )

            // Quick Garment Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QUICK_ITEMS.take(4).forEach { item ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (pieceNameInput == item) Color(0xFF00381B) else SurfaceDarkVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (pieceNameInput == item) NeonGreen else BorderDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onPieceNameChange(item) }
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item,
                            fontSize = 10.sp,
                            color = if (pieceNameInput == item) NeonGreen else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: 1. Piece (Qty) & 2. Color
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Piece (Qty)
            Column(modifier = Modifier.weight(1f)) {
                Text("1. Piece (Qty)", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = pieceInput,
                    onValueChange = onPieceChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(pieceFocusRequester)
                        .testTag("input_piece_qty"),
                    placeholder = { Text("Kitne piece", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // 2. Color
            Column(modifier = Modifier.weight(1f)) {
                Text("2. Color", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = colorInput,
                    onValueChange = onColorChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_color"),
                    placeholder = { Text("Rang (e.g. Black)", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: 3. Piece Name & 4. Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3. Piece Name
            Column(modifier = Modifier.weight(1.3f)) {
                Text("3. Piece Name", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = pieceNameInput,
                    onValueChange = onPieceNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_piece_name"),
                    placeholder = { Text("Item (e.g. Kurti)", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // 4. Size
            Column(modifier = Modifier.weight(0.9f)) {
                Text("4. Size", fontSize = 11.sp, color = AccentGold, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(SurfaceDarkVariant, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                            .clickable { sizeDropdownExpanded = true }
                            .padding(horizontal = 10.dp)
                            .testTag("input_size_dropdown"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = sizeInput, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
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
                                    onSizeChange(s)
                                    sizeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 3: 5. Price (Rate ₹) & 6. Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 5. Price (Rate ₹)
            Column(modifier = Modifier.weight(1f)) {
                Text("5. Price (Rate ₹)", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = onPriceChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_price"),
                    placeholder = { Text("Rate per piece", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleCommit() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = BorderDark
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // 6. Date
            Column(modifier = Modifier.weight(1f)) {
                Text("6. Date", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(SurfaceDarkVariant, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                        .clickable { showDatePicker() }
                        .padding(horizontal = 10.dp)
                        .testTag("input_date_btn"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = dateInput, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 4: 7. Live Total & Clear Add Entry Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Total Indicator
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .background(Color(0xFF0B1910), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF1B4D2A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("7. Total:", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", liveTotal)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonGreen
                    )
                }
            }

            // Add Entry Button
            Button(
                onClick = { handleCommit() },
                modifier = Modifier
                    .weight(1.2f)
                    .height(46.dp)
                    .testTag("btn_add_entry"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF238636),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Error message if any
        AnimatedVisibility(visible = inputError != null) {
            Text(
                text = inputError ?: "",
                color = AccentRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}
