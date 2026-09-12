package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MemberWithWeeks
import com.example.ui.theme.MenuColors
import com.example.ui.viewmodel.FinancialTotals

@Composable
fun FullSummaryDialog(
    totals: FinancialTotals,
    onDismiss: () -> Unit
) {
    val purpleColor = MenuColors.StatsPrimary

    val collectionPercentage = if (totals.expectedProfit > 0) {
        ((totals.receivedProfit.toFloat() / totals.expectedProfit.toFloat()) * 100f).coerceIn(0f, 100f)
    } else {
        100f
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header in Vivid Purple
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MenuColors.StatsBg)
                                .border(1.dp, MenuColors.StatsBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = "Summary",
                                tint = purpleColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "সম্পূর্ণ আর্থিক হিসাব",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "সমিতির সামগ্রিক আর্থিক বিবরণী",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Collection Rate Progress Card
                Surface(
                    color = MenuColors.StatsBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MenuColors.StatsBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "লাভ আদায়ের হার",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MenuColors.StatsText
                            )
                            Text(
                                text = "${String.format("%.1f", collectionPercentage)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = purpleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { collectionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = purpleColor,
                            trackColor = Color(0xFFE9D5FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Rows
                SummaryStatRow(
                    label = "মোট সদস্য সংখ্যা",
                    value = "${totals.memberCount} জন (🟢 ${totals.activeMemberCount} Active | 🔴 ${totals.overdueMemberCount} Overdue)",
                    color = MenuColors.CardMembers,
                    icon = Icons.Rounded.Groups
                )

                SummaryStatRow(
                    label = "মোট দেনা / ঋণ (আসল + লাভ)",
                    value = MemberWithWeeks.formatMoney(totals.totalDebt),
                    color = Color(0xFF4338CA),
                    icon = Icons.Rounded.MonetizationOn
                )

                SummaryStatRow(
                    label = "প্রাথমিক মোট আসল",
                    value = MemberWithWeeks.formatMoney(totals.initialPrincipal),
                    color = MenuColors.CardPrincipal,
                    icon = Icons.Rounded.MonetizationOn
                )

                SummaryStatRow(
                    label = "বর্তমান মোট আসল (বাকি যুক্ত)",
                    value = MemberWithWeeks.formatMoney(totals.currentPrincipal),
                    color = Color(0xFF047857),
                    icon = Icons.Rounded.MonetizationOn
                )

                SummaryStatRow(
                    label = "মোট পাওয়ার কথা (প্রাক্কলিত লাভ)",
                    value = MemberWithWeeks.formatMoney(totals.expectedProfit),
                    color = MenuColors.CardExpected,
                    icon = Icons.Rounded.Analytics
                )

                SummaryStatRow(
                    label = "মোট পাওয়া লাভ (আদায়কৃত)",
                    value = MemberWithWeeks.formatMoney(totals.receivedProfit),
                    color = MenuColors.CardReceived,
                    icon = Icons.Rounded.CheckCircle
                )

                SummaryStatRow(
                    label = "মোট বাকি লাভ",
                    value = MemberWithWeeks.formatMoney(totals.unpaidProfit),
                    color = MenuColors.CardUnpaid,
                    icon = Icons.Rounded.Warning
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = purpleColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryStatRow(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
