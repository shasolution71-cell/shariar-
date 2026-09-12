package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.util.SoundManager
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MenuColors
import com.example.ui.theme.RgbNeonAmber
import com.example.ui.theme.RgbNeonBlue
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonMagenta
import com.example.ui.theme.RgbNeonRed
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpeningScreen(
    onEnterClick: () -> Unit,
    onNavigateToAddMember: () -> Unit = onEnterClick,
    onNavigateToSearch: () -> Unit = onEnterClick,
    onNavigateToList: () -> Unit = onEnterClick,
    onNavigateToDue: () -> Unit = onEnterClick,
    onNavigateToSummary: () -> Unit = onEnterClick,
    modifier: Modifier = Modifier
) {
    // Dynamic continuous RGB flow animation using drawWithCache to avoid UI recomposition
    val infiniteTransition = rememberInfiniteTransition(label = "rgb_flow")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rgb_angle"
    )

    val buttonRgbBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF0055),
                Color(0xFFFF9900),
                Color(0xFF00FF66),
                Color(0xFF00DDFF),
                Color(0xFF8800FF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp)
        ) {
            // Glowing RGB Outer Border Card (Hardware accelerated GPU draw without recomposing content)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .drawBehind {
                        val radians = Math.toRadians(angle.toDouble())
                        val cosVal = cos(radians).toFloat()
                        val sinVal = sin(radians).toFloat()
                        val w = this.size.width
                        val h = this.size.height
                        val centerX = w / 2f
                        val centerY = h / 2f
                        val radius = maxOf(w, h) / 2f
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                RgbNeonRed,
                                RgbNeonAmber,
                                RgbNeonGreen,
                                RgbNeonCyan,
                                RgbNeonBlue,
                                RgbNeonMagenta,
                                RgbNeonRed
                            ),
                            start = Offset(centerX + radius * cosVal, centerY + radius * sinVal),
                            end = Offset(centerX - radius * cosVal, centerY - radius * sinVal)
                        )
                        drawRect(brush)
                    }
                    .padding(3.dp) // Border thickness
            ) {
                // Inner Dark Glassmorphism Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color(0xF00D1527))
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top glowing badge
                    Surface(
                        color = Color(0x2200E5FF),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(RgbNeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সদস্য ও সাপ্তাহিক লাভের হিসাব",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title with vibrant RGB text
                    Text(
                        text = "সাপ্তাহিক সমিতি",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "স্মার্ট ডিজিটাল খাতা ও স্বয়ংক্রিয় হিসাব",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Key Rule Pills in RGB Accents
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RgbFeaturePill(text = "সমবায় সমিতি", color = RgbNeonGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        RgbFeaturePill(text = "পরিচালক রুমান আকন্দ", color = RgbNeonAmber)
                        Spacer(modifier = Modifier.width(12.dp))
                        RgbFeaturePill(text = "01741955688", color = RgbNeonCyan)
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Pulsing, Glowing RGB ENTER Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = RgbNeonBlue,
                                spotColor = RgbNeonMagenta
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(buttonRgbBrush)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = onEnterClick
                            )
                            .padding(vertical = 14.dp)
                            .testTag("enter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENTER",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = "Enter",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section showcasing each separate menu color
            Text(
                text = "SHARIAR SHUVO SARKAR",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preview grid of the 5 separate colored menus
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpeningMenuPreviewChip(
                        title = "সদস্য যোগ",
                        color = MenuColors.AddMemberPrimary,
                        icon = Icons.Rounded.PersonAdd,
                        onClick = onNavigateToAddMember,
                        modifier = Modifier.weight(1f)
                    )
                    OpeningMenuPreviewChip(
                        title = "সদস্য খুঁজুন",
                        color = MenuColors.SearchPrimary,
                        icon = Icons.Rounded.Search,
                        onClick = onNavigateToSearch,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpeningMenuPreviewChip(
                        title = "সদস্য তালিকা",
                        color = MenuColors.ListPrimary,
                        icon = Icons.Rounded.FormatListBulleted,
                        onClick = onNavigateToList,
                        modifier = Modifier.weight(1f)
                    )
                    OpeningMenuPreviewChip(
                        title = "বাকি লাভ",
                        color = MenuColors.DuePrimary,
                        icon = Icons.Rounded.WarningAmber,
                        onClick = onNavigateToDue,
                        modifier = Modifier.weight(1f)
                    )
                }

                OpeningMenuPreviewChip(
                    title = "সম্পূর্ণ আর্থিক হিসাব বিবরণী",
                    color = MenuColors.StatsPrimary,
                    icon = Icons.Rounded.Analytics,
                    onClick = onNavigateToSummary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RgbFeaturePill(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OpeningMenuPreviewChip(
    title: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color(0x18FFFFFF),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
