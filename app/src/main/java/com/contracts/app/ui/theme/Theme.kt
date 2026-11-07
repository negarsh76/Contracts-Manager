package com.contracts.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.contracts.app.R
import com.contracts.app.data.model.AppThemeId
import com.contracts.app.data.model.ColorSlot

val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_medium, FontWeight.Medium),
    Font(R.font.vazir_bold, FontWeight.Bold),
    Font(R.font.vazir_bold, FontWeight.Black),
)

val VazirTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = VazirFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = VazirFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = VazirFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = VazirFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = VazirFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = VazirFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = VazirFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = VazirFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = VazirFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = VazirFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = VazirFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = VazirFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = VazirFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = VazirFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = VazirFontFamily),
    )
}

val StatusActive   = Color(0xFFADD8E6)
val StatusWarning  = Color(0xFFFFB266)
val StatusCritical = Color(0xFFFF6666)
val StatusExpired  = Color(0xFFA9A9A9)
val StatusActiveBg   = Color(0x1AADD8E6)
val StatusWarningBg  = Color(0x1AFFB266)
val StatusCriticalBg = Color(0x1AFF6666)
val StatusExpiredBg  = Color(0x1AA9A9A9)

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val outline: Color,
)

fun AppColors.get(slot: ColorSlot): Color = when (slot) {
    ColorSlot.BACKGROUND       -> background
    ColorSlot.SURFACE          -> surface
    ColorSlot.SURFACE_ELEVATED -> surfaceElevated
    ColorSlot.PRIMARY          -> primary
    ColorSlot.ON_PRIMARY       -> onPrimary
    ColorSlot.TEXT_PRIMARY     -> textPrimary
    ColorSlot.TEXT_SECONDARY   -> textSecondary
    ColorSlot.TEXT_MUTED       -> textMuted
    ColorSlot.OUTLINE          -> outline
}

fun AppColors.with(slot: ColorSlot, color: Color): AppColors = when (slot) {
    ColorSlot.BACKGROUND       -> copy(background = color)
    ColorSlot.SURFACE          -> copy(surface = color)
    ColorSlot.SURFACE_ELEVATED -> copy(surfaceElevated = color)
    ColorSlot.PRIMARY          -> copy(primary = color)
    ColorSlot.ON_PRIMARY       -> copy(onPrimary = color)
    ColorSlot.TEXT_PRIMARY     -> copy(textPrimary = color)
    ColorSlot.TEXT_SECONDARY   -> copy(textSecondary = color)
    ColorSlot.TEXT_MUTED       -> copy(textMuted = color)
    ColorSlot.OUTLINE          -> copy(outline = color)
}

val themeColors: Map<AppThemeId, AppColors> = mapOf(
    AppThemeId.LIGHT to AppColors(
        Color(0xFFFFFFFF), Color(0xFFF5F6F8), Color(0xFFFFFFFF),
        Color(0xFF2962FF), Color(0xFFFFFFFF),
        Color(0xFF1A1A1A), Color(0xFF5A6472), Color(0xFF9AA3AF), Color(0xFFE3E6EA)
    ),
    AppThemeId.DARK_NAVY to AppColors(
        Color(0xFF0D1B2A), Color(0xFF1A2D42), Color(0xFF243652),
        Color(0xFF00BCD4), Color(0xFF0D1B2A),
        Color(0xFFECF0F4), Color(0xFF8FA8BF), Color(0xFF4E6478), Color(0xFF2E4A63)
    ),
    AppThemeId.MIDNIGHT to AppColors(
        Color(0xFF0A0A1A), Color(0xFF12122A), Color(0xFF1A1A38),
        Color(0xFF8B5CF6), Color(0xFFFFFFFF),
        Color(0xFFE8E8FF), Color(0xFF9090C0), Color(0xFF505080), Color(0xFF2A2A50)
    ),
    AppThemeId.DEEP_FOREST to AppColors(
        Color(0xFF0A1A0F), Color(0xFF122218), Color(0xFF1A3022),
        Color(0xFF4CAF50), Color(0xFF0A1A0F),
        Color(0xFFE8F5E9), Color(0xFF80A880), Color(0xFF406040), Color(0xFF204020)
    ),
    AppThemeId.AMOLED to AppColors(
        Color(0xFF000000), Color(0xFF0D0D0D), Color(0xFF1A1A1A),
        Color(0xFFFFD700), Color(0xFF000000),
        Color(0xFFFFFFFF), Color(0xFFAAAAAA), Color(0xFF555555), Color(0xFF222222)
    ),
    AppThemeId.SUNSET to AppColors(
        Color(0xFF1A0A05), Color(0xFF2A1208), Color(0xFF3A1A0C),
        Color(0xFFFF6B35), Color(0xFF1A0A05),
        Color(0xFFFFF3E0), Color(0xFFBB8860), Color(0xFF7A5040), Color(0xFF4A2A1A)
    ),
    AppThemeId.OCEAN_GLASS to AppColors(
        Color(0xFF051520), Color(0xFF0A2535), Color(0xFF103045),
        Color(0xFF00E5FF), Color(0xFF051520),
        Color(0xFFE0F7FA), Color(0xFF70B8CC), Color(0xFF3A7080), Color(0xFF153040)
    ),
    AppThemeId.CUSTOM to AppColors(
        Color(0xFFFFFFFF), Color(0xFFF5F6F8), Color(0xFFFFFFFF),
        Color(0xFF2962FF), Color(0xFFFFFFFF),
        Color(0xFF1A1A1A), Color(0xFF5A6472), Color(0xFF9AA3AF), Color(0xFFE3E6EA)
    ),
)

val LocalAppColors = compositionLocalOf { themeColors[AppThemeId.LIGHT]!! }

@Composable
fun appColors() = LocalAppColors.current

@Composable
fun ContractsTheme(
    themeId: AppThemeId = AppThemeId.LIGHT,
    customColors: AppColors? = null,
    content: @Composable () -> Unit
) {
    val colors = if (themeId == AppThemeId.CUSTOM && customColors != null) customColors
        else themeColors[themeId] ?: themeColors[AppThemeId.LIGHT]!!
    val isLight = colors.background.luminance() > 0.5f
    val scheme = if (isLight) lightColorScheme(
        primary        = colors.primary,
        onPrimary      = colors.onPrimary,
        background     = colors.background,
        surface        = colors.surface,
        surfaceVariant = colors.surfaceElevated,
        onBackground   = colors.textPrimary,
        onSurface      = colors.textPrimary,
        outline        = colors.outline,
    ) else darkColorScheme(
        primary        = colors.primary,
        onPrimary      = colors.onPrimary,
        background     = colors.background,
        surface        = colors.surface,
        surfaceVariant = colors.surfaceElevated,
        onBackground   = colors.textPrimary,
        onSurface      = colors.textPrimary,
        outline        = colors.outline,
    )
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(colorScheme = scheme, typography = VazirTypography, content = content)
    }
}
