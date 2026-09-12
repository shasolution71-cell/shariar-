package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemberWithWeeks
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.FullSummaryDialog
import com.example.ui.components.MemberAvatar
import com.example.ui.components.MemberDetailDialog
import com.example.ui.theme.MenuColors
import com.example.ui.viewmodel.FilterMode
import com.example.ui.viewmodel.FinancialTotals
import com.example.ui.viewmodel.SomityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: SomityViewModel,
    onBackToOpening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val members by viewModel.displayedMembers.collectAsStateWithLifecycle()
    val allMembersList by viewModel.allMembers.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()

    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val showSummaryDialog by viewModel.showSummaryDialog.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var showOptionsMenu by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    val dueMembersCount = allMembersList.count { it.hasUnpaid() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "সাপ্তাহিক সমিতি",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "সদস্য ও সাপ্তাহিক লাভের হিসাব",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    // Quick RGB Opening Page button
                    IconButton(
                        onClick = onBackToOpening,
                        modifier = Modifier.testTag("opening_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "ওপেনিং পেজ",
                            tint = Color(0xFF3B82F6)
                        )
                    }

                    // 3-dots Menu with individual RGB Menu Colors
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Menu",
                                tint = Color(0xFF1E293B)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            // Menu 1: সদস্য যোগ (Emerald Green)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "👤 সদস্য যোগ",
                                        fontWeight = FontWeight.Bold,
                                        color = MenuColors.AddMemberText
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.openAddMemberDialog()
                                },
                                modifier = Modifier
                                    .background(MenuColors.AddMemberBg)
                                    .testTag("menu_item_add_member")
                            )

                            // Menu 2: Member Search (Cyan)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🔎 Member Search",
                                        fontWeight = FontWeight.Bold,
                                        color = MenuColors.SearchText
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    isSearchExpanded = true
                                    viewModel.setFilter(FilterMode.ALL)
                                },
                                modifier = Modifier.background(MenuColors.SearchBg)
                            )

                            // Menu 3: Member List (Royal Indigo)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "📋 Member List",
                                        fontWeight = FontWeight.Bold,
                                        color = MenuColors.ListText
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.setFilter(FilterMode.ALL)
                                    viewModel.onSearchQueryChange("")
                                },
                                modifier = Modifier.background(MenuColors.ListBg)
                            )

                            // Menu 4: Overdue সদস্য (Red)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🔴 Overdue সদস্য (${totals.overdueMemberCount})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.setFilter(FilterMode.OVERDUE_ONLY)
                                },
                                modifier = Modifier.background(Color(0xFFFEF2F2))
                            )

                            // Menu 5: Active সদস্য (Emerald Green)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🟢 Active সদস্য (${totals.activeMemberCount})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.setFilter(FilterMode.ACTIVE_ONLY)
                                },
                                modifier = Modifier.background(Color(0xFFECFDF5))
                            )

                            // Menu 6: সম্পূর্ণ হিসাব (Vivid Purple)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "📊 সম্পূর্ণ হিসাব",
                                        fontWeight = FontWeight.Bold,
                                        color = MenuColors.StatsText
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.openSummaryDialog()
                                },
                                modifier = Modifier.background(MenuColors.StatsBg)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SEPARATE MENU COLOR BUTTONS (Prominent Bar on Top)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Menu 1: সদস্য যোগ (Emerald Green)
                    MenuFilterChip(
                        title = "+ সদস্য যোগ",
                        icon = Icons.Rounded.PersonAdd,
                        primaryColor = MenuColors.AddMemberPrimary,
                        bgColor = MenuColors.AddMemberBg,
                        borderColor = MenuColors.AddMemberBorder,
                        textColor = MenuColors.AddMemberText,
                        isSelected = false,
                        onClick = { viewModel.openAddMemberDialog() },
                        testTag = "chip_add_member"
                    )

                    // Menu 2: Member Search (Cyan)
                    MenuFilterChip(
                        title = if (isSearchExpanded) "খোঁজা বন্ধ" else "🔎 খুঁজুন",
                        icon = Icons.Rounded.Search,
                        primaryColor = MenuColors.SearchPrimary,
                        bgColor = MenuColors.SearchBg,
                        borderColor = MenuColors.SearchBorder,
                        textColor = MenuColors.SearchText,
                        isSelected = isSearchExpanded,
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                        },
                        testTag = "chip_search"
                    )

                    // Menu 3: Member List (Royal Indigo)
                    MenuFilterChip(
                        title = "📋 সদস্য তালিকা (${allMembersList.size})",
                        icon = Icons.Rounded.FormatListBulleted,
                        primaryColor = MenuColors.ListPrimary,
                        bgColor = MenuColors.ListBg,
                        borderColor = MenuColors.ListBorder,
                        textColor = MenuColors.ListText,
                        isSelected = filterMode == FilterMode.ALL && !isSearchExpanded,
                        onClick = {
                            viewModel.setFilter(FilterMode.ALL)
                            viewModel.onSearchQueryChange("")
                            isSearchExpanded = false
                        },
                        testTag = "chip_all_members"
                    )

                    // Menu 4: Overdue (Red / Rose)
                    MenuFilterChip(
                        title = "🔴 Overdue (${totals.overdueMemberCount})",
                        icon = Icons.Rounded.WarningAmber,
                        primaryColor = Color(0xFFDC2626),
                        bgColor = Color(0xFFFEF2F2),
                        borderColor = Color(0xFFFECACA),
                        textColor = Color(0xFFDC2626),
                        isSelected = filterMode == FilterMode.OVERDUE_ONLY || filterMode == FilterMode.DUE_ONLY,
                        onClick = {
                            viewModel.setFilter(FilterMode.OVERDUE_ONLY)
                        },
                        testTag = "chip_overdue"
                    )

                    // Menu 5: Active (Emerald Green)
                    MenuFilterChip(
                        title = "🟢 Active (${totals.activeMemberCount})",
                        icon = Icons.Rounded.CheckCircle,
                        primaryColor = Color(0xFF059669),
                        bgColor = Color(0xFFECFDF5),
                        borderColor = Color(0xFFA7F3D0),
                        textColor = Color(0xFF059669),
                        isSelected = filterMode == FilterMode.ACTIVE_ONLY,
                        onClick = {
                            viewModel.setFilter(FilterMode.ACTIVE_ONLY)
                        },
                        testTag = "chip_active"
                    )

                    // Menu 6: সম্পূর্ণ হিসাব (Vivid Purple)
                    MenuFilterChip(
                        title = "📊 সম্পূর্ণ হিসাব",
                        icon = Icons.Rounded.Analytics,
                        primaryColor = MenuColors.StatsPrimary,
                        bgColor = MenuColors.StatsBg,
                        borderColor = MenuColors.StatsBorder,
                        textColor = MenuColors.StatsText,
                        isSelected = false,
                        onClick = { viewModel.openSummaryDialog() },
                        testTag = "chip_summary"
                    )
                }
            }

            // SEARCH BAR SECTION (Animated appearance when clicked)
            item {
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MenuColors.SearchBg,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MenuColors.SearchBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MenuColors.SearchPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Member ID দিয়ে খুঁজুন...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = MenuColors.SearchPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(searchFocusRequester)
                                    .testTag("search_input")
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear Search",
                                        tint = MenuColors.SearchPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5 METRIC CARDS (Color-coded)
            item {
                MetricCardsGrid(totals = totals)
            }

            // FILTER MODE ACTIVE BANNER
            if (filterMode == FilterMode.OVERDUE_ONLY || filterMode == FilterMode.DUE_ONLY) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔴 Overdue (বকেয়া) সদস্যদের তালিকা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                            Text(
                                text = "সকল সদস্য দেখুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MenuColors.ListPrimary,
                                modifier = Modifier
                                    .clickable { viewModel.setFilter(FilterMode.ALL) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            } else if (filterMode == FilterMode.ACTIVE_ONLY) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🟢 Active (সচল) সদস্যদের তালিকা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                            Text(
                                text = "সকল সদস্য দেখুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MenuColors.ListPrimary,
                                modifier = Modifier
                                    .clickable { viewModel.setFilter(FilterMode.ALL) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // SECTION HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (filterMode) {
                            FilterMode.OVERDUE_ONLY, FilterMode.DUE_ONLY -> "Overdue (বকেয়া) সদস্যদের হিসাব (${members.size})"
                            FilterMode.ACTIVE_ONLY -> "Active (সচল) সদস্যদের হিসাব (${members.size})"
                            FilterMode.ALL -> "সকল সদস্যদের হিসাব (${members.size})"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // MEMBER ITEMS LIST
            if (members.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "'$searchQuery' দিয়ে কোনো সদস্য পাওয়া যায়নি"
                                } else if (filterMode == FilterMode.DUE_ONLY) {
                                    "কোনো সদস্যের বাকি লাভ নেই!"
                                } else {
                                    "কোনো সদস্য নেই"
                                },
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.openAddMemberDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = MenuColors.AddMemberPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ নতুন সদস্য যোগ করুন", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                items(members, key = { it.member.id }) { item ->
                    MemberRowCard(
                        memberWithWeeks = item,
                        onDetailsClick = { viewModel.openMemberDetail(item.member.id) },
                        onPhotoChanged = { newPhotoUri ->
                            viewModel.updateMemberProfile(
                                oldId = item.member.id,
                                newId = item.member.id,
                                phone = item.member.phone,
                                photoUri = newPhotoUri,
                                guarantor = item.member.guarantor
                            )
                        }
                    )
                }
            }
        }
    }

    // MODAL DIALOGS
    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { viewModel.closeAddMemberDialog() },
            onSave = { id, amount, phone, photoUri, guarantor, startDate ->
                viewModel.addMember(id, amount, phone, photoUri, guarantor, startDate)
            }
        )
    }

    val activeDetailMember = remember(selectedMemberId, allMembersList) {
        allMembersList.find { it.member.id == selectedMemberId }
    }

    if (activeDetailMember != null) {
        MemberDetailDialog(
            memberWithWeeks = activeDetailMember,
            onDismiss = { viewModel.closeMemberDetail() },
            onAddWeekRecord = { memberId, exp, rec ->
                viewModel.addWeekRecord(memberId, exp, rec)
            },
            onDeleteMember = { member ->
                viewModel.deleteMember(member)
            },
            onUpdateProfile = { oldId, newId, phone, photoUri, guarantor ->
                viewModel.updateMemberProfile(oldId, newId, phone, photoUri, guarantor)
            }
        )
    }

    if (showSummaryDialog) {
        FullSummaryDialog(
            totals = totals,
            onDismiss = { viewModel.closeSummaryDialog() }
        )
    }
}

