package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

// Bento Grid Dark Theme Colors (Obsidian Slate & Frosted Borders)
val BentoBackground = Color(0xFF0A0B0E)
val BentoSurface = Color(0xFF161B22)
val BentoSurfaceElevated = Color(0xFF1A212D)
val BentoSurfaceCard = Color(0xFF12161F)
val BentoSurfaceNav = Color(0xFF0F1115)
val BentoTextPrimary = Color(0xFFF8FAFC)
val BentoTextSecondary = Color(0xFF94A3B8)
val BentoTextMuted = Color(0xFF64748B)
val BentoBorder = Color(0x14FFFFFF) // border-white/8
val BentoBorderSubtle = Color(0x0DFFFFFF) // border-white/5

// Compatibility Aliases for Dark Theme
val DarkBackground = BentoBackground
val DarkSurface = BentoSurface
val DarkSurfaceElevated = BentoSurfaceElevated
val DarkSurfaceCard = BentoSurfaceCard
val DarkTextPrimary = BentoTextPrimary
val DarkTextSecondary = BentoTextSecondary
val DarkBorder = BentoBorder

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1F5F9)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightBorder = Color(0xFFE2E8F0)

// Bento Grid Accent Palette
val BrandEmerald = Color(0xFF10B981)
val BrandEmeraldLight = Color(0xFF34D399)
val BrandCyan = Color(0xFF06B6D4)
val BrandCyanLight = Color(0xFF22D3EE)
val BrandGold = Color(0xFFF59E0B)
val BrandGoldLight = Color(0xFFFBBF24)
val BrandPurple = Color(0xFF8B5CF6)
val BrandIndigo = Color(0xFF6366F1)
val BrandGreen = Color(0xFF10B981) // Aligned with Emerald
val BrandRed = Color(0xFFF43F5E) // Rose-500
val BrandRose = Color(0xFFF43F5E)
val BrandBlue = Color(0xFF3B82F6)
val BrandAmber = Color(0xFFF59E0B)
val BrandOrange = Color(0xFFF97316)
val BrandPink = Color(0xFFEC4899)

// --- CENTRALIZED PREMIUM DESIGN SYSTEM: DARK GOLD ---
data class AppColors(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val buttonColor: Color,
    val buttonTextColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val iconColor: Color,
    val borderColor: Color,
    val accentColor: Color,
    val errorColor: Color,
    val successColor: Color,
    val isDark: Boolean
)

val DarkGoldColors = AppColors(
    backgroundColor = Color(0xFF090D0E),
    surfaceColor = Color(0xFF12191A),
    buttonColor = Color(0xFF163F40),
    buttonTextColor = Color(0xFFFFFFFF),
    primaryTextColor = Color(0xFFF4F6F6),
    secondaryTextColor = Color(0xFF9AA7A8),
    iconColor = Color(0xFFD4AF37),
    borderColor = Color(0xFF293031),
    accentColor = Color(0xFFD4AF37),
    errorColor = Color(0xFFF43F5E),
    successColor = Color(0xFF10B981),
    isDark = true
)

val LightGoldColors = AppColors(
    backgroundColor = Color(0xFFF4F6F6),
    surfaceColor = Color(0xFFFFFFFF),
    buttonColor = Color(0xFF163F40), // Consistent premium brand color
    buttonTextColor = Color(0xFFFFFFFF),
    primaryTextColor = Color(0xFF090D0E),
    secondaryTextColor = Color(0xFF5F6C6D),
    iconColor = Color(0xFFD4AF37),
    borderColor = Color(0xFFD2DBDC),
    accentColor = Color(0xFFD4AF37),
    errorColor = Color(0xFFD32F2F),
    successColor = Color(0xFF00875A),
    isDark = false
)

val LocalAppColors = staticCompositionLocalOf { DarkGoldColors }

