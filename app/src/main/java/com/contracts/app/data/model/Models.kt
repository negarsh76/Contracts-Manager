package com.contracts.app.data.model

data class Contract(
    val id: Int,
    val contractNumber: String = "",
    val title: String = "",
    val company: String = "",
    val startDateJalali: String = "",
    val endDateJalali: String = "",
    val startDateGreg: String = "",
    val endDateGreg: String = "",
    val durationMonths: Int = 0,
    val daysRemaining: Long = 0,
    val amount: String = "",
    val paymentMethod: String = "",
    val status: ContractStatus = ContractStatus.UNKNOWN,
    val contractType: String = "",
    val notes: String = "",
    val fileLink: String = "",
    val isStarred: Boolean = false,
    val personalNote: String = "",
    val managerEmail: String = "",
    val department: String = "",
    val contactName: String = "",
    val contactRole: String = "",
    val contactPhone: String = "",
)

enum class ContractStatus(val label: String, val emoji: String) {
    ACTIVE  ("فعال",   "🟢"),
    WARNING ("هشدار",  "🟠"),
    CRITICAL("بحرانی", "🔴"),
    EXPIRED ("منقضی",  "⚫"),
    UNKNOWN ("نامشخص", "⚪");

    companion object {
        fun from(days: Long): ContractStatus = when {
            days < 0  -> EXPIRED
            days < 30 -> CRITICAL
            days < 60 -> WARNING
            else      -> ACTIVE
        }
    }
}

enum class SortField(val label: String) {
    DAYS_REMAINING("روزهای باقیمانده"),
    TITLE("عنوان"),
    COMPANY("شرکت"),
    STATUS("وضعیت"),
}

enum class AppThemeId(val displayName: String, val emoji: String) {
    LIGHT       ("روشن",         "☀️"),
    DARK_NAVY   ("نیوی تاریک",   "🌊"),
    MIDNIGHT    ("نیمه‌شب",       "🌙"),
    DEEP_FOREST ("جنگل عمیق",    "🌲"),
    AMOLED      ("AMOLED",       "⚫"),
    SUNSET      ("غروب آفتاب",   "🌅"),
    OCEAN_GLASS ("شیشه اقیانوس", "🪟"),
    CUSTOM      ("سفارشی من",    "🎨"),
}

/** اسلات‌های رنگی قابل شخصی‌سازی در تم سفارشی، همراه با توضیح دقیق اینکه هرکدام کجا استفاده می‌شود. */
enum class ColorSlot(val label: String, val hint: String) {
    BACKGROUND      ("پس‌زمینه اصلی",     "رنگ پشت کل صفحه‌ها"),
    SURFACE         ("پس‌زمینه کارت‌ها",  "پشت کارت‌ها و باکس‌های اطلاعات"),
    SURFACE_ELEVATED("پس‌زمینه نوار بالا", "پشت نوار بالا و نوار پایین"),
    PRIMARY         ("رنگ اصلی/برند",     "دکمه‌ها، آیکون فعال، تاکیدها"),
    ON_PRIMARY      ("متن روی رنگ اصلی",  "نوشته‌ای که روی دکمه‌ی اصلی می‌آید"),
    TEXT_PRIMARY    ("متن اصلی",          "عنوان‌ها و متن پررنگ"),
    TEXT_SECONDARY  ("متن فرعی",          "توضیحات و متن کم‌رنگ‌تر"),
    TEXT_MUTED      ("متن کم‌رنگ",        "برچسب‌ها و متن‌های خیلی کم‌رنگ"),
    OUTLINE         ("خط دور اجزا",       "بوردر و خط جداکننده‌ها"),
}
