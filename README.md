<div align="center">

# 📋 Contracts Manager

**An Android app for tracking contracts and their deadlines, with a Jalali (Shamsi) calendar, smart alerts, and a home-screen widget.**

<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" />
<img src="https://img.shields.io/badge/Material_3-757575?style=flat-square&logo=materialdesign&logoColor=white" />
<img src="https://img.shields.io/badge/Android_8.0+-3DDC84?style=flat-square&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" />

</div>

---

## ✨ Features

| | Feature | Description |
|:-:|---|---|
| 📊 | **Dashboard** | Animated donut chart, status timeline, and the most urgent contracts |
| 🔍 | **Smart search** | Fuzzy search that tolerates typos (Levenshtein distance) |
| 📅 | **Jalali calendar** | Monthly calendar view with a dot on each contract date |
| ⭐ | **Stars & notes** | Mark favourite contracts and add personal notes |
| 🎨 | **6 themes** | Dark Navy, Midnight, Deep Forest, AMOLED, Sunset, Ocean Glass |
| 🧩 | **Home-screen widget** | Live summary of contract statuses (Glance) |
| 🔔 | **Deadline alerts** | Daily check at 9 AM with Critical / Warning / Expired alerts; survives reboots |
| 📤 | **Share** | Send contract details to any app |
| 💾 | **Excel import** | Load an `.xlsx`/`.xlsm` file once and it auto-loads on every launch |

## 🔔 Status rules

| Status | Condition |
|---|---|
| 🟢 Active | 60+ days remaining |
| 🟠 Warning | 30–60 days remaining |
| 🔴 Critical | under 30 days remaining |
| ⚫ Expired | past the end date |

## 📊 Excel format

The app reads the sheet named **«قراردادها»**, or the first sheet if that one isn't found.

| Column | Field | Column | Field |
|---|---|---|---|
| A | Contract number | K | Amount |
| B | Title | L | Payment method |
| C | Company | N | Contract type |
| G | Start date (Jalali `YYYY/MM/DD`) | O | Notes |
| H | End date (Jalali `YYYY/MM/DD`) | P | File link |

## 🛠️ Tech stack

Kotlin · Jetpack Compose · Material 3 · Apache POI (Excel parsing) · WorkManager · Glance · Navigation Compose · ViewModel + StateFlow · Vazirmatn font

```
app/src/main/java/com/contracts/app/
├── MainActivity.kt            Entry point + navigation
├── data/                      Models + ExcelRepository (Apache POI)
├── domain/                    ContractViewModel, SettingsStore
├── notifications/             DeadlineWorker (WorkManager)
├── ui/                        Components, screens, 6 themes
├── utils/                     JalaliConverter, FuzzySearch
└── widget/                    Glance home-screen widget
```

More detail: [FEATURES.md](FEATURES.md) · [SETUP.md](SETUP.md)

## 🚀 Getting started

```bash
git clone https://github.com/negarsh76/Contracts-Manager.git
```

Open the project in Android Studio (Hedgehog or newer, JDK 11+), connect a device running Android 8.0 or later, and press **Run ▶**. On first launch, tap **Load Excel File** and pick your contracts file.

## 📄 License

MIT

---

<p align="center">Made by <a href="https://github.com/negarsh76">Negar Shojaei</a></p>
