# 📋 Contracts Manager — Complete Feature Documentation

## Overview
A local Android app for managing Persian (Jalali/Shamsi) contracts.
No internet required. All data stays on your device.
Built with Kotlin + Jetpack Compose + Material Design 3.

---

## 🗂️ SCREENS

### 1. Dashboard (داشبورد)
- Animated donut chart showing contract status breakdown
- Center of donut shows total contract count
- Status breakdown list: Active / Warning / Critical / Expired with counts
- Timeline bar: proportional colored bar showing all statuses at a glance
- "Urgent Contracts" section: top 5 critical/warning contracts sorted by days remaining
- Each urgent card shows days remaining with color-coded badge
- Progress bar on each card showing % of contract duration elapsed
- "View All" button navigating to full list
- Empty state with instructions when no file loaded

### 2. Contract List (قراردادها)
- Full list of all contracts sorted by selected field
- Real-time fuzzy search with typo tolerance
- Filter chips: All / Active / Warning / Critical / Expired
- Status count badges on each chip
- Star (⭐) toggle button in top bar to show only starred contracts
- Sort bottom sheet: Days Remaining / Title / Company / Status
- File upload button (📤) in top bar
- Each card shows:
  - Contract title (bold, 2 lines max)
  - Company name
  - Days remaining badge (color-coded)
  - Star/unstar button
  - Start date / End date / Duration
  - Animated progress bar (% elapsed)
  - Left colored accent bar matching status
- Animated card entrance (fade + slide)
- Loading spinner while parsing Excel
- Error state with dismiss button
- Empty state with "Load Excel" button

### 3. Contract Detail (جزئیات)
- Hero card with status color background
- Contract title, company, contract number
- Animated pulsing status dot
- Giant days counter (remaining or elapsed)
- Animated progress bar with % text
- Star toggle in top bar
- Share button → sends formatted text via any app
- Dates section: Start/End in Jalali + Gregorian
- Duration in months
- Financial section: Amount (Rials) + Payment method
- Info section: Contract type + File link
- Original notes from Excel
- Personal note section:
  - Displays saved note or "No note" placeholder
  - Edit button → inline text field
  - Save button → persists in memory

### 4. Jalali Calendar (تقویم)
- Month/year header with prev/next navigation
- 7-column weekday grid (Sat → Fri, Persian)
- Each day cell shows:
  - Day number
  - Color-coded background if contract ends that day
  - Up to 3 colored dots for multiple contracts
  - Tappable → navigates to contract detail
- Contracts list below calendar for current month
- Sorted by days remaining
- Shows contract title, company, end day number
- "No contracts this month" empty state

### 5. Settings (تنظیمات)
- Theme picker: 2-column grid of 6 themes
  - Each theme card shows color preview dots
  - Selected theme has highlighted border + checkmark
  - Tap to switch instantly (live preview)
- Notification slider: 7 to 90 days before deadline
- App info: version, platform, date engine description

---

## 🎨 THEMES (6 Total)

| Name | Background | Primary | Mood |
|------|-----------|---------|------|
| 🌊 Dark Navy | #0D1B2A | #00BCD4 Teal | Professional |
| 🌙 Midnight | #0A0A1A | #8B5CF6 Violet | Sleek |
| 🌲 Deep Forest | #0A1A0F | #4CAF50 Green | Natural |
| ⚫ AMOLED | #000000 | #FFD700 Gold | Battery saver |
| 🌅 Sunset | #1A0A05 | #FF6B35 Orange | Warm |
| 🪟 Ocean Glass | #051520 | #00E5FF Cyan | Cool |

All themes have:
- background, surface, surfaceElevated layers
- primary + onPrimary colors
- textPrimary, textSecondary, textMuted hierarchy
- outline color for borders

---

## 🔴 STATUS SYSTEM

Ported exactly from original VBA macro logic:

| Status | Condition | Color (VBA match) |
|--------|-----------|-------------------|
| 🟢 Active | 60+ days remaining | RGB(173,216,230) Light Blue |
| 🟠 Warning | 30–59 days remaining | RGB(255,178,102) Orange |
| 🔴 Critical | 0–29 days remaining | RGB(255,102,102) Red |
| ⚫ Expired | Past end date (negative days) | RGB(169,169,169) Gray |

---

## 🔍 FUZZY SEARCH

Algorithm: Combined exact match + word match + Levenshtein distance

Priority order:
1. Exact substring match → score 90
2. All words found → score up to 85
3. Levenshtein distance ≤ 2 (for queries 3+ chars) → score 50
4. Character overlap ≥ 60% → score proportional

Searches across: Title + Company + Contract Number simultaneously
Results sorted by relevance score descending

---

## 🗓️ JALALI DATE ENGINE

File: `JalaliConverter.kt`
Ported line-by-line from VBA `JalaliToGregorian()` function.
Produces identical results to the Excel macro.