@Composable
private fun MenuFilterChip(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) primaryColor else bgColor,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) primaryColor else borderColor
        ),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else primaryColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricCardsGrid(totals: FinancialTotals) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "মোট সদস্য (🟢${totals.activeMemberCount} | 🔴${totals.overdueMemberCount})",
                value = "${totals.memberCount} জন",
                themeColor = MenuColors.CardMembers,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "মোট দেনা / ঋণ (আসল+লাভ)",
                value = MemberWithWeeks.formatMoney(totals.totalDebt),
                themeColor = Color(0xFF4338CA),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "মোট আসল",
                value = MemberWithWeeks.formatMoney(totals.currentPrincipal),
                themeColor = MenuColors.CardPrincipal,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "মোট পাওয়ার লাভ",
                value = MemberWithWeeks.formatMoney(totals.expectedProfit),
                themeColor = MenuColors.CardExpected,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "মোট পাওয়া লাভ",
                value = MemberWithWeeks.formatMoney(totals.receivedProfit),
                themeColor = MenuColors.CardReceived,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "মোট বাকি লাভ",
                value = MemberWithWeeks.formatMoney(totals.unpaidProfit),
                themeColor = MenuColors.CardUnpaid,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(themeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = themeColor
            )
        }
    }
}

@Composable
private fun MemberRowCard(
    memberWithWeeks: MemberWithWeeks,
    onDetailsClick: () -> Unit,
    onPhotoChanged: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val cycleInfo = remember(memberWithWeeks) { memberWithWeeks.getCycleInfo() }
    val isOverdue = cycleInfo.isOverdue
    val currentPrincipal = cycleInfo.currentPrincipal
    val weeklyProfit = cycleInfo.weeklyProfit
    val totalDebt = cycleInfo.totalDebt

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${memberWithWeeks.member.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MemberAvatar(
                        photoUri = memberWithWeeks.member.photoUri,
                        name = memberWithWeeks.member.id,
                        size = 46.dp,
                        isEditable = true,
                        memberIdForStorage = memberWithWeeks.member.id,
                        onPhotoChanged = onPhotoChanged
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = memberWithWeeks.member.id,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Visual Indicator: Active vs Overdue Status Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOverdue) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isOverdue) Color(0xFFFECACA) else Color(0xFFA7F3D0)
                                ),
                                modifier = Modifier.testTag("status_badge_${memberWithWeeks.member.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isOverdue) Color(0xFFDC2626) else Color(0xFF059669))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOverdue) "Overdue" else "Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverdue) Color(0xFFDC2626) else Color(0xFF059669)
                                    )
                                }
                            }
                        }
                        // Week upore
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "সপ্তাহ ${cycleInfo.currentWeekNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = " • শুরু: ${MemberWithWeeks.formatBanglaDate(if (memberWithWeeks.member.startDate > 0L) memberWithWeeks.member.startDate else memberWithWeeks.member.createdAt)}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Number niche, size e boro
                        if (memberWithWeeks.member.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 3.dp)
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = memberWithWeeks.member.phone,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDetailsClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MenuColors.ListBg,
                        contentColor = MenuColors.ListPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("details_button_${memberWithWeeks.member.id}")
                ) {
                    Text(
                        text = "বিস্তারিত",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Countdown Timer Badge (e.g., '3 days remaining' / 'আর ৩ দিন বাকি')
            // Updates daily based on selected start date
            val isUrgent = cycleInfo.daysRemaining <= 1 || isOverdue
            Surface(
                shape = RoundedCornerShape(10.dp),
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
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUrgent) Icons.Rounded.WarningAmber else Icons.Rounded.Schedule,
                            contentDescription = "Countdown",
                            tint = if (isUrgent) Color(0xFFDC2626) else Color(0xFF166534),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${cycleInfo.countdownText} (${cycleInfo.banglaCountdownText})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUrgent) Color(0xFFDC2626) else Color(0xFF166534)
                            )
                        }
                    }

                    if (cycleInfo.autoCompoundedWeeksCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "🔄 আসলে যুক্ত +${MemberWithWeeks.formatMoney(cycleInfo.autoCompoundedAmount)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary (Total Debt, Current Principal, Weekly Profit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "মোট দেনা (ঋণ)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = MemberWithWeeks.formatMoney(totalDebt),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOverdue) Color(0xFFDC2626) else Color(0xFF0F172A)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "বর্তমান আসল",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = MemberWithWeeks.formatMoney(currentPrincipal),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MenuColors.CardPrincipal
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "সাপ্তাহিক লাভ",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = MemberWithWeeks.formatMoney(weeklyProfit),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MenuColors.DueSecondary
                    )
                }
            }
        }
    }
}
