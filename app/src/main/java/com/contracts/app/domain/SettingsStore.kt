package com.contracts.app.domain

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.contracts.app.data.model.AppThemeId
import com.contracts.app.data.model.ColorSlot
import com.contracts.app.ui.theme.AppColors
import com.contracts.app.ui.theme.get
import com.contracts.app.ui.theme.themeColors
import com.contracts.app.ui.theme.with

/**
 * ذخیره‌سازی ساده‌ی تنظیمات اپ (تم، رنگ‌های سفارشی، لوگو، ایمیل مدیر)
 * روی SharedPreferences تا بعد از بستن اپ هم باقی بمونن.
 */
object SettingsStore {
    private const val PREFS = "contracts_app_settings"
    private const val KEY_THEME = "theme_id"
    private const val KEY_LOGO_PATH = "logo_path"
    private const val KEY_MY_EMAIL = "my_email"
    private const val KEY_NOTIFY_THRESHOLDS = "notify_thresholds"
    private fun keyForSlot(slot: ColorSlot) = "custom_${slot.name}"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadThemeId(ctx: Context): AppThemeId =
        runCatching { AppThemeId.valueOf(prefs(ctx).getString(KEY_THEME, null) ?: "") }
            .getOrDefault(AppThemeId.LIGHT)

    fun saveThemeId(ctx: Context, id: AppThemeId) {
        prefs(ctx).edit().putString(KEY_THEME, id.name).apply()
    }

    fun loadCustomColors(ctx: Context): AppColors {
        val base = themeColors[AppThemeId.LIGHT]!!
        val p = prefs(ctx)
        var colors = base
        ColorSlot.entries.forEach { slot ->
            val argb = p.getInt(keyForSlot(slot), base.get(slot).toArgb())
            colors = colors.with(slot, Color(argb))
        }
        return colors
    }

    fun saveCustomColor(ctx: Context, slot: ColorSlot, color: Color) {
        prefs(ctx).edit().putInt(keyForSlot(slot), color.toArgb()).apply()
    }

    fun loadLogoPath(ctx: Context): String? = prefs(ctx).getString(KEY_LOGO_PATH, null)

    fun saveLogoPath(ctx: Context, path: String?) {
        prefs(ctx).edit().putString(KEY_LOGO_PATH, path).apply()
    }

    fun loadMyEmail(ctx: Context): String = prefs(ctx).getString(KEY_MY_EMAIL, "") ?: ""

    fun saveMyEmail(ctx: Context, email: String) {
        prefs(ctx).edit().putString(KEY_MY_EMAIL, email).apply()
    }

    fun loadNotifyThresholds(ctx: Context): Set<Int> {
        val raw = prefs(ctx).getString(KEY_NOTIFY_THRESHOLDS, null) ?: return setOf(30, 60)
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().ifEmpty { setOf(30, 60) }
    }

    fun saveNotifyThresholds(ctx: Context, days: Set<Int>) {
        prefs(ctx).edit().putString(KEY_NOTIFY_THRESHOLDS, days.joinToString(",")).apply()
    }
}
