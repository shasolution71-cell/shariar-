package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MemberWithWeeks
import com.example.ui.theme.MenuColors
import java.util.Calendar

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onSave: (id: String, initialPrincipal: Long, phone: String, photoUri: String, guarantor: String, startDate: Long) -> Unit
) {
    var memberId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guarantor by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val emeraldColor = MenuColors.AddMemberPrimary

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with Emerald Green styling
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MenuColors.AddMemberBg)
                            .border(1.dp, MenuColors.AddMemberBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = "সদস্য যোগ",
                            tint = emeraldColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "নতুন সদস্য",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "ছবি, গ্যারান্টার ও টাকার হিসাব",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Photo selector in center
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MemberAvatar(
                        photoUri = photoUri,
                        name = memberId,
                        size = 76.dp,
                        isEditable = true,
                        memberIdForStorage = if (memberId.isNotBlank()) memberId else "temp_new",
                        onPhotoChanged = { newPath -> photoUri = newPath }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (photoUri.isBlank()) "ছবি যোগ করুন (ঐচ্ছিক)" else "ছবি পরিবর্তন করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = emeraldColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Member ID input
                OutlinedTextField(
                    value = memberId,
                    onValueChange = {
                        memberId = it
                        errorMessage = null
                    },
                    label = { Text("Member ID (নাম বা নম্বর)") },
                    placeholder = { Text("যেমন: M-104 বা রহিম") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_id_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Member Phone Number input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    label = { Text("মোবাইল নম্বর (Phone Number)") },
                    placeholder = { Text("যেমন: 017xxxxxxxx") },
                    prefix = { Text("📞 ", fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Guarantor Name input (Optional additional option)
                OutlinedTextField(
                    value = guarantor,
                    onValueChange = {
                        guarantor = it
                        errorMessage = null
                    },
                    label = { Text("গ্যারান্টারের নাম (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: জামাল হোসেন") },
                    prefix = { Text("🛡️ ", fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guarantor_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Principal Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("কত টাকা নিয়েছে (আসল)") },
                    placeholder = { Text("যেমন: 20000") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Start Date Selection with DatePickerDialog (Calendar Picker)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(Calendar.YEAR, year)
                                    newCal.set(Calendar.MONTH, month)
                                    newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    newCal.set(Calendar.HOUR_OF_DAY, 0)
                                    newCal.set(Calendar.MINUTE, 0)
                                    newCal.set(Calendar.SECOND, 0)
                                    newCal.set(Calendar.MILLISECOND, 0)
                                    selectedDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .testTag("date_picker_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MenuColors.AddMemberBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = "তারিখ বাছাই",
                                    tint = emeraldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "টাকা দেওয়ার শুরুর তারিখ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = MemberWithWeeks.formatBanglaDateWithDay(selectedDateMillis),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = emeraldColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "বদলান 📅",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation Preview & Auto-compounding Hint Box
                val enteredAmount = amountText.toLongOrNull() ?: 0L
                val calculatedWeekly = (enteredAmount / 1000L) * 20L

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MenuColors.AddMemberBg)
                        .border(1.dp, MenuColors.AddMemberBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Hint",
                            tint = emeraldColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "প্রতি ৳১,০০০ আসলে সাপ্তাহিক লাভ ৳২০",
                                fontSize = 12.sp,
                                color = MenuColors.AddMemberText,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (enteredAmount > 0) {
                                Text(
                                    text = "সাপ্তাহিক প্রাক্কলিত লাভ: ৳$calculatedWeekly টাকা",
                                    fontSize = 12.sp,
                                    color = emeraldColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "সদস্য যোগ করার তারিখ থেকেই প্রতি ৭ দিনের পেন্ডিং সাইকেল শুরু হবে। সময় পার হলে টাকা জমা না দিলে তা স্বয়ংক্রিয়ভাবে আসলের সাথে যোগ হয়ে যাবে।",
                                fontSize = 10.5.sp,
                                color = Color(0xFF047857),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE11D48),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("cancel_button")
                    ) {
                        Text("বাতিল", color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val trimmedId = memberId.trim()
                            val amount = amountText.toLongOrNull() ?: 0L
                            if (trimmedId.isEmpty()) {
                                errorMessage = "দয়া করে Member ID প্রদান করুন"
                                return@Button
                            }
                            if (amount <= 0L) {
                                errorMessage = "আসল টাকার পরিমাণ ১ বা তার বেশি হতে হবে"
                                return@Button
                            }
                            onSave(trimmedId, amount, phone.trim(), photoUri.trim(), guarantor.trim(), selectedDateMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = emeraldColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_member_button")
                    ) {
                        Text("সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
