# 🚀 Complete Setup & Run Guide
## Contracts Manager — Android App

---

## PART 1: ONE-TIME SETUP (Mac)

### Step 1 — Install Homebrew
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
When asked for password, type your Mac password (invisible while typing) → Enter
When done, run:
```bash
echo >> ~/.zprofile
echo 'eval "$(/opt/homebrew/bin/brew shellenv zsh)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv zsh)"
```

---

### Step 2 — Install Android Debug Bridge (ADB)
```bash
curl -o ~/Downloads/platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-darwin.zip
cd ~/Downloads && unzip platform-tools.zip
echo 'export PATH="$HOME/Downloads/platform-tools:$PATH"' >> ~/.zprofile
source ~/.zprofile
```

Verify:
```bash
adb version
```
Should show: `Android Debug Bridge version 1.0.xx`

---

### Step 3 — Install Android Studio
1. Go to: https://developer.android.com/studio
2. Download **Android Studio** for Mac (Apple Silicon if M1/M2/M3, Intel if older Mac)
3. Open the .dmg → drag Android Studio to Applications
4. Launch Android Studio → complete setup wizard
5. When asked about SDK → let it install (takes ~5 min)

---

### Step 4 — Verify Android SDK installed
```bash
ls ~/Library/Android/sdk
```
Should show folders like: `build-tools  emulator  platforms  platform-tools  tools`

If empty or not found, inside Android Studio:
```
Android Studio → Settings (⌘,)
→ Languages & Frameworks → Android SDK
→ SDK Platforms tab → check "Android 14 (API 34)" → Apply
→ SDK Tools tab → check Build Tools, Platform Tools, Emulator → Apply
```

---

## PART 2: GET THE PROJECT

### Option A — From ZIP file
```bash
cd ~/Downloads
rm -rf ContractsApp2
unzip ContractsApp_Final.zip
```

Verify files:
```bash
find ~/Downloads/ContractsApp2 -type f | sort
```
Should show exactly 24 files, no `{` in any name.

### Option B — From GitHub (after you push)
```bash
cd ~/Downloads
git clone https://github.com/YOUR_USERNAME/contracts-manager.git ContractsApp2
```

---

## PART 3: OPEN IN ANDROID STUDIO

```
1. Android Studio → File → Open
2. Navigate to: Downloads/ContractsApp2
3. Click OK
4. Wait for "Gradle sync" to finish (bottom progress bar)
   → First time: 5-10 min (downloads dependencies)
   → Next times: 30 sec
```

If you see "Gradle sync failed":
- Make sure VPN is ON (needed to download from Maven Central in Iran)
- Click "Try Again" in the yellow bar at top

---

## PART 4: CONNECT SAMSUNG PHONE

### On Samsung phone:

**Enable Developer Options:**
```
Settings → About phone → Software information
→ Tap "Build number" 7 times quickly
→ "You are now a developer!" message appears
```

**Enable USB Debugging:**
```
Settings → Developer options
→ USB debugging → toggle ON
→ Confirm "OK"
```

**Connect cable:**
- Use original Samsung USB cable
- On phone popup: "Allow USB debugging?" → tap "Allow"
- Check "Always allow from this computer" → OK

### On Mac, verify connection:
```bash
adb devices
```
Expected output:
```
List of devices attached
R5CT1234567    device
```

If it shows `unauthorized` → check phone for popup again
If empty → try different USB cable or port

---

## PART 5: RUN THE APP

### In Android Studio:

```
1. Top bar → device dropdown (next to ▶️ button)
2. Select your Samsung Galaxy (should appear automatically)
3. Click ▶️ green Run button
4. Wait for build (1-3 min first time)
5. App opens automatically on your phone
```

### Watch build progress:
```
Bottom panel → Build tab → shows progress
"BUILD SUCCESSFUL" → app launches on phone
```

---

## PART 6: USE THE APP

### First time:
```
1. Open app on Samsung
2. Tap "قراردادها" (Contracts) tab at bottom
3. Tap "انتخاب فایل اکسل" button
4. Navigate to your Contracts_Smart.xlsm file
5. Tap the file → data loads automatically
6. ✅ Done! File is saved — never need to reload
```

### Home Screen Widget:
```
Long press empty area on Samsung home screen
→ Widgets
→ Search "Contracts" or "قرارداد"
→ Long press widget → drag to home screen
→ Widget shows live contract status
```

### Enable Notifications:
```
When app asks "Allow notifications" → Allow
Notifications fire automatically every day at 9:00 AM
Works even after phone restart
```

---

## PART 7: BUILD APK (to share with others)

### Debug APK (for testing):
```
Android Studio → Build → Build Bundle(s)/APK(s) → Build APK(s)
```
Wait ~2 min, then:
```
Click "locate" in bottom notification
File location: ContractsApp2/app/build/outputs/apk/debug/app-debug.apk
```

