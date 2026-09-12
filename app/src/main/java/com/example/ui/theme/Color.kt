package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Default theme tokens
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// RGB Opening Page Colors
val RgbNeonRed = Color(0xFFFF1744)
val RgbNeonGreen = Color(0xFF00E676)
val RgbNeonBlue = Color(0xFF2979FF)
val RgbNeonCyan = Color(0xFF00E5FF)
val RgbNeonMagenta = Color(0xFFF50057)
val RgbNeonAmber = Color(0xFFFFAB00)
val RgbDarkBg = Color(0xFF0A0F1D)
val RgbCardBg = Color(0xCC11192C)

val RgbRainbowBrush = Brush.linearGradient(
    colors = listOf(
        RgbNeonRed,
        RgbNeonAmber,
        RgbNeonGreen,
        RgbNeonCyan,
        RgbNeonBlue,
        RgbNeonMagenta,
        RgbNeonRed
    )
)

// Distinct RGB Colors for Every Separate Menu
object MenuColors {
    // Menu 1: 👤 সদস্য যোগ (Add Member) - Emerald Green Theme
    val AddMemberPrimary = Color(0xFF10B981)
    val AddMemberSecondary = Color(0xFF059669)
    val AddMemberBg = Color(0xFFECFDF5)
    val AddMemberBorder = Color(0xFFA7F3D0)
    val AddMemberText = Color(0xFF065F46)

    // Menu 2: 🔎 Member Search (সদস্য খুঁজুন) - Electric Cyan Theme
    val SearchPrimary = Color(0xFF06B6D4)
    val SearchSecondary = Color(0xFF0284C7)
    val SearchBg = Color(0xFFF0F9FF)
    val SearchBorder = Color(0xFFBAE6FD)
    val SearchText = Color(0xFF0369A1)

    // Menu 3: 📋 Member List (সকল সদস্য) - Royal Indigo Theme
    val ListPrimary = Color(0xFF6366F1)
    val ListSecondary = Color(0xFF4F46E5)
    val ListBg = Color(0xFFEEF2FF)
    val ListBorder = Color(0xFFC7D2FE)
    val ListText = Color(0xFF3730A3)

    // Menu 4: ⚠️ বাকি লাভ (বাকি লাভ) - Radiant Amber Theme
    val DuePrimary = Color(0xFFF59E0B)
    val DueSecondary = Color(0xFFD97706)
    val DueBg = Color(0xFFFFFBEB)
    val DueBorder = Color(0xFFFDE68A)
    val DueText = Color(0xFF92400E)

    // Menu 5: 📊 সম্পূর্ণ হিসাব (সম্পূর্ণ বিবরণী) - Vivid Purple Theme
    val StatsPrimary = Color(0xFFA855F7)
    val StatsSecondary = Color(0xFF7C3AED)
    val StatsBg = Color(0xFFFAF5FF)
    val StatsBorder = Color(0xFFE9D5FF)
    val StatsText = Color(0xFF6B21A8)

    // Card Specific Tints
    val CardMembers = Color(0xFF4F46E5)
    val CardPrincipal = Color(0xFF059669)
    val CardExpected = Color(0xFF0284C7)
    val CardReceived = Color(0xFF10B981)
    val CardUnpaid = Color(0xFFE11D48)
}