Functions:
- `toGregorian(jalaliStr)` → Triple(year, month, day)
- `isValid(s)` → validates format and range (1300–1500)
- `daysRemaining(endJalali)` → Long (negative if expired)
- `durationMonths(start, end)` → Int
- `formatGregorian(triple)` → "YYYY-MM-DD"
- Julian Day Number diff for accurate day calculation

---

## 📊 EXCEL PARSER

Library: Apache POI 5.2.5
Supported formats: .xlsx, .xlsm
Sheet name: "قراردادها" (falls back to first sheet)

Column mapping:
- A (col 0) → Contract Number
- B (col 1) → Title
- C (col 2) → Company
- G (col 6) → Start Date Jalali
- H (col 7) → End Date Jalali
- K (col 10) → Amount
- L (col 11) → Payment Method
- N (col 13) → Contract Type
- O (col 14) → Notes
- P (col 15) → File Link

Skips rows where H column is not a valid Jalali date.
File saved to internal storage (filesDir) on first load.
Auto-loads from cache on every subsequent app open.

---

## 🔔 NOTIFICATIONS

Worker: `DeadlineWorker` (WorkManager PeriodicWork)
Schedule: Every 24 hours, fires at 9:00 AM
Survives device reboot via `BootReceiver`

3 separate notifications:
1. ID 1 → Critical contracts (< 30 days) — shows up to 3 names
2. ID 2 → Warning contracts (30–60 days) — shows up to 3 names
3. ID 3 → Expired contracts — shows count

Each notification:
- Opens app on tap
- Auto-dismisses on tap
- High priority + vibration
- BigText style for long names

---

## 🌙 HOME SCREEN WIDGET

Library: Jetpack Glance 1.0.0
Size: 250×180dp minimum, resizable

Displays:
- App title + total contract count
- Color-coded count pills: 🔴 Critical / 🟠 Warning / ⚫ Expired
- Up to 4 urgent contracts with title + days remaining
- "All Active" message when no urgent contracts
- "Open App" tap target
- Refreshes every hour

---

## ⭐ STARRED CONTRACTS

- Toggle star on any contract from list or detail screen
- Filter button in list screen to show only starred
- Star persists in ViewModel state during session
- Gold star icon (⭐) vs outline star

---

## 📤 SHARE

Available from:
- Contract list (long press menu)
- Contract detail screen (share icon in top bar)

Share text format:
```
📋 [Contract Title]
🏢 [Company]
📅 End Date: [Jalali Date]
⏳ [Status] – [X days remaining / X days past]
Sent from Contracts Manager App
```

Opens Android share sheet → WhatsApp, Email, Telegram, etc.

---

## 💾 DATA PERSISTENCE

- Excel file: copied to `context.filesDir/contracts.xlsm`
- Auto-loaded on every app start
- Star state + personal notes: in-memory (ViewModel)
  → resets on app restart (future: DataStore persistence)
- Theme selection: in-memory (future: DataStore persistence)
- Notification schedule: WorkManager (survives reboot)

---

## 🏗️ ARCHITECTURE

Pattern: MVVM (Model-View-ViewModel)

```
UI Layer (Compose Screens)
    ↕ StateFlow
ViewModel (ContractViewModel)
    ↕ suspend functions
Repository (ExcelRepository)
    ↕ Apache POI
Storage (filesDir cache)
```

State management: Kotlin StateFlow + collectAsState()
Side effects: LaunchedEffect, rememberCoroutineScope
Navigation: Navigation Compose with named routes

---

## 📦 DEPENDENCIES

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 1.9.22 | Language |
| Compose BOM | 2024.02.00 | UI framework |
| Material3 | BOM | Design system |
| Navigation Compose | 2.7.6 | Screen routing |
| ViewModel Compose | 2.7.0 | State management |
| WorkManager | 2.9.0 | Background tasks |
| Glance | 1.0.0 | Home screen widget |
| DataStore | 1.0.0 | Key-value storage |
| Apache POI OOXML | 5.2.5 | Excel parsing |
| Coroutines Android | 1.7.3 | Async operations |
| Kotlinx Serialization | 1.6.3 | JSON (future use) |

---

## 🔒 PERMISSIONS

| Permission | Why |
|-----------|-----|
| POST_NOTIFICATIONS | Send deadline alerts |
| RECEIVE_BOOT_COMPLETED | Reschedule notifications after reboot |
| READ_MEDIA_IMAGES | File picker access (Android 13+) |

No internet permission. No data leaves the device.

---

## 📐 UI DETAILS

- RTL layout (supportsRtl="true")
- Status bar: transparent
- Navigation bar: transparent
- Edge-to-edge display
- Animated transitions: fadeIn + slideInVertically
- Pulsing dot animation on Critical/Warning status
- Animated donut chart on load (900ms ease)
- Animated progress bars (600ms)
- Color-animated status chips (180ms)
- Bottom navigation bar (4 tabs)
- Top bar hidden on Detail screen
- Modal bottom sheet for sort options
