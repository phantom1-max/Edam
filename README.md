# Edam — Learn Anything (Android & Multi-Platform PC)

Edam is an AI-powered interactive learning application built with Kotlin, Jetpack Compose (Material 3), Firebase Firestore, Room Database, and Retrofit.

## Features

- **Daily Learning Streak with Firestore Cloud Sync & Push Notifications**: Tracks consecutive days of learning activity in local preferences and Firebase Firestore (`/users/{userId}/streaks/daily`). Features a prominent flame icon with current streak counter on the home dashboard, streak freeze protection, 7-day consistency strip, daily Android push notification alerts via WorkManager, and in-app streak defense banners when the user hasn't studied today yet.
- **Firestore Badges & Learning Milestones**: Tracks multi-domain achievements in Firestore (Daily Streaks, Stock Market, Chess Tactics, Coding, and Polymath courses) displayed dynamically with unlock progress in the user profile.
- **Companions & Mascot System**:
  - **Edam**: The main hooded learning mascot with sprout, tablet, and backpack.
  - **RoboBroker / Businessman Robot**: Stock Market executive robot with titanium chassis, golden stock ticker coin crest, and red tie.
  - **Bit Virus**: Coding & cyber companion with matrix emerald green capsid, viral bit nodes, and terminal visor eyes.
  - **Vex**: Grandmaster Chess companion guiding 8x8 tactics and 5-unit curriculum.
- **First Hero Section with 3 Highlighted Domains & Learn Any Subject AI**:
  - Highlights **Chess GM Academy & Tactics**, **Stock Market & Live Trading Simulator**, and **Daily Flashcard Streaks**.
  - Interactive **"Learn Any Other Subject on Earth"** launcher allowing learners to generate custom curricula on any topic (Python, Quantum Physics, World History, etc.) with AI.
- **Multi-Provider Authentication**: Separate "Sign In with Google" and "Create Account with Google" flows, along with Apple, GitHub, Facebook, Phone SMS OTP, Email/Password, and Guest access.
- **Pre-Built APK in Repository**:
  - Direct download: [`releases/edam-android-latest.apk`](releases/edam-android-latest.apk) (v1.3.0 signed package).
  - Versioned archive: [`releases/edam-android-v1.3.0.apk`](releases/edam-android-v1.3.0.apk).

## Editable Versions & Multi-Platform Release (Windows, macOS, Linux, Android)

You can easily bump versions and trigger builds for **Android**, **Windows**, **macOS**, and **Linux**:

1. **Edit `release-config.env`** in the root directory:
   ```env
   APP_VERSION_NAME=1.3.0
   APP_VERSION_CODE=4
   RELEASE_TAG=v1.3.0
   RELEASE_TITLE=Edam v1.3.0 — Multi-Platform Release
   BUILD_ANDROID_APK=true
   BUILD_WINDOWS_PC=true
   BUILD_MACOS_PC=true
   BUILD_LINUX_PC=true
   ```
2. Commit and push the file to GitHub.
3. The GitHub Actions workflow (`.github/workflows/release-apk.yml`) will:
   - Build the Android APK and commit it to `releases/`.
   - Build desktop packages from `desktop-pc/` for **Windows** (`.msi` / `.exe`), **macOS** (`.dmg`), and **Linux** (`.deb`).
   - Create a GitHub Release with all platform installers attached.

## Running the Desktop PC Version Locally

The standalone desktop project is located in `desktop-pc/`:
```bash
# Run on Windows, macOS, or Linux
gradle -p desktop-pc run

# Package native installer for your current OS
gradle -p desktop-pc packageDistributionForCurrentOS
```
