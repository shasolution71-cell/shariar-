package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithWeeks
import com.example.ui.theme.MenuColors
import com.example.util.PhotoStorageUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailDialog(
    memberWithWeeks: MemberWithWeeks,
    onDismiss: () -> Unit,
    onAddWeekRecord: (memberId: String, expected: Long, received: Long) -> Unit,
    onDeleteMember: (MemberEntity) -> Unit,
    onUpdateProfile: (oldId: String, newId: String, phone: String, photoUri: String, guarantor: String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var showAddWeekForm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // 1 to 100 Loading Animation State
    var loadingProgress by remember { mutableIntStateOf(1) }
    var isLoadingComplete by remember { mutableStateOf(false) }

    LaunchedEffect(memberWithWeeks.member.id) {
        isLoadingComplete = false
        loadingProgress = 1
        val durationMs = 850L
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            val eased = fraction * fraction * (3f - 2f * fraction)
            loadingProgress = (1 + eased * 99).toInt().coerceIn(1, 100)
            if (fraction >= 1f) {
                loadingProgress = 100
                delay(80)
                isLoadingComplete = true
                break
            }
            delay(16)
        }
    }

    val cycleInfo = memberWithWeeks.getCycleInfo()
    val currentPrincipal = cycleInfo.currentPrincipal
    val weeklyProfit = cycleInfo.weeklyProfit
    val effectiveStart = if (memberWithWeeks.member.startDate > 0L) memberWithWeeks.member.startDate else memberWithWeeks.member.createdAt
    val weekDisplayItems = memberWithWeeks.getAllWeekDisplayItems()

    // Full screen dialog taking entire device screen
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
                .systemBarsPadding()
        ) {
            Crossfade(
                targetState = isLoadingComplete,
                label = "detail_loading_crossfade"
            ) { loaded ->
                if (!loaded) {
                    // Full Screen 1 to 100 Loading Screen
                    DetailLoadingScreen(
                        progress = loadingProgress,
                        memberId = memberWithWeeks.member.id,
                        photoUri = memberWithWeeks.member.photoUri
                    )
                } else {
                    // Full Screen Member Profile & Accounts Page
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Navigation App Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.testTag("back_to_dashboard_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = "ফিরে যান",
                                            tint = Color(0xFF0F172A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = "সদস্য প্রোফাইল ও বিস্তারিত",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "আইডি: ${memberWithWeeks.member.id}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showEditProfileDialog = true },
                                        modifier = Modifier.testTag("top_edit_profile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "PROFILE EDIT",
                                            tint = MenuColors.ListPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDeleteConfirm = true },
                                        modifier = Modifier.testTag("delete_member_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "সদস্য মুছুন",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }

                        // Scrollable Body (Profile & Installment Content)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            // Member Profile Card
                            item {
                                MemberProfileHeaderCard(
                                    memberWithWeeks = memberWithWeeks,
                                    cycleInfo = cycleInfo,
                                    effectiveStart = effectiveStart,
                                    onEditProfileClick = { showEditProfileDialog = true },
                                    onPhotoChanged = { newPhotoUri ->
                                        onUpdateProfile(
                                            memberWithWeeks.member.id,
                                            memberWithWeeks.member.id,
                                            memberWithWeeks.member.phone,
                                            newPhotoUri,
                                            memberWithWeeks.member.guarantor
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // 7-day Cycle & Dynamic Days Remaining Card
                            item {
                                val isUrgent = cycleInfo.daysRemaining <= 1 || cycleInfo.isOverdue
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isUrgent) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isUrgent) Color(0xFFFECACA) else Color(0xFFBBF7D0)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Schedule,
                                                contentDescription = "Cycle status",
                                                tint = if (isUrgent) Color(0xFFDC2626) else Color(0xFF166534),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "সপ্তাহ ${cycleInfo.currentWeekNumber} এর ৭ দিনের সাইকেল",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isUrgent) Color(0xFF991B1B) else Color(0xFF14532D)
                                                )
                                                Text(
                                                    text = "${cycleInfo.countdownText} (${cycleInfo.banglaCountdownText})",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isUrgent) Color(0xFFDC2626) else Color(0xFF166534)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isUrgent) Color(0xFFDC2626) else Color(0xFF059669)
                                        ) {
                                            Text(
                                                text = if (isUrgent) "বাকি" else "চলমান",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Financial Summary Grid
                            item {
                                Text(
                                    text = "আর্থিক বিবরণী",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FinancialStatTile(
                                        title = "মূল আসল",
                                        amount = MemberWithWeeks.formatMoney(memberWithWeeks.member.initialPrincipal),
                                        bg = Color(0xFFF1F5F9),
                                        textColor = Color(0xFF334155),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FinancialStatTile(
                                        title = "বর্তমান মোট আসল",
                                        amount = MemberWithWeeks.formatMoney(currentPrincipal),
                                        bg = MenuColors.AddMemberBg,
                                        textColor = MenuColors.AddMemberPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FinancialStatTile(
                                        title = "সাপ্তাহিক লাভ",
                                        amount = MemberWithWeeks.formatMoney(weeklyProfit),
                                        bg = MenuColors.ListBg,
                                        textColor = MenuColors.ListPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FinancialStatTile(
                                        title = "মোট লাভ জমা",
                                        amount = MemberWithWeeks.formatMoney(memberWithWeeks.totalReceived()),
                                        bg = Color(0xFFEFF6FF),
                                        textColor = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Weekly Installment Section Header with BIG "জমা দিন" BUTTON (NO EMOJI)
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "সাপ্তাহিক কিস্তি বিবরণ",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "প্রতি সপ্তাহের লাভ আদায় ও হিসাব",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        // Requirement 4: "joma din lekha ta boro kore daw emoji tule dew"
                                        Button(
                                            onClick = { showAddWeekForm = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MenuColors.ListPrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                            modifier = Modifier.testTag("add_week_record_button")
                                        ) {
                                            Text(
                                                text = "জমা দিন",
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Week Cycles List Header
                            item {
                                Text(
                                    text = "কিস্তি ও সাইকেল ইতিহাস (${weekDisplayItems.size} টি)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            // Week cycles list items
                            items(weekDisplayItems) { item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "সপ্তাহ ${item.weekIndex}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                if (item.isCurrentActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFDBEAFE)
                                                    ) {
                                                        Text(
                                                            text = "চলমান",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF1E40AF),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = when {
                                                    item.isAutoCompounded -> "বকেয়া থাকায় আসলের সাথে যুক্ত হয়েছে"
                                                    item.isCurrentActive -> "বর্তমান চলমান সপ্তাহ"
                                                    item.isPaid -> "সম্পূর্ণ পরিশোধিত"
                                                    else -> "আংশিক জমা"
                                                },
                                                fontSize = 11.sp,
                                                color = if (item.isAutoCompounded) Color(0xFFD97706) else Color(0xFF64748B),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "আদায়: ${MemberWithWeeks.formatMoney(item.receivedAmount)} / ${MemberWithWeeks.formatMoney(item.expectedAmount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A)
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            when {
                                                item.isPaid -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFECFDF5),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                                                    ) {
                                                        Text(
                                                            text = "পরিশোধিত",
                                                            color = Color(0xFF059669),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                                item.isAutoCompounded -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFFFBEB),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                                                    ) {
                                                        Text(
                                                            text = "আসলে যুক্ত",
                                                            color = Color(0xFFB45309),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFFFBEB),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                                                    ) {
                                                        Text(
                                                            text = "বাকি ${MemberWithWeeks.formatMoney(item.dueAmount)}",
                                                            color = Color(0xFFD97706),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to add a week profit record (Requirement 4: BIG "জমা দিন" without emoji)
    if (showAddWeekForm) {
        var receivedText by remember { mutableStateOf(weeklyProfit.toString()) }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddWeekForm = false },
            title = {
                Text(
                    text = "টাকা জমা (${memberWithWeeks.member.id})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "বর্তমান আসল: ${MemberWithWeeks.formatMoney(currentPrincipal)}",
                                color = Color(0xFF166534),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "এই সপ্তাহের নির্ধারিত লাভ: ${MemberWithWeeks.formatMoney(weeklyProfit)} টাকা (${cycleInfo.statusText})",
                                color = Color(0xFF15803D),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = receivedText,
                        onValueChange = {
                            receivedText = it.filter { c -> c.isDigit() }
                            formError = null
                        },
                        label = { Text("কত টাকা আদায় হয়েছে?") },
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MenuColors.ListPrimary,
                            cursorColor = MenuColors.ListPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("received_amount_input")
                    )

                    val receivedVal = receivedText.toLongOrNull() ?: 0L
                    if (receivedVal < weeklyProfit) {
                        val dueDiff = weeklyProfit - receivedVal
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ বাকি থাকবে ${MemberWithWeeks.formatMoney(dueDiff)}, যা স্বয়ংক্রিয়ভাবে আসলের সাথে যোগ হয়ে যাবে।",
                            color = MenuColors.DueSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (formError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formError!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                // Requirement 4: "joma din lekha ta boro kore daw emoji tule dew"
                Button(
                    onClick = {
                        val received = receivedText.toLongOrNull()
                        if (received == null || received < 0) {
                            formError = "সঠিক টাকার পরিমাণ দিন"
                            return@Button
                        }
                        onAddWeekRecord(memberWithWeeks.member.id, weeklyProfit, received)
                        showAddWeekForm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MenuColors.ListPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("confirm_week_record_button")
                ) {
                    Text("জমা দিন", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddWeekForm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Modal to Edit Profile (Change Name, Phone, Guarantor, Photo)
    if (showEditProfileDialog) {
        EditProfileDialog(
            member = memberWithWeeks.member,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newId, newPhone, newPhotoUri, newGuarantor ->
                onUpdateProfile(
                    memberWithWeeks.member.id,
                    newId,
                    newPhone,
                    newPhotoUri,
                    newGuarantor
                )
                showEditProfileDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("সদস্য ডিলিট নিশ্চিতকরণ") },
            text = {
                Text("আপনি কি নিশ্চিত যে সদস্য '${memberWithWeeks.member.id}' এবং তার সব হিসাব ডিলিট করতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(memberWithWeeks.member)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("ডিলিট করুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

// 1 to 100 Loading Screen Component
@Composable
private fun DetailLoadingScreen(
    progress: Int,
    memberId: String,
    photoUri: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF064E3B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pulsating Avatar Box
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .border(2.dp, Color(0xFF34D399).copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                MemberAvatar(
                    photoUri = photoUri,
                    name = memberId,
                    size = 80.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = memberId,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "হিসাব ও প্রোফাইল প্রস্তুত হচ্ছে...",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Big Digital Percentage Display (1 to 100)
            Text(
                text = "$progress%",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF34D399),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Glowing Modern Linear Progress Bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF10B981),
                        trackColor = Color(0x3310B981),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "১% থেকে ১০০% সম্পূর্ণ হচ্ছে...",
                fontSize = 12.sp,
                color = Color(0xFF6EE7B7)
            )
        }
    }
}

// Profile Header Card (প্রোফাইলের মতো ডিজাইন)
@Composable
private fun MemberProfileHeaderCard(
    memberWithWeeks: MemberWithWeeks,
    cycleInfo: com.example.data.model.CycleInfo,
    effectiveStart: Long,
    onEditProfileClick: () -> Unit,
    onPhotoChanged: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Photo (Large + Editable)
                MemberAvatar(
                    photoUri = memberWithWeeks.member.photoUri,
                    name = memberWithWeeks.member.id,
                    size = 80.dp,
                    isEditable = true,
                    memberIdForStorage = memberWithWeeks.member.id,
                    onPhotoChanged = onPhotoChanged
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = memberWithWeeks.member.id,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (cycleInfo.isOverdue) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (cycleInfo.isOverdue) Color(0xFFFECACA) else Color(0xFFA7F3D0)
                            )
                        ) {
                            Text(
                                text = if (cycleInfo.isOverdue) "Overdue" else "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cycleInfo.isOverdue) Color(0xFFDC2626) else Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Mobile Number with Click-to-Call
                    if (memberWithWeeks.member.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${memberWithWeeks.member.phone}"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = "ফোন",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = memberWithWeeks.member.phone,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF059669).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "কল করুন",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "মোবাইল নম্বর যোগ করুন",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Guarantor Name Display
                    if (memberWithWeeks.member.guarantor.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = "গ্যারান্টার",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "গ্যারান্টার: ${memberWithWeeks.member.guarantor}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    Text(
                        text = "শুরুর তারিখ: ${MemberWithWeeks.formatBanglaDateWithDay(effectiveStart)}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Profile Edit Button
            OutlinedButton(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MenuColors.ListPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MenuColors.ListPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "প্রোফাইল তথ্য এডিট করুন (নাম, নম্বর, গ্যারান্টার)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Dialog to Edit Profile (Name, Phone, Photo, Guarantor)
@Composable
private fun EditProfileDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (newId: String, newPhone: String, newPhotoUri: String, newGuarantor: String) -> Unit
) {
    var idText by remember { mutableStateOf(member.id) }
    var phoneText by remember { mutableStateOf(member.phone) }
    var guarantorText by remember { mutableStateOf(member.guarantor) }
    var photoUri by remember { mutableStateOf(member.photoUri) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val emeraldColor = MenuColors.ListPrimary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "প্রোফাইল এডিট করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo selector in center
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MemberAvatar(
                        photoUri = photoUri,
                        name = idText,
                        size = 80.dp,
                        isEditable = true,
                        memberIdForStorage = idText,
                        onPhotoChanged = { photoUri = it }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ছবি পরিবর্তন করতে ট্যাপ করুন 📷",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = emeraldColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name / Member ID input
                OutlinedTextField(
                    value = idText,
                    onValueChange = {
                        idText = it
                        errorMsg = null
                    },
                    label = { Text("সদস্যের নাম বা আইডি") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone number input
                OutlinedTextField(
                    value = phoneText,
                    onValueChange = {
                        phoneText = it
                        errorMsg = null
                    },
                    label = { Text("মোবাইল নম্বর") },
                    prefix = { Text("📞 ", fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Guarantor name input
                OutlinedTextField(
                    value = guarantorText,
                    onValueChange = {
                        guarantorText = it
                        errorMsg = null
                    },
                    label = { Text("গ্যারান্টারের নাম") },
                    prefix = { Text("🛡️ ", fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emeraldColor,
                        focusedLabelColor = emeraldColor,
                        cursorColor = emeraldColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_guarantor_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedId = idText.trim()
                    if (trimmedId.isEmpty()) {
                        errorMsg = "নাম বা আইডি খালি রাখা যাবে না"
                        return@Button
                    }
                    onSave(trimmedId, phoneText.trim(), photoUri.trim(), guarantorText.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = emeraldColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun FinancialStatTile(
    title: String,
    amount: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