### Send APK to Samsung:
```bash
# Via ADB (fastest):
adb install ~/Downloads/ContractsApp2/app/build/outputs/apk/debug/app-debug.apk
```

Or send via:
- AirDrop (if Samsung has AirDrop alternative)
- Google Drive → download on Samsung
- Telegram → send to yourself → download
- USB cable → copy to Samsung Downloads folder

### Install on Samsung:
```
Settings → Apps → Special app access → Install unknown apps
→ Files app (or wherever you saved APK) → Allow
Open APK file → Install
```

---

## PART 8: PUSH TO GITHUB

### First time setup:
```bash
# Install Git (if not installed)
brew install git

# Configure Git
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

### Create GitHub repository:
```
1. Go to github.com → Sign in
2. Click "+" → New repository
3. Name: contracts-manager
4. Description: Android app for managing Jalali contracts
5. Choose Public or Private
6. ⚠️ DO NOT check "Initialize with README" (we have our own)
7. Click "Create repository"
```

### Push code:
```bash
cd ~/Downloads/ContractsApp2

# Initialize git
git init

# Add all files
git add .

# First commit
git commit -m "feat: initial release — Contracts Manager Android App

- Jalali calendar with contract dots
- 6 color themes (Dark Navy, Midnight, Forest, AMOLED, Sunset, Ocean Glass)
- Fuzzy search with Levenshtein algorithm
- Home screen widget (Glance)
- Daily deadline notifications (WorkManager)
- Apache POI Excel parser for .xlsm/.xlsx
- Dashboard with animated donut chart
- Star/favorite + personal notes
- Share contracts via any app
- VBA Jalali algorithm ported to Kotlin"

# Connect to GitHub
git remote add origin https://github.com/YOUR_USERNAME/contracts-manager.git

# Set main branch
git branch -M main

# Push
git push -u origin main
```

When asked for username/password:
- Username: your GitHub username
- Password: use a **Personal Access Token** (not your GitHub password)
  ```
  GitHub → Settings → Developer settings
  → Personal access tokens → Tokens (classic)
  → Generate new token → check "repo" → Generate
  → Copy the token → paste as password
  ```

### After pushing, your repo will have:
```
github.com/YOUR_USERNAME/contracts-manager
├── README.md          ← Project overview + screenshots placeholder
├── FEATURES.md        ← Complete feature documentation
├── SETUP.md           ← This file
├── .gitignore         ← Excludes build files + local.properties
├── app/               ← Android app source code
└── gradle/            ← Build configuration
```

---

## TROUBLESHOOTING

### "Gradle sync failed: Could Not Resolve..."
```
→ Turn VPN ON
→ File → Sync Project with Gradle Files
```

### "SDK location not found"
```bash
echo "sdk.dir=/Users/$(whoami)/Library/Android/sdk" > ~/Downloads/ContractsApp2/local.properties
```
Then sync again in Android Studio.

### "ADB device not found"
```bash
# Restart ADB server
adb kill-server
adb start-server
adb devices
```
Also try different USB cable or port.

### "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
```bash
# Uninstall old version first
adb uninstall com.contracts.app
# Then install again
adb install app-debug.apk
```

### App crashes on launch
```bash
# See crash logs
adb logcat -s AndroidRuntime:E | head -50
```
Send the output as a GitHub issue.

### Excel file not loading
- Make sure sheet is named exactly: `قراردادها`
- If not, app uses first sheet automatically
- Column H must have valid Jalali dates (format: 1402/06/31)
- Rows with empty H column are skipped

### Notifications not appearing
```
Samsung Settings → Apps → Contracts Manager
→ Notifications → Allow all
```
Also check: Do Not Disturb is OFF during 9 AM.

---

## USEFUL ADB COMMANDS

```bash
# List connected devices
adb devices

# Install APK
adb install app-debug.apk

# Uninstall app
adb uninstall com.contracts.app

# See live logs
adb logcat

# See only crash logs
adb logcat -s AndroidRuntime:E

# Clear app data (reset)
adb shell pm clear com.contracts.app

# Take screenshot
adb exec-out screencap -p > screenshot.png

# Open app
adb shell am start -n com.contracts.app/.MainActivity

# Check battery (for widget testing)
adb shell dumpsys battery
```

---

## FILE LOCATIONS ON DEVICE

```
App data: /data/data/com.contracts.app/
Cached Excel: /data/data/com.contracts.app/files/contracts.xlsm
App logs: accessible via adb logcat
```

---

## QUICK REFERENCE

| Task | Command/Location |
|------|-----------------|
| Open project | Android Studio → File → Open → ContractsApp2 |
| Sync gradle | File → Sync Project with Gradle Files |
| Run on phone | ▶️ button in Android Studio |
| Build APK | Build → Build APK(s) |
| Install APK | `adb install app-debug.apk` |
| See logs | `adb logcat` |
| Push to GitHub | `git push origin main` |
| Reset app data | `adb shell pm clear com.contracts.app` |
